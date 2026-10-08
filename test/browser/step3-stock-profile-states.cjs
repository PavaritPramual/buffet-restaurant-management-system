// Controlled UI states, separate from real Stock/Profile and migration evidence.
const assert = require('node:assert/strict')
const fs = require('node:fs/promises')
const path = require('node:path')
const {execFileSync} = require('node:child_process')
const {validateRuntimeUrls} = require('./step3-runtime-config.cjs')
const mode=process.env.FINAL_ENVIRONMENT,web=process.env.FINAL_WEB_URL,api=process.env.FINAL_API_URL
assert(['local-h2','local-postgres'].includes(mode),'Fixtures require isolated local runtime')
validateRuntimeUrls(mode,web,api)
const {chromium}=require(process.env.PLAYWRIGHT_MODULE || 'playwright')
const output=path.resolve(process.env.FINAL_STOCK_PROFILE_EVIDENCE_DIR || 'test/reports/step3-stock-profile')+'-fixtures'
const sourceCommit=execFileSync('git',['rev-parse','HEAD'],{encoding:'utf8'}).trim(),results=[]
async function main() {
  await fs.mkdir(output,{recursive:true})
  const browser=await chromium.launch({channel:process.env.FINAL_BROWSER_CHANNEL || 'chrome',headless:true})
  try {
    const context=await browser.newContext({viewport:{width:1280,height:1000}}),page=await context.newPage()
    await page.goto(web+'/admin');await page.getByLabel('ชื่อผู้ใช้',{exact:true}).fill(process.env.FINAL_MANAGER_USERNAME)
    await page.getByLabel('รหัสผ่าน',{exact:true}).fill(process.env.FINAL_MANAGER_PASSWORD)
    await page.getByRole('button',{name:'เข้าสู่ระบบ',exact:true}).click();await page.waitForURL('**/admin/stock')
    const scenarios=[
      {name:'stock',url:'/admin/stock',endpoints:['/stock','/stock/transactions'],empty:'ยังไม่มีรายการสต็อก'},
      {name:'stock-items',url:'/admin/stock-items',endpoints:['/stock'],empty:'ยังไม่มีรายการ'},
      {name:'profile',url:'/admin/users',endpoints:['/admin/users'],empty:'ไม่พบข้อมูลพนักงาน'},
    ]
    for(const config of scenarios) for(const state of ['loading','empty','error']) {
      let release
      const gate=new Promise(resolve=>{release=resolve})
      const handler=async route=>{
        const endpoint=new URL(route.request().url()).pathname.replace('/api/v1','')
        if(!config.endpoints.includes(endpoint)||route.request().method()!=='GET') return route.continue()
        if(state==='loading') await gate
        await route.fulfill({status:state==='error'?503:200,contentType:'application/json',body:JSON.stringify(state==='error'?{status:503,error:'Service Unavailable',message:'Controlled Stock/Profile error',path:'/api/v1'+endpoint,timestamp:new Date().toISOString()}:[])})
      }
      await page.route('**/api/v1/**',handler)
      try {
        await page.goto(web+config.url)
        if(state==='empty') await page.getByText(config.empty,{exact:true}).waitFor()
        else if(state==='error') await page.getByRole('alert').waitFor()
        else await page.getByText(/กำลังโหลด/).first().waitFor()
        assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth),1280,'Document overflow')
        await page.screenshot({path:path.join(output,`${config.name}-${state}-1280.png`),fullPage:true})
        results.push({page:config.name,state,result:'PASS',width:1280,httpMocks:true})
      } finally {release();await page.unroute('**/api/v1/**',handler)}
    }
    const legacy=[{id:999999,username:'isolated-legacy-fixture',displayName:'ชื่อเดิมของพนักงาน',email:null,role:'SERVICE_STAFF',active:true,firstName:null,lastName:null,phoneNumber:null}]
    await page.route('**/api/v1/admin/users',route=>route.fulfill({status:200,contentType:'application/json',body:JSON.stringify(legacy)}))
    await page.goto(web+'/admin/users');await page.getByText('ยังไม่มีชื่อ-นามสกุล (ใช้ชื่อที่แสดงเดิม)',{exact:true}).waitFor()
    await page.getByRole('button',{name:'เติมข้อมูล',exact:true}).waitFor()
    await page.screenshot({path:path.join(output,'profile-legacy-fallback-1280.png'),fullPage:true})
    results.push({page:'profile',state:'legacy fallback',result:'PASS',width:1280,httpMocks:true,scope:'UI presentation only; real legacy migration is covered by automated migration tests'})
    console.log(`PASS Stock/Profile controlled states: ${results.length}`)
  } finally {
    await browser.close()
    await fs.writeFile(path.join(output,'results.json'),JSON.stringify({sourceCommit,executedAt:new Date().toISOString(),httpMocks:true,publicAcceptance:false,results},null,2)+'\n')
  }
}
main().catch(error=>{console.error(error.message);process.exitCode=1})

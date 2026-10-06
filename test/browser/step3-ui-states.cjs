// Controlled response fixtures on loopback only. These are not real API outage/public acceptance evidence.
const {chromium}=require(process.env.PLAYWRIGHT_MODULE || 'playwright')
const assert=require('node:assert/strict'), fs=require('node:fs/promises'), path=require('node:path')
const web=process.env.FINAL_WEB_URL || 'http://localhost:5175',api=process.env.FINAL_API_URL || 'http://localhost:8085/api/v1'
for(const address of [web,api]) assert(['localhost','127.0.0.1'].includes(new URL(address).hostname),'Fixtures require loopback')
const output=path.resolve(process.env.FINAL_STATE_EVIDENCE_DIR || 'test/reports/step3-ui-states')
const cases=[
 ['customer',360,'/customer/qr','/dining-sessions/1/menu','กำลังตรวจสอบรอบการรับประทานและโหลดเมนู…','ยังไม่มีเมนูในหมวดนี้'],
 ['staff',768,'/staff/tables','/tables','กำลังโหลดโต๊ะและรอบกิน…','ยังไม่มีโต๊ะ'],
 ['kitchen',768,'/kitchen','/orders/incoming','กำลังโหลดออเดอร์…','ยังไม่มีออเดอร์เข้าครัว'],
 ['manager',1280,'/admin/stock','/stock','กำลังโหลด...','ยังไม่มีรายการสต็อก'],
]
const results=[]
async function main(){
 await fs.mkdir(output,{recursive:true})
 const browser=await chromium.launch({channel:'chrome',headless:true})
 try{
  for(const [name,width,url,endpoint,loading,empty] of cases){
   const context=await browser.newContext({viewport:{width,height:1000}}),page=await context.newPage()
   if(name!=='customer'){
    const key=name.toUpperCase(), username=process.env[`FINAL_${key}_USERNAME`],password=process.env[`FINAL_${key}_PASSWORD`]
    assert(username && password,`Missing ${key} credentials`)
    assert((await context.request.post(api+'/auth/login',{data:{username,password},headers:{Origin:new URL(web).origin}})).ok())
   }else{
    // Numeric ID belongs exclusively to this response fixture, never a real customer grant.
    await page.route(api+'/dining-sessions/customer-context',r=>r.fulfill({json:{sessionId:1,packageId:1,tableNumber:'Fixture',sessionStatus:'ACTIVE'}}))
    await page.route(api+'/dining-sessions/1/package',r=>r.fulfill({json:{id:1,name:'Fixture package',price:299,active:true}}))
    await page.route(api+'/dining-sessions/1/orders',r=>r.fulfill({json:[]}))
    await page.route(api+'/dining-sessions/1/bill-status',r=>r.fulfill({json:{sessionId:1,status:'NOT_REQUESTED',requestedAt:null,dueAmount:299,paidAmount:0,bill:{sessionId:1,subtotalAmount:299,discountAmount:0,totalAmount:299}}}))
   }
   let release,state='loading'; const gate=new Promise(done=>{release=done})
   await page.route(api+endpoint,async r=>{
    if(state==='loading') await gate
    await r.fulfill(state==='error'?{status:503,json:{message:'ข้อมูลทดสอบ: เชื่อมต่อไม่สำเร็จ กรุณาลองใหม่'}}:{json:[]})
   })
   const shot=async stateName=>{
    assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth),width)
    await page.screenshot({path:path.join(output,`${name}-${stateName}-${width}.png`),fullPage:true})
   }
   await page.goto(web+url,{waitUntil:'domcontentloaded'})
   await page.getByText(loading,{exact:true}).waitFor(); await shot('loading')
   state='empty';release();await page.getByText(empty,{exact:true}).waitFor();await shot('empty')
   state='error';await page.reload();await page.getByRole('alert').waitFor();await shot('error')
   const style=await page.locator('body').evaluate(el=>({font:getComputedStyle(el).fontFamily,primary:getComputedStyle(el).getPropertyValue('--color-primary').trim()}))
   assert(style.font.includes('Noto Sans Thai'));assert.equal(style.primary,'#9a3412')
   results.push({name,width,states:['loading','empty','error'],result:'PASS',style,source:name==='customer'?'Full Customer HTTP context fixture; no real grant':'Real employee cookie/shell; targeted data response fixture'})
   await context.close();console.log(`PASS ${name}: loading/empty/error ${width}px`)
  }
  await fs.writeFile(path.join(output,'results.json'),JSON.stringify({executedAt:new Date().toISOString(),environment:'local H2; controlled HTTP response fixtures',publicAcceptance:false,results},null,2))
 }finally{await browser.close()}
}
main().catch(()=>{console.error('State fixture failed; inspect local runtime');process.exitCode=1})

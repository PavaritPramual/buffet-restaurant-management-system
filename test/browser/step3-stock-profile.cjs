// PR-independent real HTTP/browser checks; run before merge on isolated feature code.
const assert = require('node:assert/strict')
const fs = require('node:fs/promises')
const path = require('node:path')
const crypto = require('node:crypto')
const {execFileSync} = require('node:child_process')
const {validateRuntimeUrls} = require('./step3-runtime-config.cjs')
const web = process.env.FINAL_WEB_URL, api = process.env.FINAL_API_URL, mode = process.env.FINAL_ENVIRONMENT
validateRuntimeUrls(mode,web,api)
assert.equal(process.env.FINAL_ALLOW_TEST_DATA,'true')
const {chromium} = require(process.env.PLAYWRIGHT_MODULE || 'playwright')
const output = path.resolve(process.env.FINAL_STOCK_PROFILE_EVIDENCE_DIR || 'test/reports/step3-stock-profile')
const sourceCommit = execFileSync('git',['rev-parse','HEAD'],{encoding:'utf8'}).trim()
const runnerSha256 = crypto.createHash('sha256').update(require('node:fs').readFileSync(__filename)).digest('hex')
const startedAt = new Date().toISOString(), run = `SP${Date.now().toString().slice(-9)}`, results = [], contexts = {}
let scenario = 'startup'
const pass = detail => {results.push({name:scenario,result:'PASS',detail});console.log(`PASS ${scenario}`)}
async function main() {
  await fs.mkdir(output,{recursive:true})
  const browser = await chromium.launch({channel:process.env.FINAL_BROWSER_CHANNEL || 'chrome',headless:true})
  try {
    for(const role of ['manager','supervisor','staff','kitchen','anonymous']) {
      const c=await browser.newContext({viewport:{width:1280,height:1000}}); c.page=await c.newPage(); contexts[role]=c
      if(role==='anonymous') continue
      await c.page.goto(web+'/admin')
      await c.page.getByLabel('ชื่อผู้ใช้',{exact:true}).fill(process.env[`FINAL_${role.toUpperCase()}_USERNAME`])
      await c.page.getByLabel('รหัสผ่าน',{exact:true}).fill(process.env[`FINAL_${role.toUpperCase()}_PASSWORD`])
      await c.page.getByRole('button',{name:'เข้าสู่ระบบ',exact:true}).click()
      await c.page.waitForURL(`**${role==='staff'?'/staff/tables':role==='kitchen'?'/kitchen':'/admin/stock'}`)
    }
    const request = (role,method,endpoint,data) => contexts[role].request[method](api+endpoint,{headers:{Origin:new URL(web).origin},...(data===undefined?{}:{data})})
    const read = async endpoint => {const r=await request('manager','get',endpoint);assert.equal(r.status(),200);return r.json()}
    const m=contexts.manager.page, s=contexts.supervisor.page
    const shot=async(page,file)=>{assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth),page.viewportSize().width,'Document overflow');await page.screenshot({path:path.join(output,file),fullPage:true})}
    const save=async(page,method,endpoint,button)=>{
      const waiting=page.waitForResponse(r=>new URL(r.url()).pathname.endsWith(endpoint)&&r.request().method()===method)
      await button.click(); const r=await waiting; assert.equal(r.status(),method==='POST'&&endpoint==='/stock/items'?201:200); return r.json()
    }
    scenario='Stock defaults and decimal target through Manager UI'
    const defaults=await request('manager','post','/stock/items',{sku:run+'-default',name:run+' default',unit:'kg',lowStockThreshold:0})
    assert.equal(defaults.status(),201); const defaultItem=await defaults.json()
    assert.equal(defaultItem.openingTargetStock,0); assert.equal(defaultItem.active,true);assert.equal(defaultItem.quantity,0)
    await m.goto(web+'/admin/stock-items')
    for(const [label,value] of Object.entries({'รหัสสต็อก':run,'ชื่อรายการ':run+' stock','หน่วย':'kg','ยอดแจ้งเตือนต่ำ':'1.125','ยอดเป้าหมายก่อนเปิดร้าน':'4.125'})) await m.getByLabel(label,{exact:true}).fill(value)
    let item=await save(m,'POST','/stock/items',m.getByRole('button',{name:'บันทึก',exact:true}))
    assert.equal(item.quantity,0);assert.equal(item.shortfall,4.125);assert.equal(item.active,true)
    const card=()=>m.locator('.ui-card').filter({has:m.getByRole('heading',{name:item.name,exact:true})})
    await m.getByText('บันทึกข้อมูลแล้ว',{exact:true}).waitFor()
    await card().getByRole('button',{name:'แก้ไข',exact:true}).click()
    await m.getByLabel('ยอดเป้าหมายก่อนเปิดร้าน',{exact:true}).fill('5.125')
    item=await save(m,'PUT',`/stock/items/${item.id}`,m.getByRole('button',{name:'บันทึก',exact:true}))
    await m.reload();await card().waitFor();assert.equal((await read('/stock')).find(x=>x.id===item.id).openingTargetStock,5.125)
    await shot(m,'stock-target-1280.png');pass({itemId:item.id,defaultTarget:0,defaultActive:true,target:5.125,quantity:0,shortfall:5.125})
    scenario='Negative target validation preserves stock'
    const before=await read('/stock')
    const rejected=await request('manager','put',`/stock/items/${item.id}`,{sku:item.sku,name:item.name,unit:item.unit,lowStockThreshold:1.125,openingTargetStock:-1})
    assert.equal(rejected.status(),400); const error=await rejected.json();assert.equal(error.status,400);assert.match(error.message,/openingTargetStock/)
    assert.deepEqual(await read('/stock'),before)
    await card().getByRole('button',{name:'แก้ไข',exact:true}).click()
    await m.getByLabel('ยอดเป้าหมายก่อนเปิดร้าน',{exact:true}).fill('-1')
    assert.equal(await m.getByLabel('ยอดเป้าหมายก่อนเปิดร้าน',{exact:true}).evaluate(e=>e.checkValidity()),false)
    await m.getByRole('button',{name:'ยกเลิกแก้ไข',exact:true}).click();pass({httpStatus:400,unchanged:true,browserMin:0})
    scenario='Supervisor stock-in and confirmed adjustment through UI'
    await s.reload()
    const row=()=>s.getByRole('row').filter({has:s.getByText(item.name,{exact:true})})
    await row().getByRole('button',{name:'รับเข้า',exact:true}).click()
    await s.getByLabel('จำนวนที่รับเข้า',{exact:true}).fill('2.125');await s.getByLabel('เหตุผล',{exact:true}).fill(run+' in')
    await save(s,'POST',`/stock/${item.id}/in`,s.getByRole('button',{name:'บันทึกรายการ',exact:true}))
    await row().getByRole('button',{name:'ปรับยอด',exact:true}).click()
    await s.getByLabel('ผลต่างที่ปรับ (kg)',{exact:true}).fill('1');await s.getByLabel('เหตุผล',{exact:true}).fill(run+' adjustment')
    await s.getByRole('button',{name:'บันทึกรายการ',exact:true}).click()
    await s.getByRole('dialog').waitFor();await shot(s,'stock-adjust-confirm-1280.png')
    assert.equal((await read(`/stock/transactions?itemId=${item.id}`)).length,1,'No adjustment before confirmation')
    await save(s,'POST',`/stock/${item.id}/adjustments`,s.getByRole('dialog').getByRole('button',{name:'ยืนยัน',exact:true}))
    await row().getByRole('button',{name:'รับเข้า',exact:true}).waitFor()
    const moved=(await read('/stock')).find(x=>x.id===item.id);assert.equal(moved.quantity,3.125);assert.equal(moved.shortfall,2)
    assert.equal((await read(`/stock/transactions?itemId=${item.id}`)).length,2);pass({quantity:3.125,shortfall:2,historyEntries:2,confirmation:true})
    scenario='Deactivate through UI; inactive movements denied and history retained'
    await m.reload();await card().getByRole('button',{name:'ปิดใช้งาน',exact:true}).click()
    await m.getByRole('dialog').waitFor();await shot(m,'stock-deactivate-confirm-1280.png')
    await save(m,'PUT',`/stock/items/${item.id}/active`,m.getByRole('dialog').getByRole('button',{name:'ยืนยัน',exact:true}))
    await s.reload();await row().waitFor()
    assert(await row().getByRole('button',{name:'รับเข้า',exact:true}).isDisabled());assert(await row().getByRole('button',{name:'ปรับยอด',exact:true}).isDisabled())
    const frozen=await read('/stock'), history=await read(`/stock/transactions?itemId=${item.id}`)
    for(const [endpoint,data] of [[`/stock/${item.id}/in`,{quantity:1,reason:run}],[`/stock/${item.id}/adjustments`,{quantityDelta:1,reason:run}]]) assert.equal((await request('supervisor','post',endpoint,data)).status(),409)
    assert.deepEqual(await read('/stock'),frozen);assert.deepEqual(await read(`/stock/transactions?itemId=${item.id}`),history)
    await shot(s,'stock-inactive-history-1280.png');pass({httpStatuses:[409,409],movementButtonsDisabled:true,unchanged:true,historyRetained:true})
    scenario='Stock activation permissions and reactivation'
    for(const role of ['supervisor','staff','kitchen','anonymous']) assert.equal((await request(role,'put',`/stock/items/${item.id}/active`,{active:true})).status(),role==='anonymous'?401:403)
    await card().getByRole('button',{name:'เปิดใช้งาน',exact:true}).click()
    await save(m,'PUT',`/stock/items/${item.id}/active`,m.getByRole('dialog').getByRole('button',{name:'ยืนยัน',exact:true}))
    await s.reload();assert.equal(await row().getByRole('button',{name:'รับเข้า',exact:true}).isDisabled(),false)
    const inAfter=await request('supervisor','post',`/stock/${item.id}/in`,{quantity:3,reason:run+' reactivate'});assert.equal(inAfter.status(),200)
    const restored=(await read('/stock')).find(x=>x.id===item.id);assert.equal(restored.shortfall,0);assert.equal(restored.quantity,6.125)
    assert.equal((await read(`/stock/transactions?itemId=${item.id}`)).length,3);pass({roleDenials:[403,403,403,401],reactivated:true,quantity:6.125,shortfall:0})
    scenario='Manager creates and edits Profile through UI'
    await m.goto(web+'/admin/users')
    const create=m.locator('form').filter({has:m.getByRole('button',{name:'สร้างบัญชี',exact:true})})
    for(const [label,value] of Object.entries({'ชื่อที่แสดง':run+' user','ชื่อ':'ทดสอบ','นามสกุล':'โปรไฟล์','โทรศัพท์':'extension-12','ชื่อผู้ใช้':run.toLowerCase(),'รหัสผ่านเริ่มต้น':crypto.randomBytes(16).toString('hex')+'Aa1!'})) await create.getByLabel(label,{exact:true}).fill(value)
    const posted=m.waitForResponse(r=>new URL(r.url()).pathname.endsWith('/admin/users')&&r.request().method()==='POST')
    await create.getByRole('button',{name:'สร้างบัญชี',exact:true}).click();const created=await posted;assert.equal(created.status(),201);let user=await created.json()
    const userRow=()=>m.getByRole('row').filter({has:m.getByText(user.username,{exact:true})})
    await userRow().waitFor();await userRow().getByRole('button',{name:'แก้ไข',exact:true}).click()
    const edit=m.locator('form').filter({has:m.getByRole('button',{name:'บันทึกโปรไฟล์',exact:true})})
    await edit.getByLabel('ชื่อ',{exact:true}).fill('แก้ไข');await edit.getByLabel('นามสกุล',{exact:true}).fill('ข้อมูล');await edit.getByLabel('โทรศัพท์',{exact:true}).fill('')
    const blankValid=await edit.getByLabel('โทรศัพท์',{exact:true}).evaluate(e=>e.checkValidity());assert(blankValid)
    const updated=m.waitForResponse(r=>new URL(r.url()).pathname.endsWith(`/admin/users/${user.id}/profile`)&&r.request().method()==='PUT')
    await edit.getByRole('button',{name:'บันทึกโปรไฟล์',exact:true}).click();assert.equal((await updated).status(),200)
    await m.reload();await userRow().getByText('แก้ไข ข้อมูล',{exact:true}).waitFor()
    const stored=(await read('/admin/users')).find(x=>x.id===user.id)
    assert.equal(stored.firstName,'แก้ไข');assert.equal(stored.lastName,'ข้อมูล');assert.equal(stored.phoneNumber,null);assert.equal(stored.email,null)
    assert.equal(stored.displayName,user.displayName);assert.equal(stored.username,user.username);assert.equal(stored.role,user.role)
    await shot(m,'profile-edited-1280.png');pass({userId:user.id,createAndUpdateThroughUI:true,optionalEmailAndPhone:true,legacyIdentityFieldsPreserved:true,phonePolicy:'max length only'})
    scenario='Profile required/length and role denials preserve saved data'
    const profileEndpoint=`/admin/users/${user.id}/profile`, profile={firstName:'แก้ไข',lastName:'ข้อมูล',phoneNumber:null}
    for(const bad of [{...profile,firstName:''},{...profile,lastName:''},{...profile,firstName:'x'.repeat(101)},{...profile,phoneNumber:'x'.repeat(21)}]) assert.equal((await request('manager','put',profileEndpoint,bad)).status(),400)
    for(const role of ['supervisor','staff','kitchen','anonymous']) assert.equal((await request(role,'put',profileEndpoint,profile)).status(),role==='anonymous'?401:403)
    assert.deepEqual((await read('/admin/users')).find(x=>x.id===user.id),stored)
    await userRow().getByRole('button',{name:'แก้ไข',exact:true}).click()
    assert.equal(await edit.getByLabel('ชื่อ',{exact:true}).getAttribute('maxlength'),'100');assert.equal(await edit.getByLabel('โทรศัพท์',{exact:true}).getAttribute('maxlength'),'20')
    await edit.getByLabel('ชื่อ',{exact:true}).fill('');assert.equal(await edit.getByLabel('ชื่อ',{exact:true}).evaluate(e=>e.checkValidity()),false)
    await edit.getByRole('button',{name:'ยกเลิก',exact:true}).click();pass({validationDenials:4,roleDenials:[403,403,403,401],unchanged:true,browserRequiredAndMaxLength:true})
    scenario='Stock/Profile responsive real data at 360/768/1280'
    for(const width of [360,768,1280]) {
      await m.setViewportSize({width,height:1000})
      await m.goto(web+'/admin/users');await userRow().waitFor();await shot(m,`profile-${width}.png`)
      await m.goto(web+'/admin/stock');await m.getByText(item.name,{exact:true}).first().waitFor();await shot(m,`stock-${width}.png`)
    }
    pass({widths:[360,768,1280],documentOverflow:false,httpMocks:false})
  } catch(error) {
    results.push({name:scenario,result:'FAIL',detail:error.message});process.exitCode=1;console.error(`FAIL ${scenario}: ${error.message}`)
  } finally {
    await browser.close()
    await fs.writeFile(path.join(output,'results.json'),JSON.stringify({sourceCommit,runnerSha256,environment:mode,webUrl:web,apiUrl:api,startedAt,finishedAt:new Date().toISOString(),httpMocks:false,publicAcceptance:false,
      counts:{passed:results.filter(x=>x.result==='PASS').length,failed:results.filter(x=>x.result==='FAIL').length},results},null,2)+'\n')
  }
}
main().catch(error=>{console.error(error.message);process.exitCode=1})

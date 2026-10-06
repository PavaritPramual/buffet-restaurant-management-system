// Real HTTP/cookies only. Creates run-specific test data; never modifies existing IDs.
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'playwright')
const assert = require('node:assert/strict')
const fs = require('node:fs/promises')
const path = require('node:path')
const { execFileSync } = require('node:child_process')
const web = process.env.FINAL_WEB_URL || 'http://localhost:5175'
const api = process.env.FINAL_API_URL || 'http://localhost:8085/api/v1'
const mode = process.env.FINAL_ENVIRONMENT
assert(['local-h2', 'local-postgres', 'public'].includes(mode), 'Set FINAL_ENVIRONMENT explicitly')
for (const address of [web, api]) {
  const url = new URL(address)
  assert(!url.username && !url.password && !url.search && !url.hash, 'URLs must not contain credentials/query/fragment')
  if (mode === 'public') assert.equal(url.protocol, 'https:', 'Public acceptance requires HTTPS')
  else assert(['localhost', '127.0.0.1'].includes(url.hostname), 'Local runs require loopback URLs')
}
assert.equal(process.env.FINAL_ALLOW_TEST_DATA, 'true', 'Explicit approved test-data scope required')
const required = name => { assert(process.env[name], `Missing ${name}`); return process.env[name] }
const roles = { manager: 'MANAGER', supervisor: 'SUPERVISOR', staff: 'SERVICE_STAFF', kitchen: 'KITCHEN_STAFF' }
const credentials = Object.fromEntries(Object.keys(roles).map(name => {
  const key = name.toUpperCase()
  return [name, { username: required(`FINAL_${key}_USERNAME`), password: required(`FINAL_${key}_PASSWORD`) }]
}))
const releaseCommit = mode === 'public' ? required('FINAL_DEPLOYED_COMMIT') : null
const output = path.resolve(process.env.FINAL_EVIDENCE_DIR || 'test/reports/step3-core-flow')
const sourceCommit = execFileSync('git', ['rev-parse', 'HEAD'], { encoding: 'utf8' }).trim()
const sourceTree = execFileSync('git', ['status', '--porcelain'], { encoding: 'utf8' }).trim() ? 'dirty working tree; pair with source diff' : 'clean'
const startedAt = new Date().toISOString()
const results = [], ctx = {}, run = `F3${Date.now().toString().slice(-9)}`
let scenario = 'startup'
const pass = (name, detail) => { results.push({ name, result: 'PASS', detail }); console.log(`PASS ${name}`) }

async function main() {
  await fs.mkdir(output, { recursive: true })
  const browser = await chromium.launch({ channel: process.env.FINAL_BROWSER_CHANNEL || 'chrome', headless: true })
  try {
    for (const [name, width] of Object.entries({ manager:1280, supervisor:1280, staff:768, kitchen:768, phoneA:360, phoneB:360, anonymous:360 })) {
      ctx[name] = await browser.newContext({ viewport: { width, height: 1000 } })
      ctx[name].page = await ctx[name].newPage()
    }
    const request = async (name, method, endpoint, data) => {
      const r = await ctx[name].request[method](api + endpoint, { ...(data === undefined ? {} : { data }), headers: { Origin: new URL(web).origin } })
      assert(r.ok(), `${method} ${endpoint}: HTTP ${r.status()}`)
      return r.status() === 204 ? null : r.json()
    }
    const shot = async (name, file) => {
      const page = ctx[name].page
      assert.equal(await page.evaluate(() => document.documentElement.scrollWidth), page.viewportSize().width, `${name} overflow`)
      await page.screenshot({ path: path.join(output, file), fullPage: true, mask: [page.locator('.staff-session-qr-card')] })
    }
    const confirm = async page => page.getByRole('dialog').getByRole('button', { name:'ยืนยัน', exact:true }).click()
    scenario = 'real staff logins and role matrix'
    const invalidLogin = ctx.anonymous.page
    await invalidLogin.goto(web + '/admin')
    await invalidLogin.getByLabel('ชื่อผู้ใช้', { exact:true }).fill(run + '-unknown')
    await invalidLogin.getByLabel('รหัสผ่าน', { exact:true }).fill(run + '-invalid')
    const invalidResponse = invalidLogin.waitForResponse(r => r.url().endsWith('/auth/login') && r.request().method() === 'POST')
    await invalidLogin.getByRole('button', { name:'เข้าสู่ระบบ', exact:true }).click()
    assert.equal((await invalidResponse).status(),401)
    await invalidLogin.getByRole('alert').waitFor()
    assert.equal((await ctx.anonymous.request.get(api + '/auth/me')).status(),401)
    for (const [name, role] of Object.entries(roles)) {
      const page = ctx[name].page
      await page.goto(web + '/admin')
      await page.getByLabel('ชื่อผู้ใช้', { exact:true }).fill(credentials[name].username)
      await page.getByLabel('รหัสผ่าน', { exact:true }).fill(credentials[name].password)
      await page.getByRole('button', { name:'เข้าสู่ระบบ', exact:true }).click()
      await page.waitForURL(`**${name === 'staff' ? '/staff/tables' : name === 'kitchen' ? '/kitchen' : '/admin/stock'}`)
      assert.equal((await request(name, 'get', '/auth/me')).role, role)
    }
    for (const [name, incoming, ready, stock] of [['manager',403,403,200],['supervisor',403,403,200],['staff',403,200,403],['kitchen',200,403,403],['anonymous',401,401,401]]) {
      for (const [endpoint, expected] of [['/orders/incoming',incoming],['/orders/ready',ready],['/stock',stock]]) {
        assert.equal((await ctx[name].request.get(api + endpoint, { headers:{'X-User-Role':'MANAGER'} })).status(), expected)
      }
    }
    pass(scenario, 'Four independent cookie contexts; invalid login, anonymous and spoofed-header denial')
    scenario = 'direct URL case and trailing slash guards'
    for (const name of ['supervisor','staff','kitchen']) {
      await ctx[name].page.goto(web + '/ADMIN/MENU/')
      await ctx[name].page.waitForURL(`**${name === 'staff' ? '/staff/tables' : name === 'kitchen' ? '/kitchen' : '/admin/stock'}`)
    }
    await ctx.staff.page.goto(web + '/KITCHEN/')
    await ctx.staff.page.waitForURL('**/staff/tables')
    await ctx.kitchen.page.goto(web + '/KITCHEN/')
    await ctx.kitchen.page.getByRole('heading', { name:'ออเดอร์ที่รอดำเนินการ' }).waitFor()
    await shot('kitchen','kitchen-768.png')
    pass(scenario, 'Actual protected routes with real roles')
    scenario = 'Manager master data UI'
    const manager = ctx.manager.page
    async function createMaster(route, endpoint, fields) {
      await manager.goto(web + route)
      await manager.getByRole('heading', { name:'เพิ่มรายการ', exact:true }).waitFor()
      for (const [label,value] of Object.entries(fields)) await manager.getByLabel(label, { exact:true }).fill(String(value))
      const pending = manager.waitForResponse(r => r.url().endsWith(endpoint) && r.request().method() === 'POST')
      await manager.getByRole('button', { name:'บันทึก', exact:true }).click()
      const r = await pending; assert.equal(r.status(),201)
      await manager.getByText('บันทึกข้อมูลแล้ว', { exact:true }).waitFor()
      return r.json()
    }
    const pack = await createMaster('/admin/packages','/buffet-packages', { 'ชื่อรายการ':run, 'ราคา':399, 'รายละเอียด':'Final regression test data' })
    const soup = await createMaster('/admin/soups','/soups', { 'ชื่อรายการ':run })
    const table = await createMaster('/admin/tables','/tables', { 'หมายเลขโต๊ะ':run, 'ความจุ':4 })
    await shot('manager','manager-tables-1280.png')
    const category = await request('manager','post','/menu-categories', {name:run})
    const item = await request('manager','post','/menu-items', {categoryId:category.id,name:run+' หมู',available:true,packageIds:[pack.id]})
    pass(scenario, {tableId:table.id,packageId:pack.id,menuSetup:'Existing Manager domain API; not evidence of Menu CRUD UI'})
    scenario = 'open actual session and reject unpaid close'
    const staff = ctx.staff.page, kitchen = ctx.kitchen.page, a = ctx.phoneA.page, b = ctx.phoneB.page
    await staff.reload()
    await staff.locator('.staff-table-card').filter({hasText:run}).getByRole('button',{name:'เปิดโต๊ะ',exact:true}).click()
    await staff.getByLabel('จำนวนผู้ใหญ่').fill('2'); await staff.getByLabel('จำนวนเด็ก').fill('1')
    await staff.getByRole('combobox',{name:/^แพ็กเกจ/}).selectOption(String(pack.id))
    await staff.getByRole('combobox',{name:/^น้ำซุป/}).selectOption(String(soup.id))
    await staff.getByRole('button',{name:'ยืนยันเปิดโต๊ะ'}).click()
    await staff.waitForURL(/\/staff\/sessions\/\d+$/)
    const sessionId = Number(staff.url().split('/').pop())
    const qr = async () => { const link = await staff.locator('a[href*="/customer/qr#token="]').getAttribute('href'); assert(link); return link }
    const firstQr = await qr()
    await staff.getByRole('button',{name:'ปิดรอบกิน',exact:true}).click(); await confirm(staff)
    await staff.getByRole('alert').waitFor()
    assert.equal((await request('staff','get',`/dining-sessions/${sessionId}`)).sessionStatus,'ACTIVE')
    await shot('staff','staff-unpaid-close-768.png')
    pass(scenario,{sessionId,tableId:table.id})
    scenario = 'one-use QR and two independent phones'
    await a.goto(firstQr)
    await a.getByRole('button',{name:'เพิ่ม '+item.name,exact:true}).waitFor()
    assert.equal(new URL(a.url()).hash,'')
    const reuse = await ctx.anonymous.request.post(api+'/dining-sessions/qr-exchange',{data:{token:new URL(firstQr).hash.slice(7)},headers:{Origin:new URL(web).origin}})
    assert.equal(reuse.status(),404)
    await staff.reload()
    await staff.locator('a[href*="/customer/qr#token="]').waitFor()
    await b.goto(await qr())
    await b.getByRole('button',{name:'เพิ่ม '+item.name,exact:true}).waitFor()
    assert.equal((await request('phoneB','get','/dining-sessions/customer-context')).sessionId,sessionId)
    assert.equal((await ctx.anonymous.request.get(api+`/dining-sessions/${sessionId}/orders`)).status(),401)
    pass(scenario,{sessionId,phones:2,qrReuseStatus:404})
    scenario = 'same-tab fresh QR resets cart and reload restores cookie without re-exchange'
    const timeOrigin = await a.evaluate(() => performance.timeOrigin)
    await a.getByRole('button',{name:'เพิ่ม '+item.name,exact:true}).click()
    await a.getByRole('button',{name:'ยืนยันการสั่ง',exact:true}).click()
    await a.getByRole('dialog').waitFor()
    await staff.reload()
    await staff.locator('a[href*="/customer/qr#token="]').waitFor()
    const replacementQr = await qr()
    assert.notEqual(replacementQr,firstQr)
    let rescanExchanges = 0
    a.on('request',r=>{if(r.method()==='POST' && r.url().endsWith('/dining-sessions/qr-exchange')) rescanExchanges++})
    const exchanged = a.waitForResponse(r=>r.url().endsWith('/dining-sessions/qr-exchange') && r.request().method()==='POST')
    await a.goto(replacementQr)
    assert.equal((await exchanged).status(),200)
    await a.getByRole('button',{name:'เพิ่ม '+item.name,exact:true}).waitFor()
    assert.equal(await a.evaluate(() => performance.timeOrigin),timeOrigin,'Rescan must be same-document navigation')
    assert.equal(new URL(a.url()).hash,'')
    assert.equal(await a.getByRole('dialog').count(),0)
    assert(await a.getByRole('button',{name:'ยืนยันการสั่ง',exact:true}).isDisabled())
    assert.equal(rescanExchanges,1,'StrictMode must not redeem a one-use QR twice')
    await a.reload()
    await a.getByRole('button',{name:'เพิ่ม '+item.name,exact:true}).waitFor()
    assert.equal(rescanExchanges,1,'Reload must restore the cookie rather than re-exchange the used token')
    assert.equal((await request('phoneA','get','/dining-sessions/customer-context')).sessionId,sessionId)
    assert.equal((await request('phoneB','get','/dining-sessions/customer-context')).sessionId,sessionId)
    await shot('phoneA','customer-rescan-restored-360.png')
    pass(scenario,{sessionId,sameDocument:true,rescanExchanges,cartReset:true,phones:2})
    scenario = 'single confirmed order and actual Kitchen Serving transitions'
    let orderPosts=0
    a.on('request',r=>{if(r.method()==='POST' && /\/orders$/.test(r.url())) orderPosts++})
    await a.getByRole('button',{name:'เพิ่ม '+item.name,exact:true}).click()
    await a.getByRole('button',{name:'ยืนยันการสั่ง',exact:true}).click()
    await shot('phoneA','customer-confirm-360.png')
    await confirm(a); await a.locator('.order-card').waitFor(); assert.equal(orderPosts,1)
    await shot('phoneA','customer-order-360.png')
    const orderId=(await request('phoneA','get',`/dining-sessions/${sessionId}/orders`))[0].orderId
    await kitchen.reload()
    let ticket=kitchen.locator('.order-board-card').filter({hasText:run})
    await ticket.getByRole('button',{name:'เริ่มเตรียมอาหาร'}).click()
    await ticket.getByRole('button',{name:'พร้อมเสิร์ฟแล้ว'}).waitFor(); await shot('kitchen','kitchen-preparing-768.png')
    await ticket.getByRole('button',{name:'พร้อมเสิร์ฟแล้ว'}).click(); await ticket.waitFor({state:'hidden'})
    await staff.getByRole('link',{name:'งานเสิร์ฟ'}).click()
    ticket=staff.locator('.order-board-card').filter({hasText:run})
    await ticket.getByRole('button',{name:'เสิร์ฟแล้ว'}).waitFor(); await shot('staff','staff-serving-768.png')
    await ticket.getByRole('button',{name:'เสิร์ฟแล้ว'}).click(); await ticket.waitFor({state:'hidden'})
    assert.equal((await request('phoneA','get',`/dining-sessions/${sessionId}/orders`))[0].status,'SERVED')
    pass(scenario,{orderId,status:'SERVED',orderPosts})
    scenario = 'bill request stops both phones and backend orders'
    await a.getByRole('button',{name:'ขอคิดบิล',exact:true}).click(); await confirm(a)
    for(const [name,page] of [['phoneA',a],['phoneB',b]]) {
      await page.getByText('ขอคิดบิลแล้ว · รอพนักงานรับชำระ',{exact:true}).waitFor({timeout:15000})
      assert(await page.getByRole('button',{name:'เพิ่ม '+item.name,exact:true}).isDisabled())
      assert.equal((await ctx[name].request.post(api+`/dining-sessions/${sessionId}/orders`,{data:{items:[{menuItemId:item.id,quantity:1}]},headers:{Origin:new URL(web).origin}})).status(),409)
    }
    await shot('phoneB','customer-second-phone-requested-360.png')
    pass(scenario,{sessionId,phones:2,rejectedOrderStatus:409})
    scenario = 'payment recorded; both customers show total due and paid'
    await staff.goto(web+`/staff/sessions/${sessionId}`)
    await staff.getByRole('button',{name:'ดูบิล / รับชำระ'}).click()
    await staff.getByRole('button',{name:'ดูบิล',exact:true}).click()
    await staff.getByRole('button',{name:'บันทึกการชำระ',exact:true}).click(); await confirm(staff)
    await staff.getByText('ชำระแล้ว',{exact:true}).waitFor()
    const payment=await request('staff','get',`/payments/sessions/${sessionId}`)
    assert.equal(payment.amount,997.5)
    assert.equal((await request('staff','get',`/dining-sessions/${sessionId}`)).sessionStatus,'ACTIVE')
    for(const [name,page] of [['phoneA',a],['phoneB',b]]) {
      await page.getByText('ชำระแล้ว · รอพนักงานปิดรอบกิน',{exact:true}).waitFor({timeout:15000})
      const bill=await request(name,'get',`/dining-sessions/${sessionId}/bill-status`)
      assert.equal(bill.bill.totalAmount,payment.amount); assert.equal(bill.dueAmount,0); assert.equal(bill.paidAmount,payment.amount)
    }
    await shot('phoneA','customer-paid-360.png'); await shot('staff','staff-paid-768.png')
    pass(scenario,{sessionId,amount:payment.amount,dueAmount:0})
    scenario = 'explicit close returns AVAILABLE and revokes both phones'
    await staff.getByRole('button',{name:'กลับไปรายละเอียดรอบกิน'}).click()
    await staff.getByRole('button',{name:'ปิดรอบกิน',exact:true}).click(); await confirm(staff)
    await staff.waitForURL('**/staff/tables')
    assert.equal((await request('staff','get',`/tables/${table.id}`)).status,'AVAILABLE')
    for(const name of ['phoneA','phoneB']) assert([401,404].includes((await ctx[name].request.get(api+`/dining-sessions/${sessionId}/orders`)).status()))
    pass(scenario,{sessionId,tableStatus:'AVAILABLE',phonesRevoked:2})
    scenario = 'employee expiry and logout hide protected UI'
    // Invalidate the same real cookie from another client in its context, then request via UI.
    await request('manager','post','/auth/logout')
    await manager.getByRole('link',{name:'สต็อก',exact:true}).click()
    await manager.getByRole('heading',{name:'เข้าสู่ระบบ',exact:true}).waitFor()
    await shot('manager','manager-expired-1280.png')
    assert.equal((await ctx.manager.request.get(api+'/stock')).status(),401)
    await request('supervisor','post','/auth/logout')
    await ctx.supervisor.page.getByRole('button',{name:'โหลดข้อมูลใหม่',exact:true}).click()
    await ctx.supervisor.page.getByRole('heading',{name:'เข้าสู่ระบบ',exact:true}).waitFor()
    assert.equal((await ctx.supervisor.request.get(api+'/stock')).status(),401)
    await staff.getByRole('button',{name:'ออกจากระบบ',exact:true}).click()
    await staff.getByRole('heading',{name:'เข้าสู่ระบบ',exact:true}).waitFor()
    assert.equal((await ctx.staff.request.get(api+'/orders/ready')).status(),401)
    await kitchen.getByRole('button',{name:'ออกจากระบบ'}).click()
    await kitchen.getByRole('heading',{name:'เข้าสู่ระบบ',exact:true}).waitFor()
    assert.equal((await ctx.kitchen.request.get(api+'/orders/incoming')).status(),401)
    pass(scenario,'Real server logout -> UI API401 for Manager/Supervisor; Staff/Kitchen UI logout revokes access')
    scenario = 'Swagger customer bill contract'
    const specResponse=await ctx.anonymous.request.get(new URL('/v3/api-docs',api).href)
    assert(specResponse.ok())
    const spec=await specResponse.json()
    assert(spec.paths['/api/v1/dining-sessions/{sessionId}/bill-request'].post)
    assert(spec.paths['/api/v1/dining-sessions/{sessionId}/bill-status'].get)
    pass(scenario,'HTTP200 and bill paths present')
  } catch(error) {
    results.push({name:scenario,result:'FAIL',detail:'See local runner error; raw request/cookie data excluded'})
    throw error
  } finally {
    await fs.writeFile(path.join(output,'results.json'),JSON.stringify({startedAt,finishedAt:new Date().toISOString(),sourceCommit,sourceTree,
      environment:mode,webUrl:web,apiUrl:api,deployedCommit:releaseCommit,deployedCommitSource:releaseCommit?'runtime owner supplied; not independently attested':null,
      browser:browser.version(),httpMocks:false,counts:{passed:results.filter(r=>r.result==='PASS').length,failed:results.filter(r=>r.result==='FAIL').length},
      pending:['Stock target/active and Profile owner implementation/UI review','Public release schema/provider confirmation and full release rerun'],results},null,2))
    await browser.close()
  }
}
main().catch(() => { console.error(`FAIL ${scenario}; inspect local runtime without publishing credentials`); process.exitCode=1 })

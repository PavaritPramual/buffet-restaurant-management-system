// Disposable PostgreSQL demo only. Never run against the shared Supabase runtime.
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'playwright')
const assert = require('node:assert/strict')
const fs = require('node:fs/promises')
const path = require('node:path')
const web = 'http://localhost:5173', api = 'http://localhost:8080/api/v1'
assert.equal(process.env.STEP2_DISPOSABLE_DEMO, 'true', 'Explicit isolated-demo opt-in required')
const output = path.resolve(process.env.STEP2_EVIDENCE_DIR || 'test/reports/step2-core-flow')
const results = []
const pass = (name, detail) => { results.push({ name, result: 'PASS', detail }); console.log(`PASS ${name}`) }
const run = `S2${Date.now().toString().slice(-8)}`
const samplePassword = 'isolated-demo-only'

async function main() {
  await fs.mkdir(output, { recursive: true })
  const browser = await chromium.launch({ channel: 'chrome', headless: true })
  const ctx = {}
  try {
    for (const [name, width] of Object.entries({ admin: 1280, supervisor: 1280, staff: 768, kitchen: 768, customer: 360 })) {
      ctx[name] = await browser.newContext({ viewport: { width, height: 1000 } })
      ctx[name].page = await ctx[name].newPage()
    }
    const post = async (context, endpoint, data) => {
      const r = await context.request.post(api + endpoint, { data, headers: { Origin: web } })
      assert(r.ok(), `${endpoint}: HTTP ${r.status()}`)
      return r.json()
    }
    const get = async (context, endpoint) => {
      const r = await context.request.get(api + endpoint)
      assert(r.ok(), `${endpoint}: HTTP ${r.status()}`)
      return r.json()
    }
    const login = async (context, username, password, expectedPath) => {
      const p = context.page
      await p.goto(web + '/admin')
      await p.getByLabel('ชื่อผู้ใช้', { exact: true }).fill(username)
      await p.getByLabel('รหัสผ่าน', { exact: true }).fill(password)
      await p.getByRole('button', { name: 'เข้าสู่ระบบ', exact: true }).click()
      await p.waitForURL(`**${expectedPath}`)
    }
    const shot = async (context, file) => {
      await context.page.screenshot({ path: path.join(output, file), fullPage: true,
        mask: [context.page.locator('.staff-session-qr-card')] })
    }
    await login(ctx.admin, 'admin', 'admin123', '/admin/stock')
    const admin = ctx.admin.page
    await admin.goto(web + '/admin/users')
    for (const [name, role] of Object.entries({ staff: 'SERVICE_STAFF', kitchen: 'KITCHEN_STAFF', supervisor: 'SUPERVISOR' })) {
      await admin.getByLabel('ชื่อที่แสดง', { exact: true }).fill(`${run} ${name}`)
      await admin.getByLabel('ชื่อผู้ใช้', { exact: true }).fill(`${run}-${name}`)
      await admin.getByLabel('รหัสผ่านเริ่มต้น').fill(samplePassword)
      await admin.getByRole('combobox', { name: /^บทบาท/ }).selectOption(role)
      const saved = admin.waitForResponse(r => r.url().endsWith('/admin/users') && r.request().method() === 'POST')
      await admin.getByRole('button', { name: 'สร้างบัญชี', exact: true }).click()
      assert.equal((await saved).status(), 201)
      await admin.getByRole('row').filter({ hasText: `${run}-${name}` }).waitFor()
    }
    await shot(ctx.admin, 'admin-users-1280.png')
    pass('Create staff accounts through Manager UI', 'SERVICE_STAFF/KITCHEN_STAFF/SUPERVISOR')
    await login(ctx.staff, `${run}-staff`, samplePassword, '/staff/tables')
    await login(ctx.kitchen, `${run}-kitchen`, samplePassword, '/kitchen')
    await login(ctx.supervisor, `${run}-supervisor`, samplePassword, '/admin/stock')
    for (const [name, incoming, ready] of [['admin',403,403], ['supervisor',403,403], ['staff',403,200], ['kitchen',200,403], ['customer',401,401]]) {
      for (const [endpoint, expected] of [['incoming',incoming], ['ready',ready]]) {
        const response = await ctx[name].request.get(`${api}/orders/${endpoint}`, { headers: { 'X-User-Role': 'MANAGER' } })
        assert.equal(response.status(), expected)
      }
    }
    pass('Real cookie role matrix and spoofed headers', 'All four staff roles plus anonymous')
    for (const [name, expected] of [['admin',200], ['supervisor',200], ['staff',200], ['kitchen',403], ['customer',401]]) {
      for (const endpoint of ['/tables', '/buffet-packages', '/soups']) {
        assert.equal((await ctx[name].request.get(api + endpoint, { headers: { 'X-User-Role': 'MANAGER' } })).status(), expected)
      }
      if (name !== 'admin') {
        assert.equal((await ctx[name].request.delete(api + '/tables/999999999', { headers: { Origin: web, 'X-User-Role': 'MANAGER' } })).status(), name === 'customer' ? 401 : 403)
      }
    }
    pass('Master data guards reject anonymous and spoofed mutation privileges', 'Real cookie checks for Table/Package/Soup')
    await ctx.staff.page.goto(web + '/KITCHEN/')
    await ctx.staff.page.waitForURL('**/staff/tables')
    await ctx.kitchen.page.goto(web + '/KITCHEN/')
    await ctx.kitchen.page.getByRole('heading', { name: 'ออเดอร์ที่รอดำเนินการ' }).waitFor()
    await shot(ctx.kitchen, 'kitchen-empty-768.png')
    pass('Case and trailing slash guards', 'Staff redirected; Kitchen allowed')

    async function createMaster(route, endpoint, fields) {
      await admin.goto(web + route)
      await admin.getByRole('heading', { name: 'เพิ่มรายการ', exact: true }).waitFor()
      for (const [label,value] of Object.entries(fields)) await admin.getByLabel(label, { exact: true }).fill(String(value))
      const saved=admin.waitForResponse(r => r.url().endsWith(endpoint) && r.request().method()==='POST')
      await admin.getByRole('button', { name: 'บันทึก', exact: true }).click()
      const response=await saved; assert.equal(response.status(),201)
      const created=await response.json()
      await admin.getByText('บันทึกข้อมูลแล้ว', { exact: true }).waitFor()
      assert.equal(await admin.evaluate(() => document.documentElement.scrollWidth),1280)
      return created
    }
    const pack = await createMaster('/admin/packages','/buffet-packages', { 'ชื่อรายการ':run, 'ราคา':399, 'รายละเอียด':'Disposable Step 2 evidence' })
    await shot(ctx.admin,'manager-packages-1280.png')
    const soup = await createMaster('/admin/soups','/soups', { 'ชื่อรายการ':run })
    await shot(ctx.admin,'manager-soups-1280.png')
    const table = await createMaster('/admin/tables','/tables', { 'หมายเลขโต๊ะ':run, 'ความจุ':4 })
    await shot(ctx.admin,'manager-tables-1280.png')
    const managedStock = await createMaster('/admin/stock-items','/stock/items', { 'รหัสสต็อก':run, 'ชื่อรายการ':run+' เนื้อ', 'หน่วย':'kg', 'ยอดแจ้งเตือนต่ำ':2 })
    assert.equal(managedStock.quantity,0)
    await shot(ctx.admin,'manager-stock-items-1280.png')
    pass('Manager creates table/package/soup/stock through real UI', { tableId:table.id, packageId:pack.id, soupId:soup.id, stockItemId:managedStock.id })
    await admin.goto(web + '/admin/menu')
    const category = await post(ctx.admin, '/menu-categories', { name: run })
    const item = await post(ctx.admin, '/menu-items', { categoryId: category.id, name: `${run} หมู`, description: 'ทดสอบระบบรวม', available: true, packageIds: [pack.id] })
    const staff = ctx.staff.page, kitchen = ctx.kitchen.page, customer = ctx.customer.page
    await staff.reload()
    await staff.locator('.staff-table-card').filter({ hasText: run }).getByRole('button', { name: 'เปิดโต๊ะ', exact: true }).click()
    await staff.getByLabel('จำนวนผู้ใหญ่').fill('2')
    await staff.getByLabel('จำนวนเด็ก').fill('1')
    await staff.getByRole('combobox', { name: /^แพ็กเกจ/ }).selectOption(String(pack.id))
    await staff.getByRole('combobox', { name: /^น้ำซุป/ }).selectOption(String(soup.id))
    await staff.getByRole('button', { name: 'ยืนยันเปิดโต๊ะ' }).click()
    await staff.waitForURL(/\/staff\/sessions\/\d+$/)
    const sessionId = Number(staff.url().split('/').pop())
    const qrLink = await staff.locator('a[href*="/customer/qr#token="]').getAttribute('href')
    assert(qrLink)
    await shot(ctx.staff, 'staff-session-768.png')
    await staff.getByRole('button', { name: 'ปิดรอบกิน', exact: true }).click()
    await staff.getByRole('dialog').getByRole('button', { name: 'ยืนยัน', exact: true }).click()
    await staff.getByRole('alert').waitFor()
    assert.equal((await get(ctx.staff, `/dining-sessions/${sessionId}`)).sessionStatus, 'ACTIVE')
    await shot(ctx.staff, 'staff-unpaid-error-768.png')
    pass('Open via Staff UI; unpaid close rejected', { sessionId, tableId: table.id })

    let orderPosts = 0
    customer.on('request', r => { if (r.method() === 'POST' && /\/orders$/.test(r.url())) orderPosts++ })
    await customer.goto(qrLink)
    await customer.getByRole('button', { name: `เพิ่ม ${item.name}`, exact: true }).waitFor()
    assert.equal(new URL(customer.url()).hash, '')
    await customer.getByRole('button', { name: `เพิ่ม ${item.name}`, exact: true }).click()
    await customer.getByRole('button', { name: 'ยืนยันการสั่ง', exact: true }).click()
    await customer.getByRole('dialog').getByRole('button', { name: 'ยืนยัน', exact: true }).click()
    await customer.locator('.order-card').waitFor()
    assert.equal(orderPosts, 1)
    assert.equal(await customer.evaluate(() => document.documentElement.scrollWidth), 360)
    await shot(ctx.customer, 'customer-order-360.png')
    const orders = await get(ctx.customer, `/dining-sessions/${sessionId}/orders`)
    const orderId = orders[0].orderId
    const reuse = await ctx.customer.request.post(api + '/dining-sessions/qr-exchange', { data: { token: new URL(qrLink).hash.slice(7) }, headers: { Origin: web } })
    assert.equal(reuse.status(), 404)
    pass('Real QR exchange and single order', { sessionId, orderId, qrReuseStatus: 404 })

    await kitchen.reload()
    let ticket = kitchen.locator('.order-board-card').filter({ hasText: run })
    await ticket.getByRole('button', { name: 'เริ่มเตรียมอาหาร' }).click()
    await ticket.getByRole('button', { name: 'พร้อมเสิร์ฟแล้ว' }).waitFor()
    await shot(ctx.kitchen, 'kitchen-preparing-768.png')
    await ticket.getByRole('button', { name: 'พร้อมเสิร์ฟแล้ว' }).click()
    await ticket.waitFor({ state: 'hidden' })
    await staff.getByRole('link', { name: 'งานเสิร์ฟ' }).click()
    ticket = staff.locator('.order-board-card').filter({ hasText: run })
    await ticket.getByRole('button', { name: 'เสิร์ฟแล้ว' }).waitFor()
    await shot(ctx.staff, 'staff-serving-768.png')
    await ticket.getByRole('button', { name: 'เสิร์ฟแล้ว' }).click()
    await ticket.waitFor({ state: 'hidden' })
    assert.equal((await get(ctx.customer, `/dining-sessions/${sessionId}/orders`))[0].status, 'SERVED')
    pass('Kitchen and Serving complete actual persisted order', { orderId, status: 'SERVED' })
    await customer.getByRole('button', { name: 'อัปเดตสถานะคำสั่งซื้อ' }).click()
    await customer.getByText('เสิร์ฟแล้ว', { exact: true }).waitFor()

    await customer.getByRole('button', { name: 'ขอคิดบิล', exact: true }).click()
    await customer.getByRole('dialog').getByRole('button', { name: 'ยืนยัน', exact: true }).click()
    await customer.getByText('ขอคิดบิลแล้ว · รอพนักงานรับชำระ', { exact: true }).waitFor()
    assert.equal(await customer.getByRole('button', { name: `เพิ่ม ${item.name}`, exact:true }).isDisabled(), true)
    const rejected = await ctx.customer.request.post(api + `/dining-sessions/${sessionId}/orders`, { data:{items:[{menuItemId:item.id,quantity:1}]}, headers:{Origin:web} })
    assert.equal(rejected.status(),409)
    await shot(ctx.customer,'customer-bill-requested-360.png')
    await staff.goto(web + '/staff/tables')
    await staff.getByText('ขอคิดบิล', { exact:true }).waitFor()
    await shot(ctx.staff,'staff-bill-request-768.png')
    pass('Customer request bill freezes orders and alerts Staff', { sessionId, rejectedOrderStatus:409 })
    await staff.goto(web + `/staff/sessions/${sessionId}`)
    await staff.getByRole('button', { name: 'ดูบิล / รับชำระ' }).click()
    await staff.getByRole('button', { name: 'ดูบิล', exact: true }).click()
    await staff.getByRole('button', { name: 'บันทึกการชำระ', exact: true }).click()
    await staff.getByRole('dialog').getByRole('button', { name: 'ยืนยัน', exact: true }).click()
    await staff.getByText('ชำระแล้ว', { exact: true }).waitFor()
    const payment = await get(ctx.staff, `/payments/sessions/${sessionId}`)
    assert.equal(payment.amount, 997.5)
    assert.equal((await get(ctx.staff, `/dining-sessions/${sessionId}`)).sessionStatus, 'ACTIVE')
    await staff.reload()
    await staff.getByRole('button', { name: 'ดูบิล', exact: true }).click()
    await staff.getByText('ชำระแล้ว', { exact: true }).waitFor()
    assert.equal((await get(ctx.staff, `/payments/sessions/${sessionId}`)).paymentId, payment.paymentId)
    await customer.getByText('ชำระแล้ว · รอพนักงานปิดรอบกิน', { exact:true }).waitFor({timeout:15000})
    const customerBill=await get(ctx.customer, `/dining-sessions/${sessionId}/bill-status`)
    assert.equal(customerBill.bill.totalAmount,payment.amount)
    await shot(ctx.customer,'customer-bill-paid-360.png')
    pass('Customer sees recorded PAID and matching backend amount', { amount:payment.amount })
    await shot(ctx.staff, 'payment-refresh-768.png')
    await staff.getByRole('button', { name: 'กลับไปรายละเอียดรอบกิน' }).click()
    await staff.getByRole('button', { name: 'ปิดรอบกิน', exact: true }).click()
    await staff.getByRole('dialog').getByRole('button', { name: 'ยืนยัน', exact: true }).click()
    await staff.waitForURL('**/staff/tables')
    assert.equal((await get(ctx.staff, `/tables/${table.id}`)).status, 'AVAILABLE')
    const closed = await ctx.customer.request.get(api + `/dining-sessions/${sessionId}/orders`)
    assert([401,404].includes(closed.status()))
    await shot(ctx.staff, 'table-available-768.png')
    pass('Payment refresh; explicit close returns AVAILABLE and revokes customer', { paymentId: payment.paymentId, amount: payment.amount })

    const supervisor = ctx.supervisor.page
    await supervisor.reload()
    const stock = await get(ctx.supervisor, '/stock')
    assert(stock.length > 0, 'Disposable demo must seed stock before this run')
    const stockItem = stock.find(item => item.id === managedStock.id), row = supervisor.getByRole('row').filter({ hasText: stockItem.sku })
    await row.getByRole('button', { name: 'รับเข้า', exact: true }).click()
    await supervisor.getByLabel('จำนวนที่รับเข้า').fill('2')
    await supervisor.getByLabel('เหตุผล', { exact: true }).fill(`${run} รับเข้า`)
    await supervisor.getByRole('button', { name: 'บันทึกรายการ' }).click()
    await supervisor.getByText(`${run} รับเข้า`, { exact: true }).waitFor()
    await row.getByRole('button', { name: 'ปรับยอด', exact: true }).click()
    await supervisor.getByLabel(`ผลต่างที่ปรับ (${stockItem.unit})`).fill('-1')
    await supervisor.getByLabel('เหตุผล', { exact: true }).fill(`${run} ตรวจนับ`)
    await supervisor.getByRole('button', { name: 'บันทึกรายการ' }).click()
    await supervisor.getByRole('dialog').waitFor()
    await shot(ctx.supervisor, 'stock-confirm-1280.png')
    await supervisor.getByRole('dialog').getByRole('button', { name: 'ยืนยัน', exact: true }).click()
    await supervisor.getByText(`${run} ตรวจนับ`, { exact: true }).waitFor()
    const current = (await get(ctx.supervisor, '/stock')).find(s => s.id === stockItem.id)
    assert.equal(current.quantity, stockItem.quantity + 1)
    await shot(ctx.supervisor, 'stock-history-1280.png')
    pass('Stock UI receiving, confirmed adjustment and audit balance', { stockItemId: stockItem.id, balance: current.quantity })
    await kitchen.getByRole('button', { name: 'ออกจากระบบ' }).click()
    await kitchen.getByRole('button', { name: 'เข้าสู่ระบบ' }).waitFor()
    assert.equal((await ctx.kitchen.request.get(api + '/orders/incoming')).status(), 401)
    pass('Logout invalidates real staff session', 'API 401')
    // Query Swagger at its root rather than the API prefix.
    const swagger = await ctx.admin.request.get('http://localhost:8080/v3/api-docs')
    assert(swagger.ok())
    const spec=await swagger.json()
    assert(spec.paths['/api/v1/dining-sessions/{sessionId}/bill-request'].post)
    assert(spec.paths['/api/v1/dining-sessions/{sessionId}/bill-status'].get)
    assert(spec.paths['/api/v1/stock/items'].post)
    pass('Swagger available', 'HTTP 200')
    await fs.writeFile(path.join(output, 'results.json'), JSON.stringify({ executedAt: new Date().toISOString(), environment: 'Docker Compose + isolated PostgreSQL 18; actual session providers; seed data only', results }, null, 2))
  } finally { await browser.close() }
}
main().catch(e => { console.error(e); process.exitCode = 1 })

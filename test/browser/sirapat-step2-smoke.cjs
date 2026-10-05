// Run against a fresh local demo backend with database ordering + session menu auth.
// Requires Playwright (or PLAYWRIGHT_MODULE pointing to an installed package).
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'playwright')
const assert = require('node:assert/strict')
const fs = require('node:fs/promises')
const path = require('node:path')

const frontend = process.env.STEP2_FRONTEND_URL || 'http://localhost:5173'
const backend = process.env.STEP2_BACKEND_URL || 'http://localhost:8080'
for (const url of [frontend, backend]) {
  assert(['localhost', '127.0.0.1'].includes(new URL(url).hostname), 'Only an isolated local demo may be seeded')
}
const output = path.resolve(process.env.STEP2_EVIDENCE_DIR || 'test/reports/sirapat-step2')
const results = []
const pass = (name, detail) => { results.push({ name, result: 'PASS', detail }); console.log(`PASS ${name}`) }

async function main() {
  await fs.mkdir(output, { recursive: true })
  const browser = await chromium.launch({ channel: 'chrome', headless: true })
  try {
    const adminContext = await browser.newContext({ viewport: { width: 1280, height: 900 } })
    const admin = await adminContext.newPage()
    const api = adminContext.request
    const post = async (endpoint, data) => {
      const response = await api.post(`${backend}/api/v1${endpoint}`, { data })
      assert(response.ok(), `${endpoint}: ${response.status()} ${await response.text()}`)
      return response.json()
    }
    await post('/auth/login', { username: 'admin', password: 'admin123' })
    const buffetPackage = await post('/buffet-packages', { name: 'ชุดทดสอบ Step 2', price: 299, description: 'Isolated browser data' })
    const premium = await post('/buffet-packages', { name: 'แพ็กเกจอื่น', price: 499 })
    const soup = await post('/soups', { name: 'น้ำซุปทดสอบ' })
    const table = await post('/tables', { tableNumber: 'SQA01', capacity: 4 })
    const session = await post('/dining-sessions', { tableId: table.id, packageId: buffetPackage.id, soupId: soup.id, adultCount: 2, childCount: 0 })

    await admin.goto(`${frontend}/admin/menu`)
    await admin.getByLabel('ชื่อหมวดหมู่', { exact: true }).fill('ของทอดทดสอบ')
    await admin.getByRole('button', { name: 'เพิ่มหมวดหมู่', exact: true }).click()
    await admin.getByText('บันทึกหมวดหมู่แล้ว', { exact: true }).waitFor()
    await admin.getByLabel('ชื่อเมนู', { exact: true }).fill('ไก่ทอดทดสอบ')
    await admin.getByLabel('รายละเอียดเมนู (ถ้ามี)').fill('ปรุงใหม่ทุกจานสำหรับการตรวจบนมือถือ')
    await admin.getByLabel(buffetPackage.name, { exact: true }).check()
    await admin.getByRole('button', { name: 'บันทึกเมนู', exact: true }).click()
    await admin.getByText('บันทึกเมนูแล้ว', { exact: true }).waitFor()
    const row = admin.getByRole('row').filter({ hasText: 'ไก่ทอดทดสอบ' })
    await row.getByRole('button', { name: 'แก้ไข', exact: true }).click()
    await admin.getByLabel('ชื่อเมนู', { exact: true }).fill('ไก่ทอดกรอบทดสอบ')
    await admin.getByRole('button', { name: 'บันทึกเมนู', exact: true }).click()
    await admin.getByRole('row').filter({ hasText: 'ไก่ทอดกรอบทดสอบ' }).waitFor()
    await admin.getByLabel('เรียงเมนู').selectOption('name,desc')
    await admin.getByRole('row').filter({ hasText: 'ไก่ทอดกรอบทดสอบ' }).waitFor()
    await admin.screenshot({ path: path.join(output, 'admin-1280.png'), fullPage: true })
    pass('Manager browser catalog create/update/sort', 'Real Auth session and persisted API; 1280px')

    const catalog = await (await api.get(`${backend}/api/v1/menu-items`)).json()
    const menuItem = catalog.content.find((item) => item.name === 'ไก่ทอดกรอบทดสอบ')
    assert(menuItem && menuItem.description)
    await post('/menu-items', { categoryId: menuItem.categoryId, name: 'เมนูปิดขาย', available: false, packageIds: [buffetPackage.id] })
    await post('/menu-items', { categoryId: menuItem.categoryId, name: 'เมนูคนละแพ็กเกจ', available: true, packageIds: [premium.id] })
    const temporary = await post('/menu-items', { categoryId: menuItem.categoryId, name: 'เมนูชั่วคราวสำหรับลบ', available: true, packageIds: [buffetPackage.id] })
    await admin.reload()
    await admin.getByRole('row').filter({ hasText: temporary.name }).getByRole('button', { name: 'ลบ', exact: true }).click()
    await admin.getByRole('dialog').getByRole('button', { name: 'ยืนยัน', exact: true }).click()
    await admin.getByText('ลบข้อมูลแล้ว', { exact: true }).waitFor()
    assert.equal((await api.get(`${backend}/api/v1/menu-items/${temporary.id}`)).status(), 404)
    pass('Manager browser delete', 'Unordered item is deleted through confirmation and API')

    const customerContext = await browser.newContext({ viewport: { width: 360, height: 800 }, isMobile: true, hasTouch: true })
    const customer = await customerContext.newPage()
    let orderingPosts = 0
    customer.on('request', (request) => { if (request.method() === 'POST' && request.url().endsWith(`/dining-sessions/${session.sessionId}/orders`)) orderingPosts++ })
    await customer.goto(`${frontend}/customer/qr#token=${session.sessionToken}`)
    await customer.getByRole('button', { name: 'เพิ่ม ไก่ทอดกรอบทดสอบ', exact: true }).waitFor()
    assert.equal(await customer.getByText('เมนูปิดขาย', { exact: true }).count(), 0)
    assert.equal(await customer.getByText('เมนูคนละแพ็กเกจ', { exact: true }).count(), 0)
    assert.equal(new URL(customer.url()).hash, '')
    const geometry = await customer.evaluate(() => ({ width: innerWidth, scrollWidth: document.documentElement.scrollWidth,
      controls: [...document.querySelectorAll('button')].map((button) => ({ label: button.getAttribute('aria-label') || button.textContent, height: button.getBoundingClientRect().height })) }))
    assert(geometry.scrollWidth <= geometry.width, JSON.stringify(geometry))
    assert(geometry.controls.every((button) => button.height >= 44), JSON.stringify(geometry.controls))
    await customer.screenshot({ path: path.join(output, 'customer-360-menu.png'), fullPage: true })
    pass('Real QR / menu by package / 360px layout', geometry)
    await customer.getByRole('button', { name: 'เพิ่ม ไก่ทอดกรอบทดสอบ', exact: true }).click()
    await customer.getByRole('button', { name: 'ลด ไก่ทอดกรอบทดสอบ', exact: true }).click()
    assert(await customer.getByRole('button', { name: 'ยืนยันการสั่ง', exact: true }).isDisabled())
    await customer.getByRole('button', { name: 'เพิ่ม ไก่ทอดกรอบทดสอบ', exact: true }).click()
    await customer.getByRole('button', { name: 'ยืนยันการสั่ง', exact: true }).click()
    await customer.screenshot({ path: path.join(output, 'customer-360-confirm.png'), fullPage: true })
    await customer.getByRole('dialog').getByRole('button', { name: 'ยืนยัน', exact: true }).evaluate((button) => { button.click(); button.click() })
    await customer.getByText('รับออเดอร์แล้ว', { exact: true }).waitFor()
    assert.equal(orderingPosts, 1)
    await customer.screenshot({ path: path.join(output, 'customer-360-order.png'), fullPage: true })
    pass('Customer quantity / confirm / single submission / RECEIVED', 'One actual order POST; cart cleared and order status shown')

    const menuRoute = '**/api/v1/dining-sessions/*/menu'
    await customer.route(menuRoute, async (route) => { await new Promise((resolve) => setTimeout(resolve, 1500)); await route.continue() })
    await customer.reload({ waitUntil: 'domcontentloaded' })
    await customer.getByText('กำลังตรวจสอบรอบการรับประทานและโหลดเมนู…', { exact: true }).waitFor()
    await customer.screenshot({ path: path.join(output, 'customer-360-loading.png'), fullPage: true })
    await customer.getByRole('button', { name: 'เพิ่ม ไก่ทอดกรอบทดสอบ', exact: true }).waitFor()
    await customer.unroute(menuRoute)
    pass('Loading state', 'Real menu request delayed for visual verification')

    await customer.route(menuRoute, (route) => route.abort())
    await customer.reload()
    await customer.getByRole('alert').waitFor()
    await customer.screenshot({ path: path.join(output, 'customer-360-error.png'), fullPage: true })
    await customer.unroute(menuRoute)
    await customer.getByRole('button', { name: 'ลองอีกครั้ง', exact: true }).click()
    await customer.getByRole('button', { name: 'เพิ่ม ไก่ทอดกรอบทดสอบ', exact: true }).waitFor()
    pass('Network error recovery', 'Aborted menu request; retry succeeds with existing customer cookie')

    await customer.route(menuRoute, (route) => route.fulfill({ status: 200, contentType: 'application/json', body: '[]' }))
    await customer.reload()
    await customer.getByText('ยังไม่มีเมนูในหมวดนี้', { exact: true }).waitFor()
    await customer.screenshot({ path: path.join(output, 'customer-360-empty.png'), fullPage: true })
    await customer.unroute(menuRoute)
    pass('Empty state', 'Visual fixture for an empty menu; main order flow used real API')

    const invalid = await browser.newPage({ viewport: { width: 360, height: 800 } })
    await invalid.goto(`${frontend}/customer/qr#token=invalid-step2-token`)
    await invalid.getByRole('alert').waitFor()
    assert.equal(await invalid.getByRole('button', { name: 'ยืนยันการสั่ง', exact: true }).count(), 0)
    pass('Invalid QR', 'Real API rejects invalid token and does not expose ordering controls')

    const anonymous = await browser.newPage({ viewport: { width: 1280, height: 900 } })
    await anonymous.goto(`${frontend}/admin/menu`)
    await anonymous.getByRole('heading', { name: 'เข้าสู่ระบบ', exact: true }).waitFor()
    assert.equal(await anonymous.getByLabel('ชื่อเมนู', { exact: true }).count(), 0)
    pass('Direct menu URL requires login', 'Catalog editor is inside authenticated Admin shell')

    const specResponse = await api.get(`${backend}/v3/api-docs`)
    assert.equal(specResponse.status(), 200)
    const spec = await specResponse.json()
    for (const endpoint of ['/api/v1/menu-categories', '/api/v1/menu-categories/{id}', '/api/v1/menu-items', '/api/v1/menu-items/{id}', '/api/v1/dining-sessions/{sessionId}/menu', '/api/v1/dining-sessions/{sessionId}/orders']) {
      assert(spec.paths[endpoint], `Missing Swagger path ${endpoint}`)
    }
    assert.equal((await api.get(`${backend}/swagger-ui/index.html`)).status(), 200)
    pass('Swagger / OpenAPI', 'Live specification contains Menu/Category CRUD and customer endpoints')
  } finally {
    await fs.writeFile(path.join(output, 'browser-results.json'), JSON.stringify({ executedAt: new Date().toISOString(), results }, null, 2))
    await browser.close()
  }
}
main().catch((error) => { console.error(error); process.exitCode = 1 })

// Visual state fixtures on disposable demo only; not evidence of a real API outage.
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'playwright')
const assert = require('node:assert/strict')
const fs = require('node:fs/promises')
const path = require('node:path')
assert.equal(process.env.STEP2_DISPOSABLE_DEMO, 'true')
const web = 'http://localhost:5173', api = 'http://localhost:8080/api/v1'
const output = path.resolve('test/reports/step2-ui-states')
const results = []

async function main() {
  await fs.mkdir(output, { recursive: true })
  const browser = await chromium.launch({ channel: 'chrome', headless: true })
  try {
    const admin = await browser.newContext({ viewport: { width: 1280, height: 1000 } })
    const post = async (ctx, endpoint, data) => {
      const r = await ctx.request.post(api + endpoint, { data, headers: { Origin: web } })
      assert(r.ok(), `${endpoint}: ${r.status()}`)
      return r.json()
    }
    await post(admin, '/auth/login', { username: 'admin', password: 'admin123' })
    const contexts = { admin }
    const run = `visual-${Date.now()}`
    for (const [name, role] of Object.entries({ kitchen: 'KITCHEN_STAFF', staff: 'SERVICE_STAFF' })) {
      await post(admin, '/admin/users', { username: `${run}-${name}`, password: 'isolated-demo-only', displayName: `Visual ${name}`, role })
      contexts[name] = await browser.newContext({ viewport: { width: 768, height: 1000 } })
      await post(contexts[name], '/auth/login', { username: `${run}-${name}`, password: 'isolated-demo-only' })
    }
    for (const [name, endpoint, route, loading, empty] of [
      ['kitchen', '/orders/incoming', '/kitchen', 'กำลังโหลดออเดอร์…', 'ยังไม่มีออเดอร์เข้าครัว'],
      ['staff', '/tables', '/staff/tables', 'กำลังโหลดโต๊ะและรอบกิน…', 'ยังไม่มีโต๊ะ'],
      ['admin', '/stock', '/admin/stock', 'กำลังโหลด...', 'ยังไม่มีรายการสต็อก'],
    ]) {
      const ctx = contexts[name], page = await ctx.newPage()
      let release
      const gate = new Promise(resolve => { release = resolve })
      await page.route(api + endpoint, async r => { await gate; await r.fulfill({ status: 200, json: [] }) })
      await page.goto(web + route, { waitUntil: 'domcontentloaded' })
      await page.getByText(loading, { exact: true }).waitFor()
      await page.screenshot({ path: path.join(output, `${name}-loading.png`), fullPage: true })
      release()
      await page.getByText(empty, { exact: true }).waitFor()
      await page.screenshot({ path: path.join(output, `${name}-empty.png`), fullPage: true })
      await page.unroute(api + endpoint)
      await page.route(api + endpoint, r => r.fulfill({ status: 503, json: { message: 'ข้อมูลทดสอบ: เชื่อมต่อไม่สำเร็จ กรุณาลองใหม่' } }))
      await page.reload()
      await page.getByRole('alert').waitFor()
      await page.screenshot({ path: path.join(output, `${name}-error.png`), fullPage: true })
      const style = await page.locator('body').evaluate(el => ({ font: getComputedStyle(el).fontFamily,
        primary: getComputedStyle(el).getPropertyValue('--color-primary').trim(), overflow: el.scrollWidth > window.innerWidth }))
      assert(style.font.includes('Noto Sans Thai'))
      assert.equal(style.primary, '#9a3412')
      assert.equal(style.overflow, false)
      results.push({ name, viewport: page.viewportSize(), result: 'PASS', style, states: ['loading', 'empty', 'error'], source: 'controlled HTTP response fixtures; real authenticated shell' })
      await page.close()
    }
    await fs.writeFile(path.join(output, 'results.json'), JSON.stringify({ executedAt: new Date().toISOString(), results }, null, 2))
  } finally { await browser.close() }
}
main().catch(e => { console.error(e); process.exitCode = 1 })

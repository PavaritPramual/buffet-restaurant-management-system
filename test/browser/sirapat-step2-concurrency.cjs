// Customer concurrency acceptance against an isolated local demo; controlled request delays establish each interleaving.
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'playwright')
const assert = require('node:assert/strict')
const fs = require('node:fs/promises')
const path = require('node:path')
const frontend = process.env.STEP2_FRONTEND_URL || 'http://localhost:5173'
const backend = (process.env.STEP2_BACKEND_URL || 'http://localhost:8080') + '/api/v1'
for (const url of [frontend, backend]) assert(['localhost', '127.0.0.1'].includes(new URL(url).hostname), 'Only an isolated local demo may be seeded')
const output = path.resolve(process.env.STEP2_CONCURRENCY_EVIDENCE_DIR || 'test/reports/step2-concurrency')
const results = []
function gate() { let resolve; const promise = new Promise(done => { resolve = done }); return { promise, resolve } }
async function navigate(page, url) {
  await page.evaluate(url => {
    history.pushState({ ...history.state, idx: (history.state?.idx ?? 0) + 1 }, '', url)
    window.dispatchEvent(new PopStateEvent('popstate', { state: history.state }))
  }, url)
}
async function confirm(page, itemName) {
  await page.getByRole('button', { name: 'เพิ่ม ' + itemName, exact: true }).click()
  await page.getByRole('button', { name: 'ยืนยันการสั่ง', exact: true }).click()
  await page.getByRole('dialog').getByRole('button', { name: 'ยืนยัน', exact: true }).click()
}
async function main() {
  await fs.mkdir(output, { recursive: true })
  const browser = await chromium.launch({ channel: 'chrome', headless: true })
  try {
    const admin = await browser.newContext()
    const api = admin.request
    async function post(url, data) {
      const response = await api.post(backend + url, { data })
      assert(response.ok(), url + ': ' + response.status())
      return response.json()
    }
    await post('/auth/login', { username: 'admin', password: 'admin123' })
    const runTag = Date.now().toString(36)
    const itemName = 'ไก่ทอดตรวจซ้ำ ' + runTag
    const pack = await post('/buffet-packages', { name: 'Review Package ' + runTag, price: 299 })
    const soup = await post('/soups', { name: 'Review Soup ' + runTag })
    const category = await post('/menu-categories', { name: 'ของทอดตรวจซ้ำ ' + runTag })
    await post('/menu-items', { categoryId: category.id, name: itemName, available: true, packageIds: [pack.id] })
    async function session(number) {
      const table = await post('/tables', { tableNumber: number, capacity: 4 })
      return post('/dining-sessions', { tableId: table.id, packageId: pack.id, soupId: soup.id, adultCount: 2, childCount: 0 })
    }
    const sessionA = await session('A-' + runTag)
    const sessionB = await session('B-' + runTag)
    const titleA = 'โต๊ะ ' + sessionA.tableNumber + ' · ' + pack.name
    const context = await browser.newContext({ viewport: { width: 360, height: 800 } })
    const page = await context.newPage()
    await page.goto(frontend + '/customer/qr#token=' + sessionA.sessionToken)
    await page.getByRole('button', { name: 'เพิ่ม ' + itemName, exact: true }).waitFor()
    const orderPattern = '**/api/v1/dining-sessions/' + sessionA.sessionId + '/orders'

    const committed = gate(), acknowledge = gate(), refreshRequested = gate(), allowRefresh = gate()
    let created, orderPosts = 0
    const orderHandler = async route => {
      if (route.request().method() !== 'POST') {
        refreshRequested.resolve()
        await allowRefresh.promise
        return route.continue()
      }
      orderPosts++
      const response = await route.fetch()
      assert.equal(response.status(), 201)
      created = await response.json()
      committed.resolve()
      await acknowledge.promise
      await route.fulfill({ response })
    }
    await page.route(orderPattern, orderHandler)
    await page.getByRole('button', { name: 'อัปเดตสถานะคำสั่งซื้อ' }).click()
    await refreshRequested.promise
    await confirm(page, itemName)
    await committed.promise
    allowRefresh.resolve()
    const label = 'คำสั่งซื้อ #' + created.orderId
    await page.getByText(label, { exact: true }).waitFor()
    acknowledge.resolve()
    await page.getByText('ส่งคำสั่งซื้อ #' + created.orderId + ' แล้ว', { exact: true }).waitFor()
    const cards = await page.getByText(label, { exact: true }).count()
    const persisted = await (await context.request.get(backend + '/dining-sessions/' + sessionA.sessionId + '/orders')).json()
    assert.equal(orderPosts, 1)
    assert.equal(persisted.length, 1)
    assert.equal(cards, 1)
    await page.screenshot({ path: path.join(output, 'single-order.png'), fullPage: true })
    results.push({ name: 'One order card when refresh observes it before acknowledgement', result: 'PASS', observed: { cards, actualPostCount: orderPosts, persistedOrders: persisted.length }, fixture: 'Actual order POST commits; its response is held until refresh observes the order' })
    await page.unroute(orderPattern, orderHandler)

    await page.reload()
    await page.getByRole('button', { name: 'เพิ่ม ' + itemName, exact: true }).waitFor()
    const refreshStarted = gate(), refreshRelease = gate()
    const refreshHandler = async route => {
      if (route.request().method() === 'POST') return route.fulfill({ status: 503, contentType: 'application/json', body: JSON.stringify({ message: 'ส่งคำสั่งซื้อไม่สำเร็จ' }) })
      const response = await route.fetch()
      refreshStarted.resolve()
      await refreshRelease.promise
      await route.fulfill({ response })
    }
    await page.route(orderPattern, refreshHandler)
    await page.getByRole('button', { name: 'อัปเดตสถานะคำสั่งซื้อ' }).click()
    await refreshStarted.promise
    await confirm(page, itemName)
    await page.getByRole('alert').waitFor()
    const errorBefore = await page.getByRole('alert').textContent()
    refreshRelease.resolve()
    await page.getByRole('button', { name: 'อัปเดตสถานะคำสั่งซื้อ' }).waitFor({ state: 'visible' })
    await page.waitForFunction(() => document.querySelector('button[aria-label="อัปเดตสถานะคำสั่งซื้อ"]')?.getAttribute('aria-busy') === 'false')
    const errorAfter = await page.getByRole('alert').count()
    assert.equal(errorBefore, 'ส่งคำสั่งซื้อไม่สำเร็จ')
    assert.equal(errorAfter, 1)
    assert.equal(await page.getByRole('alert').textContent(), errorBefore)
    await page.screenshot({ path: path.join(output, 'order-error-retained.png'), fullPage: true })
    results.push({ name: 'Order failure remains after an earlier history refresh completes', result: 'PASS', observed: { errorBefore, alertsAfterRefresh: errorAfter }, fixture: '503 order response injected; real earlier GET response delayed' })
    await page.unroute(orderPattern, refreshHandler)

    const scanStarted = gate(), exchangeRelease = gate()
    const qrHandler = async route => {
      if (route.request().postDataJSON().token === sessionB.sessionToken) {
        scanStarted.resolve()
        await exchangeRelease.promise
      }
      await route.continue()
    }
    await page.route('**/api/v1/dining-sessions/qr-exchange', qrHandler)
    await navigate(page, '/customer/qr#token=' + sessionB.sessionToken)
    await scanStarted.promise
    await navigate(page, '/design-system')
    await page.waitForFunction(() => !document.querySelector('.customer-page'))
    await page.goBack()
    await page.getByText('กำลังตรวจสอบรอบการรับประทานและโหลดเมนู…', { exact: true }).waitFor()
    assert.equal(await page.getByText(titleA, { exact: true }).count(), 0)
    const completedB = page.waitForResponse(response => response.url().endsWith('/qr-exchange') && response.status() === 200)
    exchangeRelease.resolve()
    await completedB
    const titleB = 'โต๊ะ ' + sessionB.tableNumber + ' · ' + pack.name
    await page.getByText(titleB, { exact: true }).waitFor()
    const serverContext = await (await context.request.get(backend + '/dining-sessions/customer-context')).json()
    assert.equal(serverContext.sessionId, sessionB.sessionId)
    assert.equal(await page.getByText(titleA, { exact: true }).count(), 0)
    const wrongOrder = await context.request.post(backend + '/dining-sessions/' + sessionA.sessionId + '/orders', {
      headers: { Origin: frontend }, data: { items: [{ menuItemId: created.items[0].menuItemId, quantity: 1 }] },
    })
    assert.equal(wrongOrder.status(), 404)
    await confirm(page, itemName)
    await page.getByText('รับออเดอร์แล้ว', { exact: true }).waitFor()
    const ordersB = await (await context.request.get(backend + '/dining-sessions/' + sessionB.sessionId + '/orders')).json()
    assert.equal(ordersB.length, 1)
    assert.equal(ordersB[0].sessionId, sessionB.sessionId)
    await page.screenshot({ path: path.join(output, 'restored-table-B.png'), fullPage: true })
    results.push({ name: 'Remount waits for the latest QR, restores B and orders successfully', result: 'PASS', observed: { uiTable: sessionB.tableNumber, cookieTable: serverContext.tableNumber, persistedBOrders: ordersB.length, mismatchedOrderHttpStatus: wrongOrder.status() }, fixture: 'B exchange held before server processing; SPA route left and browser Back restores its fragment-free URL' })
    console.log(JSON.stringify(results, null, 2))
    await fs.writeFile(path.join(output, 'browser-concurrency-results.json'), JSON.stringify({ executedAt: new Date().toISOString(), results }, null, 2))
  } finally { await browser.close() }
}
main().catch(error => { console.error(error); process.exitCode = 1 })

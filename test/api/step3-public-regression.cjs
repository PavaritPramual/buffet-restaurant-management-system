// Real HTTPS API checks. Browser evidence is collected separately through the UI.
// Credentials and cookie/token values never enter the committed evidence.
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const crypto = require('node:crypto')
const {execFileSync} = require('node:child_process')
const {validateRuntimeUrls} = require('../browser/step3-runtime-config.cjs')
const web = process.env.FINAL_WEB_URL
const api = process.env.FINAL_API_URL
validateRuntimeUrls('public', web, api)
assert.equal(process.env.FINAL_ALLOW_TEST_DATA, 'true', 'Approved test-data scope required')
assert(process.env.FINAL_MANAGER_USERNAME && process.env.FINAL_MANAGER_PASSWORD, 'Manager access required')
const output = path.resolve(process.env.FINAL_EVIDENCE_DIR || 'test/reports/public-api')
const accessFile = path.resolve(process.env.FINAL_LOCAL_ACCESS_FILE || 'test/reports/public-access.json')
assert(accessFile.startsWith(path.resolve('test/reports') + path.sep), 'Access file must stay in ignored local reports')
execFileSync('git', ['check-ignore', accessFile], {stdio:'pipe'})
const run = process.env.FINAL_RUN_LABEL || `QA${Date.now().toString().slice(-10)}`
assert(/^[A-Za-z0-9-]{3,28}$/.test(run), 'Use a unique simple test-data label')
const startedAt = new Date().toISOString()
const results = [], clients = {}, resources = {}, cookies = []
let scenario = 'startup'
const pass = detail => {results.push({name:scenario,result:'PASS',detail}); console.log(`PASS ${scenario}`)}
class Client {
  constructor(name) {this.name=name; this.jar=new Map()}
  async request(method, endpoint, data, expected=200, extraHeaders={}) {
    const url = new URL(api + endpoint)
    const cookie = [...this.jar.values()].filter(c=>url.pathname.startsWith(c.path)).map(c=>c.pair).join('; ')
    const response = await fetch(url, {method, redirect:'error', signal:AbortSignal.timeout(30000),
      headers:{Origin:new URL(web).origin,...(cookie?{Cookie:cookie}:{}),...(data===undefined?{}:{'Content-Type':'application/json'}),...extraHeaders},
      ...(data===undefined?{}:{body:JSON.stringify(data)})})
    for(const header of response.headers.getSetCookie()) {
      const [pair,...attributes]=header.split(';').map(s=>s.trim())
      const name=pair.split('=')[0], attrs=Object.fromEntries(attributes.map(s=>{const i=s.indexOf('=');return i<0?[s.toLowerCase(),true]:[s.slice(0,i).toLowerCase(),s.slice(i+1)]}))
      const entry={name,path:attrs.path||'/',secure:attrs.secure===true,httpOnly:attrs.httponly===true,sameSite:attrs.samesite||null,maxAge:attrs['max-age']===undefined?null:Number(attrs['max-age'])}
      cookies.push({client:this.name,...entry})
      if(entry.maxAge===0) this.jar.delete(name); else this.jar.set(name,{pair,path:entry.path})
    }
    assert((Array.isArray(expected)?expected:[expected]).includes(response.status), `${this.name} ${method} ${endpoint}: expected ${expected}, received ${response.status}`)
    const body=await response.text()
    if(response.status===204 || (!body && endpoint==='/auth/me' && response.status===401)) return null
    const value=JSON.parse(body)
    if(response.status>=400) {
      assert.equal(value.status,response.status)
      assert.equal(value.path,url.pathname)
      assert.equal(typeof value.message,'string'); assert(value.message.length)
      assert.equal(typeof value.error,'string'); assert(!Number.isNaN(Date.parse(value.timestamp)))
    }
    return value
  }
}
async function main() {
  fs.mkdirSync(output,{recursive:true}); fs.mkdirSync(path.dirname(accessFile),{recursive:true})
  for(const role of ['manager','supervisor','staff','kitchen','anonymous','phoneA','phoneB']) clients[role]=new Client(role)
  const m=clients.manager, s=clients.staff, k=clients.kitchen, a=clients.phoneA, b=clients.phoneB
  scenario='Manager login and public Staff cookie attributes'
  const manager=await m.request('POST','/auth/login',{username:process.env.FINAL_MANAGER_USERNAME,password:process.env.FINAL_MANAGER_PASSWORD})
  assert.equal(manager.role,'MANAGER')
  const staffCookie=cookies.find(c=>c.client==='manager'&&c.name==='JSESSIONID')
  assert(staffCookie&&staffCookie.secure&&staffCookie.httpOnly); assert.equal(staffCookie.sameSite.toLowerCase(),'lax')
  pass({role:manager.role,cookie:staffCookie})
  scenario='Create isolated test accounts and authenticate all roles'
  const accounts={}
  for(const [role,enumRole] of [['supervisor','SUPERVISOR'],['staff','SERVICE_STAFF'],['kitchen','KITCHEN_STAFF']]) {
    accounts[role]={username:`${run}-${role}`,password:crypto.randomBytes(24).toString('base64url')}
    const user=await m.request('POST','/admin/users',{...accounts[role],role:enumRole,displayName:`[TEST DATA] ${run} ${role}`,firstName:'QA',lastName:run},201)
    resources[`${role}UserId`]=user.id
    assert.equal((await clients[role].request('POST','/auth/login',accounts[role])).role,enumRole)
  }
  fs.writeFileSync(accessFile,JSON.stringify({run,accounts},null,2)+'\n',{mode:0o600})
  pass({userIds:Object.fromEntries(Object.entries(resources)),roles:3,credentials:'ignored local access file only'})
  scenario='Invalid credentials, anonymous and spoofed-role denials'
  await clients.anonymous.request('POST','/auth/login',{username:run+'-missing',password:crypto.randomBytes(24).toString('hex')},401)
  for(const [role,incoming,ready,stock] of [['manager',403,403,200],['supervisor',403,403,200],['staff',403,200,403],['kitchen',200,403,403],['anonymous',401,401,401]]) {
    for(const [endpoint,status] of [['/orders/incoming',incoming],['/orders/ready',ready],['/stock',stock]]) await clients[role].request('GET',endpoint,undefined,status,{'X-User-Role':'MANAGER'})
  }
  pass({denialsWithSpoofedHeader:15,invalidLogin:401})
  scenario='Unique test master data and Menu pagination'
  const pack=await m.request('POST','/buffet-packages',{name:`[TEST DATA] ${run}`,price:399,description:'Public regression data',active:true},201)
  const soup=await m.request('POST','/soups',{name:`[TEST DATA] ${run}`,active:true},201)
  const table=await m.request('POST','/tables',{tableNumber:run,capacity:4},201)
  const category=await m.request('POST','/menu-categories',{name:run,active:true},201)
  const item=await m.request('POST','/menu-items',{name:run+' menu',description:'QA menu',categoryId:category.id,packageIds:[pack.id],available:true},201)
  Object.assign(resources,{packageId:pack.id,soupId:soup.id,tableId:table.id,categoryId:category.id,menuItemId:item.id})
  const page=await m.request('GET','/menu-items?page=0&size=1&sort=name,asc')
  assert(Array.isArray(page.content));assert(page.content.length<=1)
  await m.request('GET','/menu-items?page=0&size=1&sort=unknown,asc',undefined,400)
  pass({...resources,pagination:true,invalidSort:400})
  scenario='Open session, unpaid close guard and snapshot pricing'
  const session=await s.request('POST','/dining-sessions',{tableId:table.id,packageId:pack.id,soupId:soup.id,adultCount:2,childCount:1},201)
  resources.sessionId=session.sessionId
  const endpoint=`/dining-sessions/${session.sessionId}`
  await s.request('POST',endpoint+'/close',undefined,400)
  assert.equal((await s.request('GET',endpoint)).sessionStatus,'ACTIVE')
  assert.equal((await m.request('GET',`/tables/${table.id}`)).status,'OCCUPIED')
  await m.request('PUT',`/buffet-packages/${pack.id}`,{name:pack.name,price:499,description:'Changed after QA session opened',active:true})
  assert.equal((await s.request('POST','/billing/preview',{sessionId:session.sessionId})).totalAmount,997.5)
  pass({sessionId:session.sessionId,snapshotPrice:399,newPackagePrice:499,totalAmount:997.5,unpaidClose:400})
  scenario='One-use QR, independent customer cookies and credential-free responses'
  const qrA=(await s.request('GET',endpoint)).sessionToken
  const exchanged=await a.request('POST','/dining-sessions/qr-exchange',{token:qrA})
  assert.equal(exchanged.sessionId,session.sessionId); assert(!('sessionToken' in exchanged));assert(!('token' in exchanged))
  await clients.anonymous.request('POST','/dining-sessions/qr-exchange',{token:qrA},404)
  await b.request('POST','/dining-sessions/qr-exchange',{token:(await s.request('GET',endpoint)).sessionToken})
  for(const client of [a,b]) assert.equal((await client.request('GET','/dining-sessions/customer-context')).sessionId,session.sessionId)
  const customerCookies=cookies.filter(c=>c.name==='customer_session')
  assert.equal(customerCookies.length,2)
  for(const cookie of customerCookies) {assert(cookie.secure&&cookie.httpOnly);assert.equal(cookie.sameSite.toLowerCase(),'lax');assert.equal(cookie.path,'/api/v1/dining-sessions');assert.equal(cookie.maxAge,28800)}
  await clients.anonymous.request('GET',endpoint+'/orders',undefined,401)
  assert((await a.request('GET',endpoint+'/menu')).some(row=>row.id===item.id))
  pass({sessionId:session.sessionId,independentClients:2,qrReuse:404,cookies:customerCookies,responseContainsCredential:false})
  scenario='Ordering validation, role and State transition guards'
  await a.request('POST',endpoint+'/orders',{items:[{menuItemId:item.id,quantity:0}]},400)
  const order=await a.request('POST',endpoint+'/orders',{items:[{menuItemId:item.id,quantity:1}]},201)
  resources.orderId=order.orderId;assert.equal(order.status,'RECEIVED')
  const denials=[]
  const deny=async(role,status,http,stored)=>{
    await clients[role].request('PATCH',`/orders/${order.orderId}/status`,{status},http,{'X-User-Role':'MANAGER'})
    assert.equal((await a.request('GET',endpoint+'/orders')).find(row=>row.orderId===order.orderId).status,stored)
    denials.push({role,status,http,stored})
  }
  await deny('anonymous','PREPARING',401,'RECEIVED')
  for(const role of ['manager','supervisor','staff']) await deny(role,'PREPARING',403,'RECEIVED')
  await deny('kitchen','READY',400,'RECEIVED');await deny('kitchen','unknown',400,'RECEIVED')
  await k.request('PATCH',`/orders/${Number.MAX_SAFE_INTEGER}/status`,{status:'PREPARING'},404)
  await k.request('PATCH',`/orders/${order.orderId}/status`,{status:'PREPARING'})
  await deny('kitchen','RECEIVED',400,'PREPARING')
  await k.request('PATCH',`/orders/${order.orderId}/status`,{status:'READY'})
  await deny('kitchen','SERVED',403,'READY');await deny('kitchen','PREPARING',400,'READY')
  await s.request('PATCH',`/orders/${order.orderId}/status`,{status:'SERVED'})
  await deny('staff','SERVED',400,'SERVED');await deny('kitchen','READY',400,'SERVED')
  pass({orderId:order.orderId,state:'SERVED',denials,unknownOrder:404})
  scenario='Bill request blocks both customer contexts'
  assert.equal((await a.request('POST',endpoint+'/bill-request')).status,'REQUESTED')
  for(const client of [a,b]) {
    assert.equal((await client.request('GET',endpoint+'/bill-status')).status,'REQUESTED')
    await client.request('POST',endpoint+'/orders',{items:[{menuItemId:item.id,quantity:1}]},409)
  }
  pass({contexts:2,rejectedOrderStatus:409})
  scenario='Payment, duplicate rejection, explicit close and customer revocation'
  const payment=await s.request('POST','/payments',{sessionId:session.sessionId,paymentMethod:'CASH'},201)
  assert.equal(payment.amount,997.5)
  await s.request('POST','/payments',{sessionId:session.sessionId,paymentMethod:'CASH'},409)
  assert.equal((await s.request('GET',`/payments/sessions/${session.sessionId}`)).amount,997.5)
  assert.equal((await s.request('GET',endpoint)).sessionStatus,'ACTIVE')
  for(const client of [a,b]) {const bill=await client.request('GET',endpoint+'/bill-status');assert.equal(bill.status,'PAID');assert.equal(bill.dueAmount,0);assert.equal(bill.paidAmount,997.5)}
  await s.request('POST',endpoint+'/close')
  assert.equal((await m.request('GET',`/tables/${table.id}`)).status,'AVAILABLE')
  for(const client of [a,b]) await client.request('GET',endpoint+'/orders',undefined,[401,404])
  pass({sessionId:session.sessionId,totalAmount:997.5,duplicate:409,closed:true,tableStatus:'AVAILABLE',revokedContexts:2})
  scenario='Stock targets, inactive guards, role denials and preserved history'
  const stock=await m.request('POST','/stock/items',{sku:run,name:`[TEST DATA] ${run} stock`,unit:'kg',lowStockThreshold:1.125,openingTargetStock:5.125},201)
  resources.stockItemId=stock.id;assert.equal(stock.quantity,0);assert.equal(stock.shortfall,5.125)
  const supervisor=clients.supervisor
  await supervisor.request('POST',`/stock/${stock.id}/in`,{quantity:2.125,reason:run})
  await supervisor.request('POST',`/stock/${stock.id}/adjustments`,{quantityDelta:1,reason:run})
  await m.request('PUT',`/stock/items/${stock.id}/active`,{active:false})
  const frozen=(await m.request('GET','/stock')).find(row=>row.id===stock.id)
  const history=await m.request('GET',`/stock/transactions?itemId=${stock.id}`)
  assert.equal(frozen.quantity,3.125);assert.equal(frozen.shortfall,2)
  await supervisor.request('POST',`/stock/${stock.id}/in`,{quantity:1,reason:run},409)
  await supervisor.request('POST',`/stock/${stock.id}/adjustments`,{quantityDelta:1,reason:run},409)
  assert.deepEqual((await m.request('GET','/stock')).find(row=>row.id===stock.id),frozen)
  assert.deepEqual(await m.request('GET',`/stock/transactions?itemId=${stock.id}`),history)
  for(const role of ['supervisor','staff','kitchen','anonymous']) await clients[role].request('PUT',`/stock/items/${stock.id}/active`,{active:true},role==='anonymous'?401:403)
  pass({stockItemId:stock.id,quantity:3.125,shortfall:2,inactive:true,historyEntries:history.length,unchangedAfterDenial:true})
  scenario='Profile update, validation and permission guards'
  const users=await m.request('GET','/admin/users')
  const target=users.find(row=>row.username===accounts.staff.username)
  const profile={firstName:'QA Updated',lastName:run,phoneNumber:'',email:'qa@example.test'}
  await m.request('PUT',`/admin/users/${target.id}/profile`,profile)
  const stored=(await m.request('GET','/admin/users')).find(row=>row.id===target.id)
  assert.equal(stored.firstName,profile.firstName);assert.equal(stored.role,'SERVICE_STAFF');assert.equal(stored.username,target.username)
  await m.request('PUT',`/admin/users/${target.id}/profile`,{...profile,firstName:'x'.repeat(101)},400)
  for(const role of ['supervisor','staff','kitchen','anonymous']) await clients[role].request('PUT',`/admin/users/${target.id}/profile`,profile,role==='anonymous'?401:403)
  assert.deepEqual((await m.request('GET','/admin/users')).find(row=>row.id===target.id),stored)
  pass({userId:target.id,firstNameLimit:100,unchangedAfterDenials:true})
  scenario='Logout invalidates each Staff role'
  for(const role of ['manager','supervisor','staff','kitchen']) {await clients[role].request('POST','/auth/logout',undefined,204);await clients[role].request('GET','/auth/me',undefined,401)}
  pass({roles:4,protectedStatus:401,timedTTL:false})
}
main().catch(error=>{results.push({name:scenario,result:'FAIL',detail:'Assertion or transport failure; no request bodies or credentials published'});console.error(`FAIL ${scenario}: ${error instanceof assert.AssertionError?error.message:'transport or setup failure'}`);process.exitCode=1}).finally(()=>{
  fs.mkdirSync(output,{recursive:true})
  fs.writeFileSync(path.join(output,'results.json'),JSON.stringify({startedAt,finishedAt:new Date().toISOString(),sourceCommit:execFileSync('git',['rev-parse','HEAD'],{encoding:'utf8'}).trim(),
    runnerSha256:crypto.createHash('sha256').update(fs.readFileSync(__filename)).digest('hex'),environment:'public HTTPS API',webUrl:web,apiUrl:api,
    deployedCommit:process.env.FINAL_DEPLOYED_COMMIT||null,deployedCommitSource:'owner evidence in repository; not independently attested by API',
    httpMocks:false,browser:false,run,resources,counts:{passed:results.filter(r=>r.result==='PASS').length,failed:results.filter(r=>r.result==='FAIL').length},
    pending:['Browser evidence collected separately','Timed TTL (customer lifetime 8 hours)','Runtime startup/schema and release SHA attestation','Persistence after redeploy and cold start'],results},null,2)+'\n')
  fs.writeFileSync(path.join(output,'ui-handoff.json'),JSON.stringify({run,resources},null,2)+'\n')
})

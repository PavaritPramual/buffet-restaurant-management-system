const fs = require('node:fs')
const path = require('node:path')
const crypto = require('node:crypto')
const { spawn, spawnSync } = require('node:child_process')
const root = process.cwd()
const web = 'http://127.0.0.1:5177'
const api = 'http://127.0.0.1:8087/api/v1'
const playwright = 'C:/Users/ohm25/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright'
const { request } = require(playwright)
const accounts = Object.fromEntries(['manager','supervisor','staff','kitchen'].map(name => [name, {
  username: 'sirapat-submit-' + name,
  password: crypto.randomBytes(24).toString('hex'),
}]))
const env = { ...process.env, CORS_ALLOWED_ORIGINS: web,
  BOOTSTRAP_ADMIN_ENABLED: 'true', BOOTSTRAP_ADMIN_USERNAME: accounts.manager.username,
  BOOTSTRAP_ADMIN_PASSWORD: accounts.manager.password,
  VITE_API_BASE_URL: api, PLAYWRIGHT_MODULE: playwright,
  FINAL_ENVIRONMENT: 'local-h2', FINAL_ALLOW_TEST_DATA: 'true',
  FINAL_WEB_URL: web, FINAL_API_URL: api,
  FINAL_EVIDENCE_DIR: 'test/reports/sirapat-submit-core-flow',
  FINAL_STATE_EVIDENCE_DIR: 'test/reports/sirapat-submit-ui-states',
  FINAL_CONCURRENCY_EVIDENCE_DIR: 'test/reports/sirapat-submit-concurrency',
}
for (const [name, account] of Object.entries(accounts)) {
  env['FINAL_' + name.toUpperCase() + '_USERNAME'] = account.username
  env['FINAL_' + name.toUpperCase() + '_PASSWORD'] = account.password
}
const children = [], logs = []
function launch(executable, args, name, cwd) {
  const fd = fs.openSync(path.join(root,'test/reports/sirapat-submit-' + name + '.log'),'w')
  logs.push(fd)
  const child = spawn(executable,args,{cwd,env,windowsHide:true,stdio:['ignore',fd,fd]})
  child.on('error',() => { process.exitCode = 1 })
  children.push(child)
  return child
}
async function ready(url, child) {
  const probe = await request.newContext()
  try { for (let i = 0; i < 60; i++) {
    if (child.exitCode !== null) throw new Error('Isolated runtime exited before readiness')
    try {
      const r = await probe.get(url,{timeout:2000})
      if (r.ok()) { console.log('Runtime ready: ' + url); return }
      if (i === 5 || i === 20) console.log('Readiness HTTP ' + r.status() + ': ' + url)
    } catch (error) { if (i === 5 || i === 20) console.log('Readiness connection failure: ' + url + ' ' + error.message.split('\n')[0]) }
    await new Promise(resolve => setTimeout(resolve,1000))
  } } finally { await probe.dispose() }
  throw new Error('Isolated runtime readiness timed out')
}
async function main() {
  const backend = launch('C:/Program Files/Java/jdk-21/bin/java.exe',[
    '-jar','code/backend/target/app.jar','--spring.config.import=',
    '--spring.profiles.active=local-regression','--server.port=8087',
    '--spring.datasource.url=jdbc:h2:mem:sirapat_submit;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1',
    '--spring.datasource.driver-class-name=org.h2.Driver',
    '--spring.datasource.username=sa','--spring.datasource.password=',
    '--app.bootstrap-admin.enabled=true',
    '--spring.flyway.locations=classpath:db/migration/common,classpath:db/migration/h2',
    '--app.seed.enabled=false','--app.master-data.access-provider=session',
    '--app.menu.admin-access-provider=session','--app.ordering.session-provider=database',
    '--app.dining-session.staff-access-provider=session','--app.fulfillment.access-provider=session',
    '--app.billing.context-provider=database','--app.payment.status-provider=database',
  ],'runtime',root)
  const frontend = launch(process.execPath,[
    'node_modules/vite/bin/vite.js','--host','127.0.0.1','--port','5177','--strictPort',
  ],'vite',path.join(root,'code/frontend'))
  await Promise.all([ready(api+'/system/health',backend),ready(web,frontend)])
  const setup = await request.newContext({extraHTTPHeaders:{Origin:web}})
  try {
    const loggedIn = await setup.post(api+'/auth/login',{data:accounts.manager})
    if (loggedIn.status() !== 200) {
      const error = await loggedIn.json().catch(() => ({}))
      throw new Error('Isolated manager login failed HTTP ' + loggedIn.status() + ': ' + (error.message || 'no error message'))
    }
    for (const [name,role] of [['staff','SERVICE_STAFF'],['kitchen','KITCHEN_STAFF'],['supervisor','SUPERVISOR']]) {
      const created = await setup.post(api+'/admin/users',{data:{...accounts[name],role,
        displayName:'Sirapat isolated ' + name,email:name+'@example.test'}})
      if (created.status() !== 201) throw new Error('Isolated account creation failed')
    }
  } finally { await setup.dispose() }
  const scripts = ['step3-core-flow.cjs','step3-ui-states.cjs','step3-concurrency.cjs']
  const summary = []
  for (const script of scripts) {
    const result = spawnSync(process.execPath,['test/browser/'+script],{cwd:root,env,stdio:'inherit',windowsHide:true})
    summary.push({script,exitCode:result.status})
    if (result.status !== 0) throw new Error('Browser runner failed: ' + script)
  }
  fs.writeFileSync('test/reports/sirapat-submit-browser-summary.json',JSON.stringify({
    database:'isolated in-memory H2',envFileImport:false,seedFixtures:false,
    providers:{masterData:'session',menu:'session',ordering:'database',diningSession:'session',fulfillment:'session',billing:'database',payment:'database'},
    webUrl:web,apiUrl:api,scripts:summary,
  },null,2))
}
main().catch(error => { console.error(error.message); process.exitCode=1 }).finally(() => {
  for (const child of children) child.kill()
  for (const fd of logs) fs.closeSync(fd)
})

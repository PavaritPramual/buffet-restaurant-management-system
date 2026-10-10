// Disposable H2 runtime. No env-file import, shared DB or stored credentials.
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const crypto = require('node:crypto')
const {spawn,spawnSync,execFileSync} = require('node:child_process')
const root = process.cwd()
const label = process.env.FINAL_RUN_LABEL || 'step3-local'
assert(/^[a-z0-9-]+$/.test(label),'Use a simple run label')
const output = path.resolve(process.env.FINAL_LOCAL_OUTPUT || `test/reports/${label}`)
fs.mkdirSync(output,{recursive:true})
const webPort = Number(process.env.FINAL_LOCAL_WEB_PORT || 5177)
const apiPort = Number(process.env.FINAL_LOCAL_API_PORT || 8087)
const web = `http://127.0.0.1:${webPort}`, api = `http://127.0.0.1:${apiPort}/api/v1`
const {request} = require(process.env.PLAYWRIGHT_MODULE || 'playwright')
const accounts = Object.fromEntries(['manager','supervisor','staff','kitchen'].map(name=>[name,{
  username:`${label}-${name}`,password:crypto.randomBytes(24).toString('hex'),
}]))
const env = {...process.env,CORS_ALLOWED_ORIGINS:web,BOOTSTRAP_ADMIN_ENABLED:'true',
  BOOTSTRAP_ADMIN_USERNAME:accounts.manager.username,BOOTSTRAP_ADMIN_PASSWORD:accounts.manager.password,
  VITE_API_BASE_URL:api,FINAL_ENVIRONMENT:'local-h2',FINAL_ALLOW_TEST_DATA:'true',FINAL_WEB_URL:web,FINAL_API_URL:api,
  FINAL_EVIDENCE_DIR:path.join(output,'core-flow'),FINAL_STATE_EVIDENCE_DIR:path.join(output,'ui-state-fixtures'),
  FINAL_CONCURRENCY_EVIDENCE_DIR:path.join(output,'concurrency-fixtures'),FINAL_STOCK_PROFILE_EVIDENCE_DIR:path.join(output,'stock-profile'),
}
for (const [name,account] of Object.entries(accounts)) {
  env[`FINAL_${name.toUpperCase()}_USERNAME`]=account.username
  env[`FINAL_${name.toUpperCase()}_PASSWORD`]=account.password
}
const children=[],fds=[]
function launch(binary,args,name,cwd) {
  const fd=fs.openSync(path.join(output,`${name}.log`),'w'); fds.push(fd)
  const child=spawn(binary,args,{cwd,env,windowsHide:true,stdio:['ignore',fd,fd]})
  child.on('error',()=>{process.exitCode=1}); children.push(child); return child
}
async function ready(url,child) {
  const probe=await request.newContext()
  try {for(let i=0;i<90;i++) {
    if(child.exitCode!==null) throw Error('Runtime exited before readiness; inspect local log')
    try {if((await probe.get(url,{timeout:1000})).ok()) return} catch {}
    await new Promise(resolve=>setTimeout(resolve,500))
  }} finally {await probe.dispose()}
  throw Error('Runtime readiness timed out')
}
async function main() {
  const startedAt=new Date().toISOString()
  const sourceCommit=execFileSync('git',['rev-parse','HEAD'],{encoding:'utf8'}).trim()
  const backend=launch(process.env.FINAL_JAVA || 'java',[
    '-jar','code/backend/target/app.jar','--spring.config.import=','--spring.profiles.active=local-regression',
    `--server.port=${apiPort}`,`--spring.datasource.url=jdbc:h2:mem:${label.replaceAll('-','_')};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1`,
    '--spring.datasource.driver-class-name=org.h2.Driver','--spring.datasource.username=sa','--spring.datasource.password=',
    '--spring.flyway.locations=classpath:db/migration/common,classpath:db/migration/h2',
    '--app.seed.enabled=false','--app.master-data.access-provider=session','--app.menu.admin-access-provider=session',
    '--app.ordering.session-provider=database','--app.dining-session.staff-access-provider=session','--app.fulfillment.access-provider=session',
    '--app.billing.context-provider=database','--app.payment.status-provider=database',
  ],'runtime',root)
  const frontend=launch(process.execPath,['node_modules/vite/bin/vite.js','--host','127.0.0.1','--port',String(webPort),'--strictPort'],'vite',path.join(root,'code/frontend'))
  await Promise.all([ready(api+'/system/health',backend),ready(web,frontend)])
  const setup=await request.newContext({extraHTTPHeaders:{Origin:web}})
  try {
    let loggedIn=false
    for(let i=0;i<60;i++) {
      if((await setup.post(api+'/auth/login',{data:accounts.manager})).status()===200) {loggedIn=true;break}
      await new Promise(resolve=>setTimeout(resolve,500))
    }
    assert(loggedIn,'Bootstrap manager was not ready')
    for(const [name,role] of [['staff','SERVICE_STAFF'],['kitchen','KITCHEN_STAFF'],['supervisor','SUPERVISOR']]) {
      assert.equal((await setup.post(api+'/admin/users',{data:{...accounts[name],role,displayName:`Isolated ${name}`,
        firstName:'Isolated',lastName:name,phoneNumber:'',email:`${name}@example.test`}})).status(),201,'Isolated account setup')
    }
  } finally {await setup.dispose()}
  const scripts=(process.env.FINAL_LOCAL_SCRIPTS || 'test/browser/step3-core-flow.cjs,test/browser/step3-ui-states.cjs,test/browser/step3-concurrency.cjs').split(',')
  const results=[]
  for(const script of scripts) {
    const r=spawnSync(process.execPath,[script],{cwd:root,env,stdio:'inherit',windowsHide:true})
    results.push({script:path.basename(script),exitCode:r.status,runnerSha256:crypto.createHash('sha256').update(fs.readFileSync(script)).digest('hex')})
    assert.equal(r.status,0,`Browser runner failed: ${path.basename(script)}`)
  }
  fs.writeFileSync(path.join(output,'runtime-summary.json'),JSON.stringify({sourceCommit,startedAt,finishedAt:new Date().toISOString(),
    database:'fresh isolated in-memory H2',envFileImport:false,seedFixtures:false,httpMocks:'specified per runner',
    providers:{masterData:'session',menu:'session',ordering:'database',diningSession:'session',fulfillment:'session',billing:'database',payment:'database'},
    webUrl:web,apiUrl:api,scripts:results,publicAcceptance:false,
  },null,2)+'\n')
}
main().catch(error=>{console.error(error.message);process.exitCode=1}).finally(()=>{
  for(const child of children) child.kill()
  for(const fd of fds) fs.closeSync(fd)
})

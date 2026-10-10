// Hash Git blob bytes, independent of checkout line endings and machine paths.
const assert = require('node:assert/strict')
const fs = require('node:fs')
const crypto = require('node:crypto')
const { execFileSync } = require('node:child_process')
const sha256 = bytes => crypto.createHash('sha256').update(bytes).digest('hex')
const paths = ['code/backend/src/main','code/backend/src/test','code/backend/pom.xml','code/frontend/src','code/frontend/package.json','code/frontend/package-lock.json','code/frontend/vite.config.ts','test/browser']
function manifest(ref) {
  const commit = execFileSync('git',['rev-parse',`${ref}^{commit}`],{encoding:'utf8'}).trim()
  const entries = execFileSync('git',['ls-tree','-r','-z',commit,'--',...paths]).toString('utf8').split('\0').filter(Boolean)
  const files = Object.fromEntries(entries.map(entry => {
    const [meta, name] = entry.split('\t')
    const [mode, type, blob] = meta.split(' ')
    assert.equal(type,'blob')
    return [name,{gitBlob:blob,mode,sha256:sha256(execFileSync('git',['cat-file','blob',blob],{maxBuffer:20*1024*1024}))}]
  }))
  return {sourceCommit:commit,byteFormat:'unmodified Git blob bytes; no LF/CRLF/BOM conversion',algorithm:'SHA256',files}
}
if (require.main === module) {
  const [command, ref, file] = process.argv.slice(2)
  assert(['write','verify'].includes(command) && ref && file,'Usage: node evidence-source-hashes.cjs write|verify <commit> <manifest.json>')
  const actual = manifest(ref)
  if (command === 'write') fs.writeFileSync(file,JSON.stringify(actual,null,2)+'\n')
  else {
    const recorded = JSON.parse(fs.readFileSync(file,'utf8'))
    assert.equal(recorded.sourceCommit,actual.sourceCommit,'Verify the recorded commit; a docs-only newer head needs a separate equivalence record')
    assert.equal(recorded.byteFormat,actual.byteFormat)
    assert.equal(recorded.algorithm,actual.algorithm)
    assert.deepEqual(Object.keys(recorded).sort(),Object.keys(actual).sort())
    assert.equal(Object.keys(recorded.files).length,Object.keys(actual.files).length,'Source file counts differ')
    for (const [name,entry] of Object.entries(actual.files)) assert.deepEqual(recorded.files[name],entry,`Git blob identity/hash mismatch: ${name}`)
  }
  console.log(`${command.toUpperCase()}: ${Object.keys(actual.files).length} canonical Git blob hashes at ${actual.sourceCommit}`)
}
module.exports = {manifest}

const assert = require('node:assert/strict')

function validateRuntimeUrls(mode, web, api) {
  assert(['local-h2', 'local-postgres', 'public'].includes(mode), 'Set FINAL_ENVIRONMENT explicitly')
  const urls = [web, api].map(address => new URL(address))
  for (const url of urls) {
    assert(!url.username && !url.password && !url.search && !url.hash, 'URLs must not contain credentials/query/fragment')
    if (mode === 'public') assert.equal(url.protocol, 'https:', 'Public acceptance requires HTTPS')
    else assert(['localhost', '127.0.0.1'].includes(url.hostname), 'Local runs require loopback URLs')
  }
  if (mode === 'public') assert.equal(urls[0].origin, urls[1].origin, 'Public acceptance requires same-origin HTTPS Web and API')
}

module.exports = { validateRuntimeUrls }

const { test } = require('node:test')
const assert = require('node:assert/strict')
const { validateRuntimeUrls } = require('./step3-runtime-config.cjs')

test('public accepts HTTPS paths and equivalent default ports on one origin', () => {
  validateRuntimeUrls('public', 'https://buffet.example', 'https://buffet.example:443/api/v1')
})
test('public rejects HTTP even when both URLs share an origin', () => {
  assert.throws(() => validateRuntimeUrls('public', 'http://buffet.example', 'http://buffet.example/api/v1'), /requires HTTPS/)
})
test('public rejects a separate API hostname', () => {
  assert.throws(() => validateRuntimeUrls('public', 'https://buffet.example', 'https://api.buffet.example/api/v1'), /same-origin HTTPS/)
})
test('public rejects a separate API port', () => {
  assert.throws(() => validateRuntimeUrls('public', 'https://buffet.example', 'https://buffet.example:8443/api/v1'), /same-origin HTTPS/)
})
test('public rejects embedded credentials and query or fragment data', () => {
  for (const api of ['https://user:secret@buffet.example/api/v1', 'https://buffet.example/api/v1?token=x', 'https://buffet.example/api/v1#token=x']) {
    assert.throws(() => validateRuntimeUrls('public', 'https://buffet.example', api), /credentials\/query\/fragment/)
  }
})
test('local permits separate loopback ports but rejects remote APIs', () => {
  validateRuntimeUrls('local-h2', 'http://localhost:5175', 'http://localhost:8085/api/v1')
  assert.throws(() => validateRuntimeUrls('local-postgres', 'http://localhost:5175', 'https://buffet.example/api/v1'), /loopback/)
})

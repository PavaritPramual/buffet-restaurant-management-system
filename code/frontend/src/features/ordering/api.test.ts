import { afterEach, describe, expect, it, vi } from 'vitest'
import { customerApiClient } from '../../api/client'
import { getCustomerContext, redeemQr } from './api'
import type { SessionContext } from './api'

const sessionA: SessionContext = { sessionId: 1, packageId: 1, tableNumber: 'T01', sessionStatus: 'ACTIVE' }
const sessionB: SessionContext = { sessionId: 2, packageId: 2, tableNumber: 'T02', sessionStatus: 'ACTIVE' }

function deferred<T>() {
  let resolve!: (value: T) => void
  let reject!: (reason: unknown) => void
  const promise = new Promise<T>((done, fail) => { resolve = done; reject = fail })
  return { promise, resolve, reject }
}

afterEach(() => vi.restoreAllMocks())

describe('redeemQr', () => {
  it('uses one POST for the same token while the exchange is pending', async () => {
    const response = deferred<{ data: SessionContext }>()
    const post = vi.spyOn(customerApiClient, 'post').mockImplementation(() => response.promise)
    const first = redeemQr('A')
    const second = redeemQr('A')
    expect(second).toBe(first)
    response.resolve({ data: sessionA })
    expect(await first).toEqual(sessionA)
    expect(post).toHaveBeenCalledTimes(1)
  })

  it('serializes different tokens so B sets the last cookie', async () => {
    const responseA = deferred<{ data: SessionContext }>()
    const post = vi.spyOn(customerApiClient, 'post').mockImplementation((_url, body) =>
      (body as { token: string }).token === 'A' ? responseA.promise : Promise.resolve({ data: sessionB }))
    const first = redeemQr('A')
    const second = redeemQr('B')
    await vi.waitFor(() => expect(post).toHaveBeenCalledTimes(1))
    expect(post).toHaveBeenNthCalledWith(1, '/dining-sessions/qr-exchange', { token: 'A' })
    responseA.resolve({ data: sessionA })
    expect(await first).toEqual(sessionA)
    expect(await second).toEqual(sessionB)
    expect(post).toHaveBeenNthCalledWith(2, '/dining-sessions/qr-exchange', { token: 'B' })
  })

  it('sends a new POST for a previously redeemed token and preserves a 404', async () => {
    const notFound = { response: { status: 404, data: { message: 'QR นี้ใช้ไม่ได้แล้ว' } } }
    const post = vi.spyOn(customerApiClient, 'post')
      .mockResolvedValueOnce({ data: sessionA })
      .mockRejectedValueOnce(notFound)
    expect(await redeemQr('used-token')).toEqual(sessionA)
    await expect(redeemQr('used-token')).rejects.toBe(notFound)
    expect(post).toHaveBeenCalledTimes(2)
  })

  it('queues a new A exchange after A-B-A and coalesces only the latest duplicate', async () => {
    const responseA = deferred<{ data: SessionContext }>()
    const responseB = deferred<{ data: SessionContext }>()
    const usedQr = { response: { status: 404, data: { message: 'QR นี้ใช้ไม่ได้แล้ว' } } }
    let cookieSession = 0
    const post = vi.spyOn(customerApiClient, 'post')
      .mockImplementationOnce(async () => { const response = await responseA.promise; cookieSession = 1; return response })
      .mockImplementationOnce(async () => { const response = await responseB.promise; cookieSession = 2; return response })
      .mockRejectedValueOnce(usedQr)
    const firstA = redeemQr('A')
    const scanB = redeemQr('B')
    const lastA = redeemQr('A')
    const outcome = lastA.then(() => undefined, (error: unknown) => error)
    expect(lastA).not.toBe(firstA)
    expect(redeemQr('A')).toBe(lastA)
    await vi.waitFor(() => expect(post).toHaveBeenCalledTimes(1))
    responseA.resolve({ data: sessionA })
    expect(await firstA).toEqual(sessionA)
    await vi.waitFor(() => expect(post).toHaveBeenCalledTimes(2))
    responseB.resolve({ data: sessionB })
    expect(await scanB).toEqual(sessionB)
    expect(await outcome).toBe(usedQr)
    expect(post.mock.calls.map(([, body]) => (body as { token: string }).token)).toEqual(['A', 'B', 'A'])
    expect(cookieSession).toBe(2)
  })

  it('allows the next queued exchange after a failed QR request', async () => {
    const failure = new Error('network error')
    const post = vi.spyOn(customerApiClient, 'post')
      .mockRejectedValueOnce(failure)
      .mockResolvedValueOnce({ data: sessionB })
    const first = redeemQr('A')
    const next = redeemQr('B')
    await expect(first).rejects.toBe(failure)
    expect(await next).toEqual(sessionB)
    expect(post).toHaveBeenCalledTimes(2)
  })
})

describe('getCustomerContext', () => {
  it('waits for a pending QR exchange before reading its cookie context', async () => {
    const response = deferred<{ data: SessionContext }>()
    vi.spyOn(customerApiClient, 'post').mockReturnValue(response.promise)
    const get = vi.spyOn(customerApiClient, 'get').mockResolvedValue({ data: sessionB })
    const scan = redeemQr('B')
    const context = getCustomerContext()
    expect(get).not.toHaveBeenCalled()
    response.resolve({ data: sessionB })
    await scan
    expect(await context).toEqual(sessionB)
    expect(get).toHaveBeenCalledTimes(1)
  })

  it.each(['success', 'failure'])('retries an obsolete context %s when a newer scan starts', async (outcome) => {
    vi.spyOn(customerApiClient, 'post').mockResolvedValueOnce({ data: sessionA }).mockResolvedValueOnce({ data: sessionB })
    await redeemQr('A')
    const old = deferred<{ data: SessionContext }>()
    const get = vi.spyOn(customerApiClient, 'get').mockReturnValueOnce(old.promise).mockResolvedValueOnce({ data: sessionB })
    const context = getCustomerContext()
    await vi.waitFor(() => expect(get).toHaveBeenCalledTimes(1))
    const scanB = redeemQr('B')
    if (outcome === 'success') old.resolve({ data: sessionA }); else old.reject(new Error('obsolete request failed'))
    await scanB
    expect(await context).toEqual(sessionB)
    expect(get).toHaveBeenCalledTimes(2)
  })

  it('preserves a failed latest scan and retries its exchange only when requested', async () => {
    const failure = new Error('QR network error')
    const post = vi.spyOn(customerApiClient, 'post').mockRejectedValueOnce(failure).mockResolvedValueOnce({ data: sessionB })
    const get = vi.spyOn(customerApiClient, 'get').mockResolvedValue({ data: sessionB })
    await expect(redeemQr('B')).rejects.toBe(failure)
    await expect(getCustomerContext()).rejects.toBe(failure)
    expect(get).not.toHaveBeenCalled()
    expect(await getCustomerContext({ retryFailedQr: true })).toEqual(sessionB)
    expect(post).toHaveBeenCalledTimes(2)
    expect(get).toHaveBeenCalledTimes(1)
  })

  it('follows a newer successful scan when an obsolete exchange fails', async () => {
    const response = deferred<{ data: SessionContext }>()
    vi.spyOn(customerApiClient, 'post').mockReturnValueOnce(response.promise).mockResolvedValueOnce({ data: sessionB })
    const get = vi.spyOn(customerApiClient, 'get').mockResolvedValue({ data: sessionB })
    const scanA = redeemQr('A')
    const context = getCustomerContext()
    const scanB = redeemQr('B')
    const failure = new Error('obsolete exchange failed')
    const rejected = expect(scanA).rejects.toBe(failure)
    response.reject(failure)
    await rejected
    await scanB
    expect(await context).toEqual(sessionB)
    expect(get).toHaveBeenCalledTimes(1)
  })
})

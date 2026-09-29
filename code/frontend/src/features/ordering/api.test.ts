import { afterEach, describe, expect, it, vi } from 'vitest'
import { customerApiClient } from '../../api/client'
import { redeemQr } from './api'
import type { SessionContext } from './api'

const sessionA: SessionContext = { sessionId: 1, packageId: 1, tableNumber: 'T01', sessionStatus: 'ACTIVE' }
const sessionB: SessionContext = { sessionId: 2, packageId: 2, tableNumber: 'T02', sessionStatus: 'ACTIVE' }

function deferred<T>() {
  let resolve!: (value: T) => void
  const promise = new Promise<T>((done) => { resolve = done })
  return { promise, resolve }
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
})

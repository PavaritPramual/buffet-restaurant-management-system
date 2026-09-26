export function getApiError(error: unknown) {
  if (typeof error === 'object' && error && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response
    if (response?.data?.message) return response.data.message
  }
  return 'เชื่อมต่อระบบไม่สำเร็จ กรุณาลองใหม่อีกครั้ง'
}

// @vitest-environment jsdom
import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'
import { StatusBadge } from './index'

afterEach(cleanup)

describe('StatusBadge', () => {
  it('renders domain-owned labels with a shared semantic tone', () => {
    const { rerender } = render(<StatusBadge tone="success">โต๊ะว่าง</StatusBadge>)
    expect(screen.getByText('โต๊ะว่าง').className).toContain('tone-success')

    rerender(<StatusBadge tone="danger">สต็อกใกล้หมด</StatusBadge>)
    expect(screen.getByText('สต็อกใกล้หมด').className).toContain('tone-danger')
  })
})

import { Button, SelectField, TextField } from '../../components/common'
import type { RecipeStockOption, StockUsageInput } from './api'

export default function MenuStockUsageFields({ enabled, usage, stocks, disabled, onChange }: {
  enabled: boolean; usage: StockUsageInput[]; stocks: RecipeStockOption[]; disabled: boolean
  onChange: (enabled: boolean, usage: StockUsageInput[]) => void
}) {
  function update(index: number, patch: Partial<StockUsageInput>) {
    onChange(enabled, usage.map((entry, position) => position === index ? { ...entry, ...patch } : entry))
  }
  return <fieldset className="package-options" disabled={disabled}>
    <legend>วัตถุดิบต่อหนึ่งเสิร์ฟ</legend>
    <SelectField label="การหักสต๊อก" value={enabled ? 'on' : 'off'} onChange={event => onChange(event.target.value === 'on', [])}>
      <option value="off">ไม่หักสต๊อกอัตโนมัติ</option><option value="on">หักสต๊อกอัตโนมัติ</option>
    </SelectField>
    {enabled && <>
      <p>หักตามสูตรตอนสั่ง เมื่อครัวเริ่มทำ หากวัตถุดิบไม่พอออเดอร์จะยังไม่เริ่มทำ</p>
      {usage.map((entry, index) => {
        const selected = stocks.find(stock => stock.id === entry.stockItemId)
        return <div className="recipe-row" key={index}>
          <SelectField label={`วัตถุดิบ ${index + 1}`} required value={entry.stockItemId || ''} onChange={event => update(index, { stockItemId: Number(event.target.value) })}>
            <option value="">เลือกวัตถุดิบ</option>
            {stocks.filter(stock => stock.id === entry.stockItemId || (stock.active && !usage.some(other => other.stockItemId === stock.id))).map(stock => <option key={stock.id} value={stock.id}>{stock.name} ({stock.unit}){!stock.active ? ' — ใช้งานไม่ได้' : ''}</option>)}
          </SelectField>
          <TextField label={`ปริมาณต่อเสิร์ฟ ${index + 1}${selected ? ` (${selected.unit})` : ''}`} type="number" min="0.001" max="999999999.999" step="0.001" required value={entry.quantityPerServing || ''} onChange={event => update(index, { quantityPerServing: Number(event.target.value) })} />
          <Button type="button" variant="ghost" onClick={() => onChange(enabled, usage.filter((_, position) => position !== index))}>นำวัตถุดิบ {index + 1} ออก</Button>
        </div>
      })}
      <Button type="button" variant="secondary" disabled={!stocks.some(stock => stock.active && !usage.some(entry => entry.stockItemId === stock.id))} onClick={() => onChange(enabled, [...usage, { stockItemId: 0, quantityPerServing: 0 }])}>เพิ่มวัตถุดิบ</Button>
      {!stocks.some(stock => stock.active) && <p>ยังไม่มีวัตถุดิบที่เปิดใช้งาน กรุณาเพิ่มหรือเปิดใช้งานในหน้าสต๊อกก่อน</p>}
    </>}
  </fieldset>
}

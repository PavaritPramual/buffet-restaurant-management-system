# Git audit — Step 3

ตรวจ 7 ตุลาคม 2026 ก่อน commits เอกสารชุดนี้ Baseline develop `472fba4f25a27fa2e3cd1e1213151ce971646f3a`; repository เป็น public และ default branch เป็น main ตาม GitHub API ไม่ได้ยืนยันสิทธิ์เข้าถึงของบัญชีอาจารย์แต่ละคน

## วิธีนับ

อ่าน GitHub branches/PR/reviews และ `git rev-list --no-merges` บน develop กับ personal branches ทั้งห้า นับผู้เขียนตาม author name ที่จับคู่กับบัญชี GitHub ใน merged history ไม่นับ merge commits และ commit ที่ไม่มี changed paths อ่านวันที่/ข้อความ/ไฟล์ใน CSV เพื่อประเมินความหมาย ไม่ใช้จำนวนเป็นใบรับรอง meaningful ≥15 โดยอัตโนมัติ ไม่เปิดเผย email

| สมาชิก / GitHub | Personal branch / head ก่อนงานนี้ | Candidates ใน develop | Candidates ของตนที่ reachable จาก personal branch | รวม unique ของตนจาก develop + ทั้งห้า branches | ช่วง author dates | ผลต่อเกณฑ์15 |
|---|---|---:|---:|---:|---|---|
| ปวริศช์ / PavaritPramual | `pavarit_673380278-9_01` / [b3d767c](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/b3d767c315c6bda0ae2367912ba33b8b847b3a04) | 44 | 44 | 44 | 2026-09-18 – 2026-10-06 | จำนวน candidate ถึง15 แต่ยังต้องประเมินความหมาย/การกระจายเวลา |
| ศิระพัทธ์ / sirapatw-sys | `sirapat_673380293-3_01` / [617d742](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/617d7423297f3881738d303eedbaada33ebfc4d6) | 22 | 22 | 22 | 2026-09-20 – 2026-10-06 | จำนวน candidate ถึง15 แต่ยังต้องประเมินความหมาย/การกระจายเวลา |
| ศรัณย์ / sarunph-ctrl | `sarun_673380515-1_02` / [b3a2818](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/b3a2818beb39fdaa2d1a5295d484640461d14cff) | 3 | 3 | 3 | 2026-09-18 – 2026-09-28 | ยังต่ำกว่า15 ขาดอย่างน้อย12; ไม่สร้าง commit เติมยอด |
| ธีรเมธ / kojidesu01 | `teeramet_673380273-9_02` / [f387082](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/f387082e2292d1b115b626aab5ea76fcd6b55a8f) | 8 | 8 | 8 | 2026-09-20 – 2026-10-05 | ยังต่ำกว่า15 ขาดอย่างน้อย7; ไม่สร้าง commit เติมยอด |
| เมธัส / methus-bit | `methus_673380300-2_01` / [8ac7978](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/8ac7978ea205726e4f8deb07da537da18a46ea99) | 5 | 5 | 5 | 2026-09-18 – 2026-10-02 | ยังต่ำกว่า15 ขาดอย่างน้อย10; ไม่สร้าง commit เติมยอด |

ทุก branch ตามชื่อที่กำหนดมีอยู่จริง ทั้งนี้ reachable หมายถึงประวัติที่ branch รับเข้ามาด้วย ไม่ใช่ทุก commit บน branch เป็นผลงานเจ้าของ branch และช่วงวันที่ไม่พิสูจน์การกระจายเวลา ต้องอ่าน CSV รายการต่อรายการ

## PR และ reviewer history

| PR | ผู้เปิด | สถานะ | Head branch | Reviews ที่บันทึก |
|---|---|---|---|---|
| [#1](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/1) | PavaritPramual | MERGED | `pavarit_673380278-9_01` | sarunph-ctrl:APPROVED |
| [#2](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/2) | sarunph-ctrl | CLOSED | `develop` | ไม่มี formal review |
| [#3](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/3) | methus-bit | CLOSED | `methus_673380300-2_01` | ไม่มี formal review |
| [#4](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/4) | methus-bit | MERGED | `methus_673380300-2_01` | PavaritPramual:APPROVED; copilot-pull-request-reviewer:COMMENTED; PavaritPramual:COMMENTED |
| [#5](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/5) | sarunph-ctrl | CLOSED | `sarun_673380515-1_02` | copilot-pull-request-reviewer:COMMENTED |
| [#6](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/6) | sirapatw-sys | CLOSED | `sirapat_673380293-3_01` | ไม่มี formal review |
| [#7](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/7) | sarunph-ctrl | MERGED | `sarun_673380515-1_02` | PavaritPramual:APPROVED; copilot-pull-request-reviewer:COMMENTED |
| [#8](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/8) | sirapatw-sys | MERGED | `sirapat_673380293-3_01` | PavaritPramual:APPROVED; copilot-pull-request-reviewer:COMMENTED |
| [#9](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/9) | PavaritPramual | MERGED | `pavarit_673380278-9_01` | copilot-pull-request-reviewer:COMMENTED; sirapatw-sys:CHANGES_REQUESTED; copilot-pull-request-reviewer:COMMENTED; sirapatw-sys:APPROVED |
| [#10](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/10) | kojidesu01 | MERGED | `teeramet_673380273-9_02` | copilot-pull-request-reviewer:COMMENTED; PavaritPramual:CHANGES_REQUESTED; copilot-pull-request-reviewer:COMMENTED; PavaritPramual:CHANGES_REQUESTED; copilot-pull-request-reviewer:COMMENTED; PavaritPramual:CHANGES_REQUESTED; PavaritPramual:APPROVED |
| [#11](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/11) | sirapatw-sys | CLOSED | `sirapat_673380293-3_01` | PavaritPramual:CHANGES_REQUESTED |
| [#12](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/12) | PavaritPramual | MERGED | `pavarit_673380278-9_01` | methus-bit:CHANGES_REQUESTED; methus-bit:DISMISSED; sarunph-ctrl:CHANGES_REQUESTED; sarunph-ctrl:CHANGES_REQUESTED; methus-bit:CHANGES_REQUESTED; sarunph-ctrl:DISMISSED; sarunph-ctrl:CHANGES_REQUESTED; methus-bit:COMMENTED; methus-bit:APPROVED; sarunph-ctrl:APPROVED |
| [#13](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/13) | sirapatw-sys | MERGED | `sirapat_673380293-3_01` | PavaritPramual:DISMISSED; sarunph-ctrl:CHANGES_REQUESTED; sarunph-ctrl:APPROVED; PavaritPramual:APPROVED |
| [#14](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/14) | sarunph-ctrl | MERGED | `sarun_673380515-1_02` | PavaritPramual:CHANGES_REQUESTED; sirapatw-sys:DISMISSED; PavaritPramual:CHANGES_REQUESTED; PavaritPramual:APPROVED; sirapatw-sys:APPROVED |
| [#15](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/15) | PavaritPramual | MERGED | `codex/system-design-docs` | sirapatw-sys:APPROVED |
| [#16](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/16) | PavaritPramual | MERGED | `pavarit_673380278-9_01` | sarunph-ctrl:CHANGES_REQUESTED; sirapatw-sys:CHANGES_REQUESTED; kojidesu01:COMMENTED; methus-bit:CHANGES_REQUESTED; sirapatw-sys:CHANGES_REQUESTED; kojidesu01:COMMENTED; sirapatw-sys:DISMISSED; sarunph-ctrl:CHANGES_REQUESTED; sarunph-ctrl:APPROVED; methus-bit:APPROVED |
| [#17](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/17) | methus-bit | MERGED | `methus_673380300-2_01` | sarunph-ctrl:CHANGES_REQUESTED; PavaritPramual:CHANGES_REQUESTED; sirapatw-sys:CHANGES_REQUESTED; PavaritPramual:CHANGES_REQUESTED; sirapatw-sys:CHANGES_REQUESTED; sarunph-ctrl:DISMISSED; PavaritPramual:CHANGES_REQUESTED; sirapatw-sys:CHANGES_REQUESTED; sarunph-ctrl:DISMISSED; sirapatw-sys:APPROVED; PavaritPramual:APPROVED |
| [#18](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/18) | sirapatw-sys | MERGED | `sirapat_673380293-3_01` | PavaritPramual:APPROVED; sarunph-ctrl:APPROVED; methus-bit:APPROVED |
| [#19](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/19) | kojidesu01 | MERGED | `teeramet_673380273-9_02` | sirapatw-sys:CHANGES_REQUESTED; PavaritPramual:CHANGES_REQUESTED; sarunph-ctrl:CHANGES_REQUESTED; methus-bit:CHANGES_REQUESTED; PavaritPramual:APPROVED; sirapatw-sys:APPROVED; sarunph-ctrl:APPROVED; methus-bit:APPROVED |
| [#20](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/20) | PavaritPramual | CLOSED | `pavarit_673380278-9_01` | ไม่มี formal review |
| [#21](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/21) | PavaritPramual | MERGED | `pavarit_673380278-9_01` | sirapatw-sys:CHANGES_REQUESTED; kojidesu01:CHANGES_REQUESTED; sarunph-ctrl:CHANGES_REQUESTED; sirapatw-sys:DISMISSED; kojidesu01:DISMISSED; sarunph-ctrl:APPROVED; sirapatw-sys:APPROVED |
| [#22](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/22) | sirapatw-sys | MERGED | `sirapat_673380293-3_01` | PavaritPramual:APPROVED; sarunph-ctrl:APPROVED |

PR ปิดโดยไม่ merge เช่น #11/#20 เป็นประวัติ ไม่อ้างว่าโค้ดเข้า develop แล้ว Reviews เก่าไม่รับรอง SHA ใหม่ ดู approval/latest-head ใน PR ก่อน merge จริง

## สิ่งที่สมาชิกต้องยืนยัน

- [ ] ทุกคนตรวจบัญชี GitHub/author identity ของตนจาก CSV และ PR
- [ ] ทุกคนอธิบายโค้ดจริงและ commit ที่ถือว่ามีความหมาย ไม่ใช้การนับล้วน
- [ ] สมาชิกที่ยังต่ำกว่า15 รายงานข้อขาดจริงและทำงาน Final ที่รับผิดชอบเป็นกลุ่มงานตามธรรมชาติ ไม่แก้ประวัติหรือสร้าง commit ย่อยเพื่อเติมจำนวน
- [ ] ตรวจ audit ใหม่หลัง Stock/Profile/deploy/docs merge ก่อน release
- [ ] ยืนยันบัญชีอาจารย์เปิด repository/public URLs ได้ และ release PR มี review

หลักฐาน sanitized: [commit inventory](../../test/evidence/pavarit-step3-docs-2026-10-07/git-commits.csv) บันทึก SHA ผู้เขียน วันที่ ข้อความ และ changed paths โดยไม่เก็บ secrets; head ของ branch ปวริศช์ในตารางเป็น remote ก่อน sync งานนี้ ไม่รวม commits เอกสารที่กำลังทำ

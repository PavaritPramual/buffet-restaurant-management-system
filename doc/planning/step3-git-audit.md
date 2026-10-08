# Git audit — Step 3

## เกณฑ์ปัจจุบัน — ขั้นต่ำ 5 meaningful commits ต่อคน

ปวริศช์แจ้งวันที่ 7 ตุลาคม 2026 ว่าอาจารย์ปรับขั้นต่ำจาก 15 เป็น 5 commits ต่อคน ใช้เกณฑ์ 5 ตาม [บันทึกเกณฑ์ที่เปลี่ยน](course-criteria-updates.md); ไม่แก้ใบงานต้นฉบับย้อนหลัง

### Snapshot หลัง merge `develop adc5798` — 8 ตุลาคม 2026

ตรวจ local `HEAD`, `origin/develop` และประวัติ `origin/develop` หลัง fetch: ทั้งสอง ref อยู่ที่ [`adc5798`](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/adc5798b05279840dc6178f4291278467c929791). นับ non-merge commits ตาม author name ที่ map กับบัญชี GitHub ใน develop history ณ revision นี้; แสดงเป็น **author candidates** ไม่ใช่การรับรองว่าแต่ละ commit meaningful หรือเป็นงานที่เจ้าของอธิบายได้ทั้งหมด

| สมาชิก / GitHub author | Personal branch | Candidate non-merge commits ใน `develop` | Latest author commit checked | ช่วง author dates | สถานะเทียบขั้นต่ำ 5 |
|---|---|---:|---|---|---|
| ปวริศช์ / PavaritPramual | `pavarit_673380278-9_01` | 65 | [5939ecd](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/5939ecd638ac8f49301a324aa4a8784170ed0566) | 2026-09-18 – 2026-10-08 | ถึงจำนวน candidate; owner ยืนยัน identity/meaningfulness/การกระจายเวลา |
| ศิระพัทธ์ / Sirapat Wongwiwatseree (`sirapatw-sys`) | `sirapat_673380293-3_01` | 30 | [7bde17e](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/7bde17e466b1539b988e26b505fa0f8e0fa6a74b) | 2026-09-20 – 2026-10-08 | ถึงจำนวน candidate; owner ยืนยัน identity/meaningfulness/การกระจายเวลา |
| ศรัณย์ / sarunph-ctrl | `sarun_673380515-1_02` | 6 | [2f5752d](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/2f5752d6959d2a6029f453a97dcd4cf068ab1ff1) | 2026-09-18 – 2026-10-08 | ถึงจำนวน candidate; owner ยืนยัน identity/meaningfulness/การกระจายเวลา |
| ธีรเมธ / kojidesu01 | `teeramet_673380273-9_02` | 9 | [c7e704f](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/c7e704fa3978a78ad6d078c55ff1f2993cc41ca1) | 2026-09-20 – 2026-10-07 | ถึงจำนวน candidate; owner ยืนยัน identity/meaningfulness/การกระจายเวลา |
| เมธัส / methus-bit | `methus_673380300-2_01` | 11 | [5a75d42](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/5a75d42d280f0688edbd1ccd3d231d5353b3878e) | 2026-09-18 – 2026-10-08 | ถึงจำนวน candidate; owner ยืนยัน identity/meaningfulness/การกระจายเวลา |

ผลนับมาจาก `git log origin/develop --no-merges`; commit subjects/paths ต้องอ่านยืนยันเป็นรายคน และช่วง author dates ไม่พิสูจน์การกระจายเวลา การ merge เข้า develop ไม่ใช่การรับรอง contribution ของทุก author โดยอัตโนมัติ จึงยังไม่ติ๊กเกณฑ์รายคนจนกว่าเจ้าของแต่ละคนจะยืนยันและอธิบายงานของตนได้

สำหรับเจ้าของ branch ปัจจุบัน (ศรัณย์) ตรวจเนื้อหา commit candidates แล้ว พบอย่างน้อยห้ากลุ่มงานที่อาจใช้ยืนยัน meaningfulness: [939f7d9](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/939f7d9685d54d7bd35f449e681c40682a65201b) เพิ่ม API/DTO/JSON conventions, [1216fa2](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/1216fa2cda7791f0f776f4a5dd4baeab95e7ddd5) แก้ convention/enum/error documentation, [b3a2818](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/b3a2818beb39fdaa2d1a5295d484640461d14cff) เพิ่ม Kitchen/Staff authorization, state flow และ tests, [107512d](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/107512d1e568982a3860ce03d55a4704da87dd0e) เพิ่ม API contracts/OpenAPI integration tests และ [76eccee](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/76eccee2ea932e2cae6132c96d0c80e5b572dfd4) ปรับ API/State review evidence หลายไฟล์. ใช้เป็น shortlist ให้เจ้าของยืนยัน—not automatic certification; [2f5752d](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/2f5752d6959d2a6029f453a97dcd4cf068ab1ff1) เป็น author commit ล่าสุดอีกหนึ่งรายการ

PR/CI ล่าสุดที่ตรวจ: [PR #28](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/28) Stock/Profile merged หลัง approvals; [PR #31](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/31) และ [#32](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/32) merged/reviewed; [CI run 37738400052](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37738400052) ที่ `adc5798` ผ่าน Backend/PostgreSQL 348/348, Frontend 121/121, URL guards 6/6, lint/build. ไม่มีหลักฐาน public deployment หรือ release จาก CI นี้

ตาราง/จำนวน audit ก่อนหน้าในหัวข้อด้านล่างเป็น **historical snapshots** เท่านั้น ไม่ใช่สถานะปัจจุบัน

### Historical audit snapshot — 7 ตุลาคม (เกณฑ์ 15; superseded)

ตารางและยอดด้านล่างเป็น snapshot ก่อน PR #24/#25 และใช้เกณฑ์เดิม 15; เก็บไว้เพื่อประวัติเท่านั้น ไม่ใช่สถานะปัจจุบัน. Repository เป็น public และ default branch เป็น main ตาม GitHub API แต่ไม่ได้ยืนยันสิทธิ์เข้าถึงของบัญชีอาจารย์แต่ละคน

#### Historical method

Snapshot เดิมตรวจ GitHub branches/PR/reviews และ non-merge history ของ develop/personal branches; counts เป็น candidates ไม่ยืนยัน meaningfulness/owner identity/branch-exclusive work/การกระจายเวลา. เก็บวิธี/จำนวนเดิมไว้เป็นประวัติ ไม่ใช้แทนผลปัจจุบัน

| สมาชิก / GitHub | Personal branch / head ก่อนงานนี้ | Candidates ใน develop | Candidates ของตนที่ reachable จาก personal branch | รวม unique ของตนจาก develop + ทั้งห้า branches | ช่วง author dates | ผลต่อเกณฑ์15 |
|---|---|---:|---:|---:|---|---|
| ปวริศช์ / PavaritPramual | `pavarit_673380278-9_01` / [b3d767c](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/b3d767c315c6bda0ae2367912ba33b8b847b3a04) | 44 | 44 | 44 | 2026-09-18 – 2026-10-06 | จำนวน candidate ถึง15 แต่ยังต้องประเมินความหมาย/การกระจายเวลา |
| ศิระพัทธ์ / sirapatw-sys | `sirapat_673380293-3_01` / [617d742](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/617d7423297f3881738d303eedbaada33ebfc4d6) | 22 | 22 | 22 | 2026-09-20 – 2026-10-06 | จำนวน candidate ถึง15 แต่ยังต้องประเมินความหมาย/การกระจายเวลา |
| ศรัณย์ / sarunph-ctrl | `sarun_673380515-1_02` / [b3a2818](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/b3a2818beb39fdaa2d1a5295d484640461d14cff) | 3 | 3 | 3 | 2026-09-18 – 2026-09-28 | ยังต่ำกว่า15 ขาดอย่างน้อย12; ไม่สร้าง commit เติมยอด |
| ธีรเมธ / kojidesu01 | `teeramet_673380273-9_02` / [f387082](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/f387082e2292d1b115b626aab5ea76fcd6b55a8f) | 8 | 8 | 8 | 2026-09-20 – 2026-10-05 | ยังต่ำกว่า15 ขาดอย่างน้อย7; ไม่สร้าง commit เติมยอด |
| เมธัส / methus-bit | `methus_673380300-2_01` / [8ac7978](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/8ac7978ea205726e4f8deb07da537da18a46ea99) | 5 | 5 | 5 | 2026-09-18 – 2026-10-02 | ยังต่ำกว่า15 ขาดอย่างน้อย10; ไม่สร้าง commit เติมยอด |

ทุก branch ตามชื่อที่กำหนดมีอยู่จริง ทั้งนี้ reachable หมายถึงประวัติที่ branch รับเข้ามาด้วย ไม่ใช่ทุก commit บน branch เป็นผลงานเจ้าของ branch และช่วงวันที่ไม่พิสูจน์การกระจายเวลา ต้องอ่าน CSV รายการต่อรายการ

## PR และ reviewer history

### PR/review/merge ที่ตรวจล่าสุด — 8 ตุลาคม 2026

| PR | เจ้าของ/branch | สถานะล่าสุด | Review ที่ตรวจได้ | หลักฐาน/หมายเหตุ |
|---|---|---|---|---|
| [#24](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/24) | ปวริศช์ / `pavarit_673380278-9_01` | Merged | PR closed as merged | เข้า `develop`; architecture/docs follow-up ถูกแยกใน PR #25 |
| [#25](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/25) | ปวริศช์ / `pavarit_673380278-9_01` | Merged | ศิระพัทธ์, ศรัณย์, ธีรเมธ, เมธัส: APPROVED | Auth/Stock/Billing contracts, injection, state registry; CI ของ head ผ่าน |
| [#26](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/26) | ศรัณย์ / `sarun_673380515-1_02` | Merged | Review thread ระบุข้อแก้ก่อน merge | API/State docs/tests; current evidence supplemented by #32 |
| [#27](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/27) | ธีรเมธ / `teeramet_673380273-9_02` | Merged | Review requested from ศิระพัทธ์/ปวริศช์; merged | Thai unpaid-close response; don't infer formal APPROVED absent review record |
| [#28](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/28) | เมธัส / `methus_673380300-2_01` | Merged | ปวริศช์, ศรัณย์, ศิระพัทธ์: APPROVED | Stock/Profile V15; candidate tests and migration evidence; public/release gates remain |
| [#29](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/29) | ศิระพัทธ์ / `sirapat_673380293-3_01` | Merged | PR merged; follow-up recorded in #32 | API/State regression evidence |
| [#30](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/30) | Copilot / CI fix | Closed, not merged | N/A | Do not cite as implemented; subsequent V15 test expectation is in merged feature/test history |
| [#31](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/31) | ปวริศช์ / `pavarit_673380278-9_01` | Merged | ศิระพัทธ์ requested changes then APPROVED; ศรัณย์ APPROVED at final head | HTTP-status/diagram correction verified before merge |
| [#32](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/32) | ศิระพัทธ์ / `codex/sirapat-step3-followup` | Merged | ปวริศช์ and ศรัณย์: APPROVED | Develop regression, candidate separation, artifacts; CI run 37735263342 passed |

PR states/reviews verified from GitHub on 8 October. A merge without a visible APPROVED review is recorded as merged, not upgraded to reviewer approval. Approvals are scoped to the PR head and do not certify deployment/release.

### Historical PR/reviewer history (#1–#22)

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
- [ ] ทุกคนตรวจให้มีอย่างน้อย 5 commits ที่ตนยืนยันว่า meaningful จริง; จำนวน candidates ข้างต้นยังไม่ใช่การรับรอง และไม่แก้ประวัติ/สร้าง commits ย่อยเพื่อเติมยอด
- [ ] หลังเอกสารชุดนี้ merge และก่อน release ให้ตรวจ audit ซ้ำ; Stock/Profile PR #28 merge แล้ว แต่ V15/public/release gates ยังเปิด
- [ ] ยืนยันบัญชีอาจารย์เปิด repository/public URLs ได้ และ release PR มี review

หลักฐาน sanitized ใน [commit inventory 7 ต.ค.](../../test/evidence/pavarit-step3-docs-2026-10-07/git-commits.csv) เป็น historical snapshot ก่อน PR #24 ไม่ใช่ source ของยอดปัจจุบัน; snapshot ปัจจุบันอ้าง `origin/develop adc5798` และ latest author commits ที่ลิงก์ไว้ด้านบน

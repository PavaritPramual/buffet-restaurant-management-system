# Git audit — Step 3

## เกณฑ์ปัจจุบัน — ขั้นต่ำ 5 meaningful commits ต่อคน

ปวริศช์แจ้งวันที่ 7 ตุลาคม 2026 ว่าอาจารย์ปรับขั้นต่ำจาก 15 เป็น 5 commits ต่อคน ใช้เกณฑ์ 5 ตาม [บันทึกเกณฑ์ที่เปลี่ยน](course-criteria-updates.md); ไม่แก้ใบงานต้นฉบับย้อนหลัง

### Snapshot refs ล่าสุด — 8 ตุลาคม 2026, 21:55 ICT

Fetch remote refs จาก Git วันที่ 8 ต.ค. 2026 เวลา 21:55 ICT. `origin/develop` และ local `HEAD` อยู่ที่ [`adc5798`](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/adc5798b05279840dc6178f4291278467c929791); personal branches ตรวจที่ SHA ใน [branch-head CSV](../../test/evidence/sarun-git-audit-2026-10-08/personal-branch-heads.csv). สร้าง [non-merge commit inventory](../../test/evidence/sarun-git-audit-2026-10-08/develop-non-merge-commits.csv) จาก `git log origin/develop --no-merges --name-only`; มี 121 commits ที่มี changed paths, ไม่เก็บ email. นับ author candidates จาก Git author names ที่ map กับ PR/account login; account/ownership/meaningfulness ยังรอสมาชิกยืนยันรายบุคคล.

| สมาชิก / GitHub account | Personal branch HEAD ณ snapshot | Author candidates: branch history | Author candidates: `develop` | Ahead/behind `develop` | ข้อสรุปที่ยืนยันได้ |
|---|---|---:|---:|---:|---|
| ปวริศช์ / PavaritPramual | [`5145870`](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/5145870bffcc5f8f8e4e5d993da5552d0ff672c1) | 67 | 65 | +2 / 0 | ≥5 candidates; PR #33 มี reviewer approvals แต่ยัง open |
| ศิระพัทธ์ / sirapatw-sys | [`600013a`](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/600013a56f267f474d5c787fab84ce80f08f4a50) | 25 | 30 | 0 / 28 | ≥5 candidates; personal branch ล้าหลัง `develop` 28 commits |
| ศรัณย์ / sarunph-ctrl | [`e0a1d73`](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/e0a1d7362e3d96fcb931e4950d6698ac9f056c3e) | 7 | 6 | +1 / 0 | ≥5 candidates; PR #34 รอ reviewers |
| ธีรเมธ / kojidesu01 | [`304dc07`](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/304dc07fd51b0c8f7afc511efc392fedb353f288) | 14 | 9 | +5 / 0 | ≥5 candidates; branch มี commits ใหม่หลัง baseline `develop`; ตรวจ PR linkage เพิ่มก่อน merge |
| เมธัส / methus-bit | [`adc5798`](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/adc5798b05279840dc6178f4291278467c929791) | 11 | 11 | 0 / 0 | ≥5 candidates; branch เท่ากับ `develop` |

จำนวนในสองคอลัมน์นับ commit ที่ Git author name ตรงกับชื่อในตารางและมี file paths; candidate ใน branch history อาจรวม commit ที่ไม่อยู่ใน `develop` หรือ commit ที่ merge เข้ามา จึงห้ามบวกสองคอลัมน์เข้าด้วยกันหรือใช้แทนจำนวน meaningful commits ที่สมาชิกยืนยันแล้ว. Ahead/behind เป็น graph divergence เทียบ `origin/develop`; ไม่มีการตีความเป็น contribution. ช่วง author dates/การกระจายเวลาและความเป็นเจ้าของต้องตรวจจาก inventory และสมาชิกเจ้าของยืนยันเอง.

สำหรับศรัณย์ ตรวจไฟล์/หัวข้อจาก candidates ใน inventory แล้วอย่างน้อย 5 commits มีขอบเขตงานที่แยกได้: [939f7d9](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/939f7d9685d54d7bd35f449e681c40682a65201b) API/DTO/JSON conventions, [1216fa2](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/1216fa2cda7791f0f776f4a5dd4baeab95e7ddd5) convention/enum/error docs, [b3a2818](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/b3a2818beb39fdaa2d1a5295d484640461d14cff) Kitchen/Staff authorization/state/tests, [107512d](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/107512d1e568982a3860ce03d55a4704da87dd0e) API contracts/OpenAPI tests, [76eccee](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/76eccee2ea932e2cae6132c96d0c80e5b572dfd4) API/State evidence, and [2f5752d](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/2f5752d6959d2a6029f453a97dcd4cf068ab1ff1) State-slide/audit updates. This is a candidate list for owner confirmation, not automatic certification.

PR/review/CI ล่าสุด: [PR #33](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/33) ยัง open; ศิระพัทธ์และศรัณย์ approve ที่ head `5145870`, CI `37777850248` ผ่าน synthetic merge กับ `adc5798`. [PR #34](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/34) ยัง open; ปวริศช์/ศิระพัทธ์ถูกขอ review แต่ยังไม่มี submitted reviews; CI `37794743720` ผ่านทั้งสอง jobs. [CI ของ develop](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37738400052) ที่ `adc5798` ผ่าน Backend/PostgreSQL 348/348, Frontend 121/121, URL guards 6/6, lint/build. ไม่มีหลักฐาน public deployment หรือ release จาก CI เหล่านี้

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
| [#33](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/33) | ปวริศช์ / `pavarit_673380278-9_01` | Open | ศิระพัทธ์ and ศรัณย์: APPROVED at `5145870`; PR remains open | Integrated JPA/docs/regression evidence; CI run 37777850248 passed on synthetic merge with `adc5798`; approval scope excludes public/release |
| [#34](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/34) | ศรัณย์ / `sarun_673380515-1_02` | Open | Review requested from ปวริศช์ and ศิระพัทธ์; no submitted review found at snapshot | CI run 37794743720 passed both jobs; PR awaits reviewer feedback |

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

- [ ] ปวริศช์ยืนยัน GitHub author/account, งานที่เป็นเจ้าของ, อย่างน้อย 5 meaningful commits, ช่วงเวลา/การกระจายงาน และการเข้าถึง repo/evidence
- [ ] ศิระพัทธ์ยืนยัน GitHub author/account, งานที่เป็นเจ้าของ, อย่างน้อย 5 meaningful commits, ช่วงเวลา/การกระจายงาน และการเข้าถึง repo/evidence
- [ ] ศรัณย์ยืนยัน GitHub author/account, งานที่เป็นเจ้าของ, อย่างน้อย 5 meaningful commits, ช่วงเวลา/การกระจายงาน และการเข้าถึง repo/evidence
- [ ] ธีรเมธยืนยัน GitHub author/account, งานที่เป็นเจ้าของ, อย่างน้อย 5 meaningful commits, ช่วงเวลา/การกระจายงาน และการเข้าถึง repo/evidence
- [ ] เมธัสยืนยัน GitHub author/account, งานที่เป็นเจ้าของ, อย่างน้อย 5 meaningful commits, ช่วงเวลา/การกระจายงาน และการเข้าถึง repo/evidence
- [ ] reviewer ปวริศช์ตรวจ Architecture/ความถูกต้อง และศิระพัทธ์ตรวจความครบถ้วน/evidence ของ PR #34; แก้จนมี formal approval
- [ ] หลัง PR/doc merge และก่อน release ให้ตรวจ audit ซ้ำ; Stock/Profile PR #28 merge แล้ว แต่ V15/public/release gates ยังเปิด
- [ ] ยืนยันบัญชีอาจารย์เปิด repository/public URLs ได้ และ release PR มี review

แบบฟอร์มสถานะเริ่มต้นของคำยืนยัน: [member-confirmations.csv](../../test/evidence/sarun-git-audit-2026-10-08/member-confirmations.csv). ทุกคนยัง `PENDING` ใน snapshot นี้; ห้ามทำเครื่องหมายผ่านจาก GitHub account/PR author metadata แทนคำยืนยันของเจ้าตัว. หลักฐาน sanitized ใน [commit inventory 7 ต.ค.](../../test/evidence/pavarit-step3-docs-2026-10-07/git-commits.csv) เป็น historical snapshot ไม่ใช้แทน inventory ปัจจุบัน: [develop commits](../../test/evidence/sarun-git-audit-2026-10-08/develop-non-merge-commits.csv), [branch refs](../../test/evidence/sarun-git-audit-2026-10-08/personal-branch-heads.csv), [method/limits](../../test/evidence/sarun-git-audit-2026-10-08/README.md).

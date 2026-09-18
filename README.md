# Buffet Restaurant Management System

ระบบจัดการร้านอาหารบุฟเฟต์สำหรับการเปิดโต๊ะ สั่งอาหาร ติดตามงานครัว ชำระเงิน และจัดการสต็อก พัฒนาด้วย Spring Boot, React และ PostgreSQL ตาม Layered Architecture

## สมาชิกและ Branch

| สมาชิก | รหัสนักศึกษา | Section | Branch | Feature Owner |
|---|---:|---:|---|---|
| ปวริศช์ ประมวล | 673380278-9 | 01 | `pavarit_673380278-9_01` | Table, Dining Session, Buffet Package/Soup |
| ศิระพัทธ์ วงศ์วิวัฒน์เสรี | 673380293-3 | 01 | `sirapat_673380293-3_01` | Menu Catalog, Customer Ordering |
| ศรัณย์ พาพรชัย | 673380515-5 | 02 | `sarun_673380515-5_02` | Kitchen, Serving, Order State |
| ธีรเมธ สายคำ | 673380273-9 | 02 | `teeramet_673380273-9_02` | Billing, Payment, Deployment |
| เมธัส มณีวิจิตร | 673380300-2 | 01 | `methus_673380300-2_01` | Authentication, Stock, Admin Shell |

## Repository Structure

```text
code/   source code and configuration
test/   shared fixtures, evidence, and test reports
doc/    contracts, diagrams, documentation, and slides
img/    project media
```

## Git Workflow

```text
personal branch -> Pull Request -> develop -> release Pull Request -> main
```

- สมาชิกแต่ละคนต้องสร้างและ push personal branch ด้วยบัญชี GitHub ของตนเอง
- ห้าม push feature ตรงเข้า `main` หรือ `develop`
- ทุก Pull Request ต้องมี reviewer อย่างน้อยหนึ่งคน
- Commit message ใช้รูปแบบ `<type>: <description>`

## Tech Stack

- Java 17, Spring Boot 3.x, Maven
- PostgreSQL, Spring Data JPA, Flyway
- React, Vite, TypeScript, Tailwind CSS
- Axios, React Router
- JUnit 5, Mockito, Spring Boot Test

รายละเอียดการติดตั้งและคำสั่งรันจะเพิ่มหลัง Foundation PR รวมเข้า `develop`


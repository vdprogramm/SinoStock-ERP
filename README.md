# SinoStock ERP — Spring Boot + React (Tiếng Việt)

**Frontend:** React 18 + TypeScript + Vite + Ant Design + Recharts; nằm trong [frontend/](frontend/). Giao diện chính không còn được viết thủ công trong `src/main/resources/static/index.html`: thư mục đó được tạo từ bản build React khi Docker build.

**Backend:** Java 21 + Spring Boot 3.5 + Spring Security JWT + Spring Data JPA + PostgreSQL trên Neon.

**Triển khai:** Dockerfile build React trước rồi copy bản build vào JAR Spring Boot, dùng **một** Render Web Service và URL hiện tại. Không cần tạo thêm dịch vụ frontend.

## 9 màn hình React
1. Tổng quan và biểu đồ doanh thu 7 ngày
2. Quản lý sản phẩm, chỉnh sửa và điều chỉnh tồn kho
3. Bán hàng, xác nhận / hủy đơn
4. Khách hàng
5. Nhà cung cấp
6. Mua hàng và nhập kho
7. Kế toán, thanh toán và công nợ
8. Báo cáo điều hành
9. Tài khoản nhân viên và phân quyền

Có 6 vai trò chính; dữ liệu `STAFF` cũ vẫn được hỗ trợ. Menu và thao tác phía React tuân theo vai trò; backend quyết định quyền thực tế.

## Chạy thử frontend trên máy
```bash
cd frontend
npm install
npm run dev
```
Lúc này backend chạy trên localhost:8080 và Vite proxy `/api` về backend.

## Kiểm tra build
```bash
cd frontend
npm run build
cd ..
mvn test
```

---

## Tài liệu backend ban đầu


**Java 21 · Spring Boot 3.5.16 · PostgreSQL · Spring Security/JWT · JPA · Swagger**

Backend MVP for managing inventory and orders for cross-border China-Vietnam distributors. A created order is `PENDING` and **does not reserve stock**. Confirming an order acquires database row locks and deducts stock in one transaction. Cancelling a `CONFIRMED` order restores stock exactly once; cancelling `PENDING` does not change stock.

> **安全说明 / Security:** Seeded accounts and the example JWT secret are LOCAL DEVELOPMENT ONLY. Set `SEED_DEMO=false`, rotate the JWT secret, use HTTPS, implement user management and database migrations before any public production deployment.

## Setup / Cài đặt nhanh (Windows)

1. Install **JDK 21**, **Maven 3.9+**, PostgreSQL 16/17 (or Docker Desktop). Check `java -version` and `mvn -version`.
2. Database (choose one):
   - pgAdmin Query Tool: `CREATE DATABASE sinostock_db;` and use your local PostgreSQL credentials.
   - **Or** run `docker compose up -d` from this folder; container creates `sinostock_db` (user/password `postgres`). **Do not start both PostgreSQL servers on port 5432.**
3. Configure environment variables or edit `src/main/resources/application.yml` for local development:

   ```powershell
   $env:DB_URL='jdbc:postgresql://localhost:5432/sinostock_db'
   $env:DB_USERNAME='postgres'
   $env:DB_PASSWORD='postgres' # change if your local DB password differs
   $env:JWT_SECRET='replace-with-a-long-random-secret-at-least-32-bytes'
   ```

   `.env.example` is informational: **Spring Boot does not load .env automatically**.
4. Run commands in the directory containing `pom.xml`:

   ```powershell
   mvn test
   mvn spring-boot:run
   ```

5. Test URLs:
   - Health: <http://localhost:8080/api/health>
   - Swagger UI: <http://localhost:8080/swagger-ui/index.html>
   - OpenAPI: <http://localhost:8080/v3/api-docs>
6. Run the full flow in **another PowerShell**:

   ```powershell
   powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1
   ```

## Demo users / 测试账户

| Role | Username | Password | Permissions |
|---|---|---|---|
| ADMIN | admin | Admin@12345 | manage products, adjust stock, create/confirm/cancel orders, dashboard |
| STAFF | staff | Staff@12345 | browse products, create/confirm/cancel orders, dashboard |

Accounts only seed when `SEED_DEMO=true` (default local development) and are not reset if they already exist in database. Passwords are stored hashed using BCrypt.

## REST API

| Method | Endpoint | Role / Behavior |
|---|---|---|
| `GET` | `/api/health` | Public |
| `POST` | `/api/auth/login` | Public, returns Bearer token |
| `GET` | `/api/products` | ADMIN/STAFF, active products |
| `GET` | `/api/products/{id}` | ADMIN/STAFF |
| `POST` | `/api/products` | ADMIN, create SKU |
| `PUT` | `/api/products/{id}` | ADMIN, edit fields but not stock |
| `PATCH` | `/api/products/{id}/stock` | ADMIN, atomic stock adjustment, logged |
| `DELETE` | `/api/products/{id}` | ADMIN, soft delete |
| `GET` | `/api/orders` | ADMIN/STAFF |
| `GET` | `/api/orders/{id}` | ADMIN/STAFF |
| `POST` | `/api/orders` | ADMIN/STAFF, create PENDING |
| `PATCH` | `/api/orders/{id}/confirm` | ADMIN/STAFF, deduct stock transactionally |
| `PATCH` | `/api/orders/{id}/cancel` | ADMIN/STAFF, restore stock if confirmed |
| `GET` | `/api/inventory/low-stock` | ADMIN/STAFF |
| `GET` | `/api/inventory/logs` | ADMIN/STAFF, latest 100 |
| `GET` | `/api/dashboard` | ADMIN/STAFF, 7 days Asia/Shanghai |

### Request examples

**POST** `/api/auth/login`:

```json
{"username":"admin","password":"Admin@12345"}
```

**POST** `/api/products` (ADMIN, use Bearer token):

```json
{"sku":"CN-004","nameCn":"蓝牙耳机","nameEn":"Bluetooth Earbuds","priceCny":129.00,"stockQuantity":15,"lowStockThreshold":5}
```

**POST** `/api/orders` (ADMIN/STAFF):

```json
{"items":[{"productId":1,"quantity":2},{"productId":2,"quantity":1}]}
```

**PATCH** `/api/products/1/stock` (ADMIN):

```json
{"changeQuantity":10,"reason":"PURCHASE_RECEIPT"}
```

## PostgreSQL schema

Five business tables: `app_users`, `products`, `sales_orders`, `order_items`, `inventory_logs`. Java JPA creates/updates tables (`ddl-auto: update` is for MVP development ONLY).

Core inventory rule:

```
POST /orders  -> PENDING   (no stock change)
PATCH confirm -> lock sales_order -> lock products by sorted ID
              -> validate stock -> subtract quantities -> inventory logs -> CONFIRMED
PATCH cancel  -> lock sales_order -> if CONFIRMED lock products -> add stock -> logs
              -> CANCELLED
```

Rows are locked using `@Lock(PESSIMISTIC_WRITE)` and Spring `@Transactional`; failed confirmation returns 409 and rolls back. Product updates and manual stock adjustments use row locks. Cancellation of a previously confirmed order removes its revenue from the active revenue dashboard. The daily revenue chart is grouped by **confirmation date** in Asia/Shanghai and excludes subsequently cancelled orders.

## Automated tests

`mvn test` uses an in-memory **H2** test database (PostgreSQL compatibility mode) and MockMvc. Tests cover:

- login / 401 unauthenticated / 403 STAFF trying to create product;
- creating an order does not change stock;
- confirming order deducts inventory;
- cancelling confirmed order restores stock and cannot be repeated;
- insufficient stock returns 409, leaves `PENDING` and does not affect stock;
- cancelling pending order leaves stock unchanged;
- manual stock adjustment and log endpoint.

**Important:** H2 validates business logic but is not a full replacement for a real PostgreSQL concurrency stress test. Test two concurrent confirmations against PostgreSQL before a production rollout.

## Project structure

```
src/main/java/com/sinostock/erp/
  config/       # Security, CORS, seed demo data
  controller/   # Auth, Products, Orders, Inventory, Dashboard
  domain/       # JPA models, enums
  dto/          # Request & response records
  exception/    # Consistent API errors
  repository/   # Spring Data JPA repositories and row locks
  security/     # JWT generation
  service/      # Transactional order and inventory business logic
src/test/       # H2 + MockMvc integration tests
scripts/        # PowerShell smoke test
```

## Notes and constraints

- MVP only: no suppliers/purchase-order process, payments, returns, password reset, audit user attribution on stock logs, pagination or frontend in this archive.
- No stock reservation: a PENDING order can fail at confirmation time if another order took stock first.
- CNY prices are stored directly, with no live exchange-rate conversion.
- To scale: add Flyway migrations, pagination, a proper indexed revenue aggregate query, refresh/revocation support for tokens, rate limiting, user administration, password management and monitoring.
- Do not expose Swagger or seeded credentials publicly without tightening security.

**中文简介:** 中越库存订单管理系统，支持商品管理、库存调整、订单创建/确认/取消、库存流水、角色权限控制以及营业报表。订单确认时使用数据库悲观锁与事务控制，避免超卖。

## Deploy on Render in one Blueprint (new)

**Follow [DEPLOY_RENDER.md](DEPLOY_RENDER.md).** The repo now includes
`Dockerfile`, `.dockerignore`, and `render.yaml` for a Web Service + PostgreSQL.

- Visit your deployed service's root URL `/` for the bilingual Chinese/English UI
  (login, product list, dashboard, new orders, confirm and cancel).
- Swagger remains available under `/swagger-ui/index.html`.
- Render creates `JWT_SECRET` securely and prompts for `ADMIN_PASSWORD` at
  first Blueprint setup. Account username is `admin`.
- `SEED_DEMO=false` on Render prevents known-password local test accounts.
- Sample product data is populated once with `SEED_SAMPLE_PRODUCTS=true`.
- **No local Java, Maven, PostgreSQL or Docker is needed when deploying from GitHub.**

**Free tier warning:** Render free web services sleep on idle, and Render
free Postgres databases expire after 30 days. This is suitable only for demos.
The embedded HTML/JavaScript admin interface is intentionally lightweight
for tonight's deployment; a React/TypeScript frontend can replace it later.

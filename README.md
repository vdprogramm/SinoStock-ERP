# SinoStock ERP — Hệ thống quản lý kho, mua hàng, bán hàng và công nợ

**Dự án Java Spring Boot + React + PostgreSQL — giao diện tiếng Việt, phân quyền theo 6 bộ phận.**

**Demo:** https://sinostock-erp-api.onrender.com  
**Mã nguồn:** https://github.com/vdprogramm/SinoStock-ERP  
**Tài liệu API:** https://sinostock-erp-api.onrender.com/swagger-ui/index.html  
**Kiểm tra API:** https://sinostock-erp-api.onrender.com/api/health

> **Về tài khoản demo:** Bảng username/mật khẩu bên dưới là tài khoản được tạo **chỉ khi chạy local với `SEED_DEMO=true`**. Trên Render đang để `SEED_DEMO=false`; tài khoản demo không tự được tạo và mật khẩu Admin thực tế **không được lưu trên GitHub**.

## 1. Giới thiệu và mục đích đề tài

SinoStock ERP là dự án mô phỏng hệ thống quản trị hoạt động kinh doanh cho một doanh nghiệp thương mại hoặc đơn vị phân phối hàng hóa, có thể áp dụng trong bối cảnh nhập hàng từ nhà cung cấp và bán cho khách hàng. Thay vì lưu hàng hóa ở Excel, đơn hàng ở ứng dụng khác và thanh toán ở sổ riêng, doanh nghiệp cần một hệ thống tập trung để các bộ phận cộng tác trên cùng dữ liệu.

**Bài toán cần giải quyết:**

1. **Kho không chính xác:** nhiều nhân viên đồng thời xác nhận đơn, dẫn đến bán vượt số lượng tồn kho; nhập/xuất hàng không có nhật ký.
2. **Đơn hàng rời rạc:** khó theo dõi đơn chờ xử lý, đã xác nhận, bị hủy và xác định khi nào phải trừ/hoàn kho.
3. **Mua hàng thiếu quy trình:** cần quản lý nhà cung cấp, tạo phiếu mua và xác nhận đã nhận hàng trước khi cộng kho.
4. **Công nợ khó kiểm soát:** đơn bán được xác nhận nhưng chưa thu đủ tiền; cần ghi nhận từng lần thu và số dư phải thu.
5. **Thiếu phân quyền:** nhân viên bán hàng không nên được tự ý sửa kho; kế toán không nên chỉnh sửa thông tin nhân viên; quản lý cần xem số liệu tổng hợp.
6. **Khó theo dõi hoạt động:** cần dashboard, cảnh báo hàng sắp hết và báo cáo doanh thu/chi phí mua hàng để hỗ trợ quyết định.

**Mục tiêu:** xây dựng phiên bản ERP quy mô MVP có luồng nghiệp vụ từ nhập hàng → quản lý tồn kho → tạo đơn bán → xác nhận/trừ kho → thu tiền → báo cáo, kèm RBAC, REST API và giao diện React tiếng Việt.

## 2. Công nghệ

| Thành phần | Công nghệ | Vai trò |
|---|---|---|
| Frontend | React 18, TypeScript, Vite | Giao diện SPA, component và kiểm tra kiểu |
| UI | Ant Design, Recharts, React Router | Bảng dữ liệu, biểu mẫu, biểu đồ, định tuyến |
| Backend | Java 21, Spring Boot 3.5 | REST API và xử lý nghiệp vụ |
| Bảo mật | Spring Security, JWT, BCrypt | Đăng nhập, mã hóa mật khẩu, phân quyền |
| Database | PostgreSQL (Neon khi deploy), Spring Data JPA/Hibernate | Lưu trữ dữ liệu và transaction |
| Kiểm thử | JUnit, MockMvc, H2 | Kiểm thử API và nghiệp vụ |
| Deploy | Docker multi-stage, Render | Build React + Spring Boot trên một dịch vụ |
| API documentation | Swagger/OpenAPI | Khám phá và kiểm thử endpoint |

**Ngôn ngữ giao diện:** Tiếng Việt. **Tiền tệ hiện tại:** CNY (¥) trong database/API; chưa có quy đổi VND tự động.

## 3. Chức năng chi tiết: dùng để làm gì?

| Phân hệ | Người dùng làm được gì? | Giá trị nghiệp vụ |
|---|---|---|
| **Tổng quan** | Xem tổng sản phẩm, đơn chờ/đã xác nhận, doanh thu, cảnh báo tồn kho và biểu đồ 7 ngày | Nắm nhanh tình hình vận hành |
| **Hàng hóa & kho** | Danh sách SKU, giá bán, tồn hiện có; thêm/sửa/ngừng bán; điều chỉnh kho có lý do; xem nhật ký thông qua API | Hạn chế sai lệch số lượng, truy vết nhập/xuất |
| **Bán hàng** | Tạo đơn theo sản phẩm/số lượng; xem trạng thái; xác nhận hoặc hủy | Chuẩn hóa quy trình xử lý đơn |
| **Khách hàng** | Thêm/xem tên, điện thoại, email, địa chỉ | Tập trung thông tin liên hệ khách hàng |
| **Nhà cung cấp** | Thêm/xem hồ sơ nhà cung cấp | Quản lý đầu mối nhập hàng |
| **Mua hàng & nhập kho** | Lập phiếu mua, chọn nhà cung cấp, SKU, lượng và giá nhập; xác nhận đã nhận/hủy phiếu | Nhập hàng theo phiếu và tăng tồn đúng thời điểm |
| **Kế toán & công nợ** | Xem khoản phải thu; ghi nhận từng lần thanh toán, phương thức, số còn nợ | Hạn chế thu quá giá trị đơn và theo dõi tiền chưa thu |
| **Báo cáo** | Xem doanh thu đơn đã xác nhận, tiền đã ghi nhận thu, công nợ, chi phí mua hàng đã nhận, số phiếu mua/bán | Hỗ trợ giám đốc/kế toán kiểm soát tình hình |
| **Nhân sự & phân quyền** | Admin tạo username, cấp role, đổi role, đặt lại mật khẩu | Tách quyền hạn, không cấp quyền quản trị cho nhân viên |

**Giới hạn hiện tại:** khách hàng chưa được gắn trực tiếp vào từng đơn bán; chưa có module trả hàng/hoàn tiền, duyệt mua hàng nhiều cấp, hóa đơn/thuế và sổ kế toán chuẩn. Báo cáo là báo cáo quản trị MVP, **không thay thế báo cáo tài chính**.

## 4. Luồng nghiệp vụ mẫu

### 4.1. Mua hàng → nhập kho

1. `PURCHASING_MANAGER`/Admin tạo **nhà cung cấp**.
2. Tạo phiếu mua: nhà cung cấp + sản phẩm + số lượng + giá nhập. Phiếu bắt đầu ở **DRAFT (chờ nhập)**; tồn kho **chưa tăng**.
3. Khi hàng thực tế về, chọn **Nhập kho**: Backend khóa bản ghi phiếu và sản phẩm trong transaction, cộng tồn và ghi nhật ký `PURCHASE_RECEIVE`.
4. Phiếu chuyển sang **RECEIVED (đã nhập)**. Không nhập lại một phiếu đã nhận.
5. Phiếu DRAFT có thể **CANCELLED**, không ảnh hưởng tồn.

### 4.2. Bán hàng → trừ kho → hủy đơn

1. Sales/Admin tạo đơn theo sản phẩm và số lượng, ban đầu **PENDING (chờ xác nhận)**: kho chưa bị trừ hay giữ trước.
2. Khi xác nhận, Backend dùng `@Transactional` + **Pessimistic Lock** trên sản phẩm, kiểm tra tồn và trừ số lượng, ghi nhật ký `ORDER_CONFIRM`. Nếu thiếu hàng, trả **409 Conflict**, rollback, đơn vẫn PENDING.
3. Đơn thành **CONFIRMED (đã xác nhận)** và được đưa vào doanh thu đã xác nhận.
4. Nếu hủy đơn đã xác nhận **và chưa ghi nhận thu tiền**, hệ thống hoàn tồn và ghi nhật ký `ORDER_CANCEL`. Hủy đơn PENDING không thay đổi tồn. Hủy lần thứ hai bị chặn.
5. Nếu đơn đã ghi nhận thanh toán, hệ thống **chặn hủy** cho đến khi có quy trình xử lý hoàn tiền (hiện chưa triển khai).

### 4.3. Thu tiền → theo dõi công nợ

1. Kế toán/Admin vào **Kế toán & công nợ**, chọn đơn đã xác nhận.
2. Nhập số tiền và phương thức (chuyển khoản/tiền mặt).
3. Backend chỉ chấp nhận số tiền **lớn hơn 0** và tổng thu **không vượt** tổng tiền đơn.
4. Theo dõi theo công thức: **Còn phải thu = giá trị đơn đã xác nhận – tổng tiền đã ghi nhận thu**.

Chú ý: đây là ghi nhận thanh toán **thủ công**, không kết nối ngân hàng/cổng thanh toán và không tự xác minh giao dịch.

### 4.4. Ví dụ giải quyết bán vượt tồn

Sản phẩm A còn **5** chiếc. Hai nhân viên cùng xác nhận hai đơn, mỗi đơn **4** chiếc. Hệ thống khóa dòng sản phẩm khi trừ kho; giao dịch xác nhận đầu tiên có thể thành công, giao dịch còn lại sẽ nhận lỗi **không đủ tồn**. Tồn cuối không âm. Các giao dịch đều có xử lý rollback khi thất bại.

## 5. Vai trò (RBAC) và trách nhiệm

| Mã vai trò | Tên hiển thị | Nhiệm vụ |
|---|---|---|
| `ADMIN` | Quản trị viên | Toàn quyền phân hệ, tạo tài khoản, cấp/đổi quyền |
| `WAREHOUSE_MANAGER` | Quản lý kho | Quản lý SKU, giá bán, cập nhật hàng hóa, điều chỉnh số lượng, xem lịch sử tồn |
| `SALES_STAFF` | Nhân viên bán hàng | Quản lý khách hàng; tạo, xác nhận, hủy đơn bán; xem hàng hóa |
| `ACCOUNTANT` | Kế toán | Xem đơn bán, công nợ, ghi nhận thu tiền và báo cáo |
| `PURCHASING_MANAGER` | Quản lý mua hàng | Quản lý nhà cung cấp, phiếu mua hàng và xác nhận nhập kho |
| `GENERAL_MANAGER` | Ban giám đốc | Theo dõi dashboard, đơn bán, khách hàng, công nợ và báo cáo; không được ghi nhận thu |
| `STAFF` | Nhân viên cũ (legacy) | Chỉ giữ để tương thích dữ liệu cũ: xem hàng hóa, tạo/xử lý đơn; không phải một trong 6 vai trò mới |

### Ma trận phân quyền Backend

**C = tạo/ghi, R = xem, U = cập nhật/xử lý, – = không có quyền; quyền có thể tùy endpoint.**

| Chức năng | Admin | Kho | Bán hàng | Kế toán | Mua hàng | Giám đốc |
|---|---|---|---|---|---|---|
| Dashboard | R | R | R | R | R | R |
| Danh sách sản phẩm | R | R | R | R | R | R |
| Thêm/sửa/ngừng bán/điều chỉnh kho | C/U | C/U | – | – | – | – |
| Danh sách và xử lý đơn bán | C/R/U | – | C/R/U | R | – | R |
| Khách hàng | C/R | – | C/R | – | – | C/R |
| Nhà cung cấp | C/R | – | – | – | C/R | – |
| Phiếu mua/nhập kho | C/R/U | – | – | – | C/R/U | – |
| Xem công nợ và khoản thu | R | – | – | R | – | R |
| Ghi nhận thu tiền | C | – | – | C | – | – |
| Báo cáo tổng hợp | R | – | – | R | – | R |
| Tài khoản / phân quyền | C/R/U | – | – | – | – | – |

**Quyền thực tế được kiểm tra bởi Spring Security ở Backend**, không chỉ ẩn menu React. `ADMIN` có thể tạo tài khoản cho tất cả 6 vai trò. Không cho hạ quyền Admin cuối cùng.

## 6. Tài khoản và mật khẩu đăng nhập

### 6.1. Tài khoản DEMO dùng trên máy cá nhân

Các tài khoản sau **được tự tạo khi `SEED_DEMO=true` và database chưa có username tương ứng** (mặc định khi chạy local). Nếu username đã tồn tại, ứng dụng **không tự đặt lại mật khẩu**.

| Vai trò | Tên đăng nhập | Mật khẩu demo (CHỈ LOCAL) |
|---|---|---|
| **Quản trị viên** (`ADMIN`) | `admin` | `Admin@12345` |
| **Quản lý kho** (`WAREHOUSE_MANAGER`) | `warehouse` | `Warehouse@12345` |
| **Nhân viên bán hàng** (`SALES_STAFF`) | `sales` | `SalesStaff@12345` |
| **Kế toán** (`ACCOUNTANT`) | `accountant` | `Accountant@12345` |
| **Quản lý mua hàng** (`PURCHASING_MANAGER`) | `purchasing` | `Purchasing@12345` |
| **Ban giám đốc** (`GENERAL_MANAGER`) | `manager` | `Manager@12345` |
| **Nhân viên cũ** (`STAFF`) | `staff` | `Staff@12345` |

**Cảnh báo:** Các chuỗi trên là mật khẩu mẫu công khai trong mã nguồn, tuyệt đối **không dùng trên Render, Neon hay môi trường thực tế**. Chỉ dùng database phát triển riêng.

### 6.2. Tài khoản trên website Render (production/demo online)

- Website: **https://sinostock-erp-api.onrender.com**
- Tài khoản quản trị được bootstrap ban đầu: username mặc định **`admin`** (`ADMIN_USERNAME`).
- **Mật khẩu Admin:** mật khẩu riêng đã nhập ở biến bí mật `ADMIN_PASSWORD` của Render khi khởi tạo lần đầu. **Không công khai, không ghi trong README, không lấy từ mật khẩu local**.
- Khi `SEED_DEMO=false`, **không có sẵn** các tài khoản `warehouse`, `sales`, `accountant`, `purchasing`, `manager`. Admin phải tạo trong trang **Nhân sự và phân quyền** hoặc API `POST /api/admin/users`.
- Trong giao diện Admin: **Nhân sự và phân quyền → Tạo tài khoản → Username, mật khẩu riêng ít nhất 12 ký tự, vai trò → Lưu**. Đưa thông tin đăng nhập tới người được cấp qua kênh riêng; không đăng mật khẩu lên repository.
- Sau khi tài khoản đã được tạo, đổi `ADMIN_PASSWORD` trong Render **không tự đổi mật khẩu trong PostgreSQL**; sử dụng API đặt lại mật khẩu bằng quyền Admin nếu cần.

**Vì vậy, mật khẩu chính xác của tài khoản online (Admin và các nhân viên) không thể biết từ GitHub và không nên công bố trong README public.**

## 7. Kiến trúc và cấu trúc thư mục

```text
SinoStock-ERP/
├── frontend/                          # React + TypeScript + Ant Design
│   ├── src/
│   │   ├── pages/                     # 9 màn hình nghiệp vụ
│   │   ├── App.tsx                    # Giao diện, menu và route theo quyền
│   │   ├── auth.tsx                   # Phiên JWT
│   │   ├── api.ts                     # REST client
│   │   └── types.ts                   # Types, trạng thái, quyền
│   ├── package.json
│   └── vite.config.ts
├── backend/                           # Java 21 + Spring Boot
│   ├── src/main/java/com/sinostock/erp/
│   │   ├── config/                    # Security, seed dữ liệu
│   │   ├── controller/                # REST endpoints
│   │   ├── service/                   # Transaction & nghiệp vụ
│   │   ├── repository/                # JPA repositories
│   │   ├── domain/                    # JPA entities, roles, statuses
│   │   ├── security/                  # JWT
│   │   ├── dto/                       # Request/response
│   │   └── exception/                 # API error
│   ├── src/test/
│   ├── pom.xml
│   ├── docker-compose.yml
│   └── scripts/
├── Dockerfile                         # Build React & Spring Boot
├── render.yaml                        # Render Web Service
└── .github/workflows/test.yml        # CI
```

Hai mã nguồn **tách biệt**. Khi deploy, Docker chỉ copy **bản build** React vào JAR Spring Boot để phục vụ cùng domain; không đặt `src` React trong Backend.

## 8. Database chính

| Bảng | Mục đích |
|---|---|
| `app_users` | Username, mật khẩu BCrypt và vai trò |
| `products` | SKU, tên, giá CNY, số lượng và ngưỡng cảnh báo |
| `sales_orders` + `order_items` | Đơn bán và các dòng sản phẩm |
| `inventory_logs` | Nhật ký biến động tồn kho |
| `customers` | Hồ sơ khách hàng |
| `suppliers` | Hồ sơ nhà cung cấp |
| `purchase_orders` | Phiếu mua và trạng thái nhập hàng |
| `payments` | Các khoản thu tiền đã ghi nhận theo đơn |

Hibernate sử dụng `ddl-auto=update` ở bản demo. Khi sử dụng thực tế cần migration (Flyway/Liquibase), backup và kiểm soát thay đổi schema.

## 9. REST API tiêu biểu

| Phân hệ | API |
|---|---|
| Auth | `POST /api/auth/login`, `GET /api/health` |
| Sản phẩm | `GET/POST /api/products`, `PUT /api/products/{id}`, `PATCH /api/products/{id}/stock`, `DELETE /api/products/{id}` |
| Đơn bán | `GET/POST /api/orders`, `PATCH /api/orders/{id}/confirm`, `PATCH /api/orders/{id}/cancel` |
| Kho | `GET /api/inventory/low-stock`, `GET /api/inventory/logs` |
| Khách hàng | `GET/POST /api/customers` |
| Nhà cung cấp | `GET/POST /api/suppliers` |
| Mua hàng | `GET/POST /api/purchasing`, `PATCH /api/purchasing/{id}/receive`, `PATCH /api/purchasing/{id}/cancel` |
| Kế toán | `GET/POST /api/accounting/payments`, `GET /api/accounting/receivables` |
| Báo cáo | `GET /api/dashboard`, `GET /api/reports/summary` |
| Tài khoản | `GET/POST /api/admin/users`, `PATCH /api/admin/users/{id}/role`, `PATCH /api/admin/users/{id}/password` |

Swagger: [https://sinostock-erp-api.onrender.com/swagger-ui/index.html](https://sinostock-erp-api.onrender.com/swagger-ui/index.html)

## 10. Chạy project ở máy cá nhân

Yêu cầu: **JDK 21, Maven 3.9+, Node.js 22+, Docker Desktop (hoặc PostgreSQL riêng).**

**Bước 1: Backend + PostgreSQL local**

```powershell
cd backend
docker compose up -d
mvn clean test
mvn spring-boot:run
```

Database mặc định: `sinostock_db`, `localhost:5432`. Mặc định local `SEED_DEMO=true`, nên các tài khoản mục 6.1 được tạo khi chưa tồn tại.

**Bước 2: React**

Mở terminal mới từ thư mục gốc:

```powershell
cd frontend
npm install
npm run dev
```

Mở **http://localhost:5173**. Frontend proxy `/api` tới **http://localhost:8080**.

**Bước 3: Kiểm tra build**

```powershell
cd frontend
npm run build
cd ../backend
mvn test
```

CI cũng kiểm tra Docker image. Dùng database local riêng khi thử tạo/hủy đơn để không ảnh hưởng dữ liệu trên Neon.

## 11. Deploy trên Render + Neon

Render hiện triển khai **1 Web Service** từ repository; Docker build React, tiếp đó Maven build Spring Boot và đóng gói bản React vào JAR. Neon cung cấp PostgreSQL.

Biến môi trường quan trọng trong Render:

| Biến | Ý nghĩa |
|---|---|
| `DATABASE_URL` | Chuỗi kết nối Neon (bí mật) |
| `JWT_SECRET` | Bí mật ký JWT, tối thiểu 32 byte |
| `ADMIN_USERNAME` | Username Bootstrap Admin |
| `ADMIN_PASSWORD` | Mật khẩu bootstrap Admin lần đầu (tối thiểu 12 ký tự) |
| `SEED_DEMO` | **`false`** trên website công khai |
| `SEED_SAMPLE_PRODUCTS` | Có thể `true` để tạo SKU mẫu nếu kho rỗng |
| `DB_POOL_SIZE` | Số kết nối Hikari tối đa (ví dụ: 3) |

Xem [DEPLOY_RENDER.md](DEPLOY_RENDER.md) để biết cách cập nhật deployment. Không đưa DATABASE_URL, JWT_SECRET, mật khẩu thực tế vào GitHub, screenshot hoặc README.

## 12. Kiểm thử và giới hạn kỹ thuật

Đã xây dựng test cho đăng nhập JWT và role, chu trình đơn bán trừ/hoàn kho, thiếu hàng rollback, điều chỉnh tồn, và phân quyền kế toán. CI GitHub Actions build React, chạy test Java và build Docker.

**Giới hạn MVP cần phát triển tiếp:**

- Chưa gắn khách hàng trực tiếp với đơn bán; chưa có công nợ nhà cung cấp, hóa đơn, VAT và xuất báo cáo PDF/Excel.
- Chưa có quy trình hoàn tiền, xuất hóa đơn chuẩn hay đối soát ngân hàng.
- Chưa có kiểm kê đa kho, lô hàng, phân quyền theo từng chi nhánh, audit đầy đủ hành động người dùng.
- Token JWT có hạn sử dụng; chưa có refresh-token hoặc cơ chế thu hồi token.
- Dashboard doanh thu là tổng đơn đang CONFIRMED, không phải lợi nhuận; chi phí nhập hàng chưa được phân bổ theo giá vốn thực bán.
- Không xem `ddl-auto=update` hoặc H2 test là giải pháp production hoàn chỉnh.

**Mục đích học tập/portfolio:** chứng minh thiết kế REST API, RBAC, Spring Security JWT, dữ liệu quan hệ, nghiệp vụ transaction chống bán vượt tồn, tích hợp React và triển khai cloud.

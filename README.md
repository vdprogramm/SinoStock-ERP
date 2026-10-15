# SinoStock ERP — Hệ thống quản trị doanh nghiệp

**Frontend và Backend đã tách thành hai thư mục ngang cấp, dễ mở riêng trong VS Code / IntelliJ IDEA.** Toàn bộ màn hình người dùng sử dụng tiếng Việt.

## Cấu trúc mã nguồn

```text
SinoStock-ERP/
├── frontend/                 # React + TypeScript + Vite + Ant Design
│   ├── src/
│   │   ├── pages/
│   │   ├── App.tsx
│   │   ├── api.ts
│   │   └── auth.tsx
│   ├── package.json
│   └── vite.config.ts
├── backend/                  # Java 21 + Spring Boot + PostgreSQL
│   ├── src/
│   │   ├── main/java/com/sinostock/erp/
│   │   └── test/
│   ├── pom.xml
│   ├── scripts/
│   ├── docker-compose.yml
│   └── .env.example
├── Dockerfile                # Đóng gói cả hai phần vào một image
├── render.yaml               # Render Web Service, Neon PostgreSQL
└── .github/workflows/test.yml
```

**Không viết React trong Backend.** React được phát triển độc lập tại `frontend/`. Chỉ khi xây dựng Docker image để deploy một Web Service trên Render, bản biên dịch tĩnh từ React mới được đưa vào JAR Spring Boot. Mã nguồn React không bị chép vào `backend/src/` trong repository.

## Công nghệ

- **Frontend:** React 18, TypeScript, Vite, Ant Design, Recharts, React Router.
- **Backend:** Java 21, Spring Boot 3.5, Spring Security + JWT, JPA/Hibernate, Maven.
- **Database:** PostgreSQL trên Neon, chung cho ứng dụng sau khi deploy.
- **Triển khai:** Render qua Dockerfile tại thư mục gốc. Không cần tạo một Web Service thứ hai.

## Chức năng

- Tổng quan doanh nghiệp và biểu đồ doanh thu bảy ngày.
- Quản lý sản phẩm, tồn kho, cảnh báo và điều chỉnh kho.
- Đơn bán hàng, xác nhận và hủy đơn; khóa bản ghi chống bán vượt tồn.
- Quản lý khách hàng và nhà cung cấp.
- Tạo phiếu mua hàng, nhập kho tăng tồn.
- Thu tiền, công nợ phải thu và lịch sử thanh toán.
- Báo cáo doanh thu/chi phí nhập hàng tổng hợp.
- Tạo tài khoản, phân quyền sáu vai trò.

Các vai trò chính: `ADMIN`, `WAREHOUSE_MANAGER`, `SALES_STAFF`, `ACCOUNTANT`, `PURCHASING_MANAGER`, `GENERAL_MANAGER`. Vai trò `STAFF` cũ được giữ để tương thích dữ liệu.

## Chạy trên máy

**Backend** (JDK 21, Maven, PostgreSQL):

```powershell
cd backend
docker compose up -d
mvn spring-boot:run
```

Nếu không dùng Docker, hãy chuẩn bị PostgreSQL ở `localhost:5432`. Mặc định cục bộ seed tài khoản demo; **không sử dụng mật khẩu demo khi công khai website**.

**Frontend** (Node.js 22+, mở terminal khác tại gốc repository):

```powershell
cd frontend
npm install
npm run dev
```

Frontend chạy ở `http://localhost:5173`, proxy `/api` sang Backend `http://localhost:8080`.

## Kiểm thử

```powershell
cd frontend
npm install
npm run build
cd ../backend
mvn test
```

CI trên GitHub chạy build React, test Java và kiểm tra Docker image.

## Deploy Render + Neon

Xem [DEPLOY_RENDER.md](DEPLOY_RENDER.md). Render giữ nguyên Web Service, URL và các biến môi trường hiện có. File `render.yaml` và `Dockerfile` vẫn nằm ở thư mục gốc để Render tìm được cấu hình. Không đưa chuỗi kết nối Neon, mật khẩu Admin hay JWT secret vào GitHub.

**Giới hạn:** Đây là ERP demo/portfolio, chưa phải phần mềm kế toán/ERP production. `ddl-auto=update` cần thay bằng migration, và cần bổ sung giám sát, audit, backup, chính sách xác thực mạnh hơn trước khi vận hành thực tế.

# Hướng dẫn deploy SinoStock ERP trên Render và Neon

Repository đã tách rõ `frontend/` và `backend/`, song vẫn dùng **một** Render Web Service và Neon PostgreSQL.

## Cấu hình hiện tại

- Repository: [vdprogramm/SinoStock-ERP](https://github.com/vdprogramm/SinoStock-ERP), nhánh `main`.
- Service Render: `sinostock-erp-api`.
- File triển khai tại gốc: `Dockerfile`, `render.yaml`.
- Database: Neon PostgreSQL, qua biến `DATABASE_URL` có sẵn trên Render.
- URL website: https://sinostock-erp-api.onrender.com

## Sau khi cập nhật GitHub

Render được cấu hình tự động triển khai commit từ `main`. Nếu không thấy build mới, mở Render → `sinostock-erp-api` → Deploys → Manual Deploy → Deploy latest commit.

Docker build theo quy trình:
1. Node.js biên dịch React trong `frontend/`.
2. Maven biên dịch Backend trong `backend/`.
3. Docker chèn **tệp đã biên dịch** của React vào JAR Spring Boot (không đưa mã nguồn React vào thư mục Backend trên GitHub).
4. Chạy ứng dụng tại cùng một domain, API có tiền tố `/api`.

Cấu hình `render.yaml` tiếp tục chỉ tạo Web Service, **không tạo Render PostgreSQL thứ hai**.

## Biến môi trường quan trọng

`DATABASE_URL`: chuỗi kết nối Neon (không chia sẻ và không đưa vào GitHub).

`JWT_SECRET`: chuỗi ngẫu nhiên đủ mạnh do Render tạo.

`ADMIN_USERNAME`: `admin`.

`ADMIN_PASSWORD`: mật khẩu bí mật dài ít nhất 12 ký tự, chỉ sử dụng tạo tài khoản lần đầu, không đặt lại mật khẩu sẵn có sau mỗi lần khởi động.

`SEED_DEMO`: `false` trên dịch vụ công khai.

`SEED_SAMPLE_PRODUCTS`: `true` nếu muốn seed sản phẩm mẫu khi DB trống.

`DB_POOL_SIZE`: `3`.

Không thay đổi các biến này chỉ vì di chuyển thư mục.

## Xác minh

- Website: https://sinostock-erp-api.onrender.com
- Sức khỏe API: https://sinostock-erp-api.onrender.com/api/health
- Swagger: https://sinostock-erp-api.onrender.com/swagger-ui/index.html

Mở website, thử đăng nhập, xem sản phẩm, đơn hàng, dashboard. Khi vừa deploy Render Free có thể khởi động chậm do chế độ ngủ.

## Khi có lỗi

- Nếu React không build: xem bước `npm run build` trong log.
- Nếu Docker không tìm được `pom.xml`: kiểm tra `COPY backend/pom.xml ./` và `COPY backend/src/ ./src/`.
- Nếu PostgreSQL không kết nối: kiểm tra `DATABASE_URL` trong Environment của Web Service.
- Nếu 401: đăng nhập lại.
- Nếu 403: tài khoản hiện tại không có quyền thực hiện thao tác.
- Nếu React hiển thị giao diện cũ: thử Ctrl + Shift + R.

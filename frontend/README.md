# SinoStock ERP — React Frontend (tiếng Việt)

- React 18, TypeScript, Vite, Ant Design, Recharts.
- Cùng origin với Spring Boot sau khi deploy; gọi REST API qua đường dẫn `/api`.
- JWT trong sessionStorage; role access luôn được kiểm tra lại ở Backend.
- Các màn hình: tổng quan, sản phẩm/kho, bán hàng, khách hàng, nhà cung cấp, mua hàng/nhập kho, kế toán/công nợ, báo cáo và quản lý tài khoản.

## Phát triển cục bộ
Chạy backend ở http://localhost:8080, sau đó:
```bash
cd frontend
npm install
npm run dev
```
Vite proxy các request `/api` đến Spring Boot.
```bash
npm run build
```
Render dùng Dockerfile tại thư mục gốc để build FE trước, sau đó copy `dist/` vào thư mục `src/main/resources/static` của Spring Boot khi đóng gói JAR. Chỉ cần một Render Web Service.

**Lưu ý:** Đơn vị tiền tệ hiện là CNY theo Backend; nhãn, nút, thông báo và tên vai trò hiển thị bằng tiếng Việt.

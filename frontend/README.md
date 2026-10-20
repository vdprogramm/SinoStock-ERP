# Giao diện SinoStock ERP — React + TypeScript

**Toàn bộ mã nguồn Frontend nằm riêng tại thư mục `frontend/`.**

Dùng React 18, TypeScript, Vite, Ant Design, React Router và Recharts; giao diện bằng tiếng Việt.

## Chạy riêng Frontend

```bash
cd frontend
npm install
npm run dev
```

Backend Spring Boot cần chạy song song tại http://localhost:8080; Vite proxy `/api` đến cổng Backend. `npm run build` tạo `dist/` ở Frontend.

## Tệp chính

- `src/App.tsx`: layout, menu và trang đăng nhập.
- `src/auth.tsx`: phiên đăng nhập JWT.
- `src/api.ts`: REST client.
- `src/types.ts`: DTO TypeScript và vai trò.
- `src/pages/`: 9 trang nghiệp vụ.
- `src/styles.css`: CSS.

Backend nằm tại `../backend/`, không nằm trong Frontend và không chứa mã React.

Trên Render, Dockerfile gốc tạo bản build React rồi đóng gói tệp tĩnh vào Spring Boot để chạy trên **một domain**. Việc này không làm thay đổi cách sắp xếp mã nguồn trên GitHub.


## Deploy Frontend riêng trên Vercel

1. Mở https://vercel.com/new và kết nối GitHub repository `vdprogramm/SinoStock-ERP`.
2. Chọn **Root Directory = `frontend`** (quan trọng vì repository có cả backend).
3. Framework Preset: **Vite**; Build Command: `npm run build`; Output Directory: `dist`.
4. File `frontend/vercel.json` chuyển tiếp các request `/api/*` tới `https://sinostock-erp-api.onrender.com/api/*`. Không cần công khai JWT secret hay DATABASE_URL trên Vercel.
5. Bấm **Deploy**, chờ trạng thái Ready và thử đăng nhập, xem dashboard từ URL `.vercel.app`.

Do ứng dụng sử dụng **HashRouter**, các URL như `/#/login` và `/#/overview` không cần rewrite SPA. Vercel phục vụ giao diện tĩnh ngay cả khi Render Free đang ngủ. Tuy nhiên lần gọi `/api` đầu tiên vẫn phải chờ Backend thức dậy và có thể gặp timeout. Nếu mạng báo lỗi gateway, đợi khoảng một phút và thử lại.

**Bảo mật:** Database Neon có dữ liệu thật; giữ `SEED_DEMO=false` trên Render. Không chia sẻ mật khẩu, token JWT hay DATABASE_URL. Sau khi đã xác minh URL Vercel hoạt động, cập nhật link demo trong README và portfolio. Trong giai đoạn chuyển đổi giữ site Render hiện tại làm dự phòng.

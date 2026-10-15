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

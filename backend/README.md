# SinoStock ERP — Backend Spring Boot

**Mã nguồn Backend được đặt độc lập trong `backend/`.**

- Java 21 + Spring Boot 3.5 + Maven.
- PostgreSQL, Spring Data JPA, Spring Security + JWT.
- Các package Java nằm tại `src/main/java/com/sinostock/erp/`.
- `src/test/` có bộ kiểm thử JUnit và MockMvc.
- `scripts/` chứa bộ test API PowerShell.
- `docker-compose.yml` tạo PostgreSQL local khi cần.

## Chạy riêng Backend

```bash
cd backend
docker compose up -d
mvn test
mvn spring-boot:run
```

API mặc định: http://localhost:8080/api/health. Swagger: http://localhost:8080/swagger-ui/index.html.

Giao diện React nằm tại `../frontend/`, hoàn toàn tách biệt khi phát triển. Backend không chứa file HTML giao diện cũ; Dockerfile tại gốc chỉ thêm bản React đã biên dịch khi đóng gói deployment.

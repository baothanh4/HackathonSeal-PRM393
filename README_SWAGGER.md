# HackathonSeal - API & Swagger testing

Checklist (những việc đã có trong tài liệu này):
- Liệt kê các nhóm API và endpoint chính.
- Hướng dẫn cách chạy ứng dụng và mở Swagger UI.
- Ví dụ payload để test một số endpoint phổ biến (login/register, event, team, submission).

Mục đích: tài liệu ngắn gọn giúp bạn test các API của dự án trên Swagger UI.

1) Chạy ứng dụng

- Bằng Maven wrapper (Windows Command):

  mvnw.cmd spring-boot:run

- Hoặc build jar rồi chạy:

  mvnw.cmd package
  java -jar target\HackthonSeal-0.0.1-SNAPSHOT.jar


2) Mở Swagger UI

- Sau khi ứng dụng chạy mặc định trên cổng 8080, mở trình duyệt tới:

  http://localhost:8080/swagger-ui.html
  hoặc
  http://localhost:8080/swagger-ui/index.html

- OpenAPI JSON: http://localhost:8080/v3/api-docs


3) Cách cấp quyền (JWT) trong Swagger

- Ứng dụng sử dụng scheme Bearer JWT (được cấu hình trong OpenAPI). Trên Swagger UI bấm nút "Authorize".
- Trong ô token nhập: Bearer <JWT_TOKEN>
  (ví dụ: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...)
- Sau khi Authorize, các endpoint yêu cầu authentication/roles sẽ có thể gọi được.


4) Danh sách API chính (tóm tắt) 

Lưu ý: mọi đường dẫn bắt đầu bằng /api/v1

- Auth
  - POST /api/v1/auth/login
    - Mô tả: Đăng nhập, trả về token (AuthResponse)
    - Body (JSON):
      {
        "email": "user@example.com",
        "password": "password123"
      }

  - POST /api/v1/auth/register
    - Mô tả: Đăng ký user mới
    - Body (JSON): (tham khảo DTO `RegisterRequest` trong source)

- User Administration (Admin)
  - GET /api/v1/admin/users?page=0&size=20
    - Mô tả: Lấy danh sách users (paged)
    - Yêu cầu: ROLE_ADMIN hoặc ROLE_COORDINATOR

  - GET /api/v1/admin/users/{id}
    - Mô tả: Lấy chi tiết user theo id

  - POST /api/v1/admin/users
    - Mô tả: Tạo internal user (ADMIN)
    - Body: `AdminCreateUserRequest` (email, fullName, role, password...)

  - PUT /api/v1/admin/users/{id}/status
    - Mô tả: Cập nhật trạng thái account (APPROVE/REJECT/SUSPEND)
    - Body: `AccountStatusRequest` (ví dụ { "status": "APPROVED" })

- Events
  - POST /api/v1/events  (ADMIN)
    - Tạo event mới. Body: `EventRequest`.

  - PUT /api/v1/events/{id}  (ADMIN)
    - Cập nhật event.

  - DELETE /api/v1/events/{id}  (ADMIN)
    - Xóa event.

  - GET /api/v1/events?page=0&size=10&sortBy=createdAt&sortDir=desc
    - Lấy danh sách event (có phân trang và sắp xếp).

  - GET /api/v1/events/{id}
    - Lấy chi tiết event theo id.

  - GET /api/v1/events/search?title=abc&status=OPEN&page=0&size=10
    - Tìm event theo title và/hoặc status.

- Event Registration
  - POST /api/v1/events/{eventId}/register
    - Đăng ký user cho event. Body: `RegistrationRequest` (contains userId or guest info)
    - Response: `RegistrationResponse`

  - DELETE /api/v1/events/{eventId}/registrations/{userId}
    - Hủy đăng ký. Yêu cầu ADMIN hoặc chủ user tương ứng.

  - GET /api/v1/events/{eventId}/registrations?page=0&size=20
    - Lấy danh sách participants (ADMIN/COORDINATOR)

- Rounds
  - POST /api/v1/events/{eventId}/rounds  (ADMIN)
    - Tạo vòng thi cho event. Body: `RoundRequest`.

  - GET /api/v1/events/{eventId}/rounds
    - Lấy danh sách rounds cho event.

- Rules
  - POST /api/v1/events/{eventId}/rules  (ADMIN)
    - Thêm luật cho event. Body: `RuleRequest`.

  - GET /api/v1/events/{eventId}/rules
    - Lấy tất cả luật cho event.

  - GET /api/v1/events/{eventId}/rules/{ruleId}
    - Lấy chi tiết luật theo id.

  - PUT /api/v1/events/{eventId}/rules/{ruleId}  (ADMIN)
    - Cập nhật tên/miêu tả luật.

  - DELETE /api/v1/events/{eventId}/rules/{ruleId}  (ADMIN)
    - Xóa luật.

- Teams
  - POST /api/v1/events/{eventId}/teams
    - Tạo team. Body: `TeamRequest`.

  - POST /api/v1/events/{eventId}/teams/{teamId}/join
    - Tham gia team (authenticated user).

  - POST /api/v1/events/{eventId}/teams/{teamId}/add-member
    - Thêm member (team leader). Query params: registrationId or email.

  - GET /api/v1/events/{eventId}/teams
    - Lấy danh sách teams trong event.

  - GET /api/v1/events/{eventId}/teams/{teamId}
    - Lấy chi tiết team và danh sách thành viên.

- Submissions
  - POST /api/v1/events/{eventId}/teams/{teamId}/rounds/{roundId}/submissions
    - Nộp bài dự thi (gửi GitHub URL, chỉ leader hoặc ADMIN có thể nộp). Body: `SubmissionRequest`.


5) Ví dụ nhanh test flow (đăng nhập rồi test endpoint có bảo vệ)

- 1) Đăng ký (nếu cần): POST /api/v1/auth/register với thông tin user.
- 2) Đăng nhập: POST /api/v1/auth/login -> lấy token từ response (ví dụ field `accessToken` hoặc `token`).
- 3) Trên Swagger UI bấm "Authorize" và nhập `Bearer <TOKEN>`.
- 4) Gọi các endpoint cần auth (ví dụ POST /api/v1/events nếu là ADMIN).


6) Gợi ý khi test

- Kiểm tra các DTO trong `src/main/java/com/example/hackathonseal/models/dto/request` để biết trường bắt buộc.
- Nếu gặp lỗi 403, đảm bảo token có role phù hợp (ADMIN/COORDINATOR).
- Kiểm tra logs console để biết chi tiết lỗi server.


Nếu bạn muốn, mình có thể mở rộng README bằng:
- Thêm ví dụ response mẫu cho từng endpoint.
- Tạo Postman collection hoặc file YAML OpenAPI export.

-- End of README


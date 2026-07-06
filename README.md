# Todo List — Ứng dụng Quản lý công việc

Bài test Intern Developer. Ứng dụng web quản lý công việc có **đăng nhập / đăng ký bằng JWT**.

🔗 **Demo trực tuyến:** https://todolist-diepau.up.railway.app

- **Backend:** Java 23 + Spring Boot 3.5 (xử lý API & logic)
- **Giao diện:** Thymeleaf + Bootstrap 5 (render từ server)
- **Bảo mật:** Spring Security + JWT (token lưu trong cookie HttpOnly)
- **Cơ sở dữ liệu:** H2 lưu ra file (không cần cài đặt, dữ liệu không mất khi restart)
- **Triển khai:** Dockerfile sẵn sàng deploy lên Railway

## Chức năng

- Đăng ký, đăng nhập, đăng xuất (JWT)
- Xem danh sách công việc (mỗi người chỉ thấy công việc của mình)
- Thêm / sửa / xóa công việc
- Đánh dấu hoàn thành / chưa hoàn thành
- Tìm kiếm theo tiêu đề và lọc theo trạng thái (Tất cả / Chưa xong / Đã xong)
- Kiểm tra dữ liệu không hợp lệ (tiêu đề trống, mật khẩu quá ngắn, sửa/xóa việc không phải của mình...)

## Yêu cầu môi trường

- JDK 23 (project cấu hình Java 23)
- Có sẵn Maven, hoặc dùng Maven Wrapper `mvnw` kèm theo (không cần cài Maven)

## Cách chạy

Mở terminal tại thư mục dự án:

```bash
# Windows
mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

Sau đó mở trình duyệt: **http://localhost:8080**

Lần đầu vào sẽ bị chuyển tới trang đăng nhập. Bấm **Đăng ký** để tạo tài khoản, rồi đăng nhập.

## Xem dữ liệu trong H2

Mở **http://localhost:8080/h2-console**, điền:

- JDBC URL: `jdbc:h2:file:./data/tododb`
- User Name: `sa`
- Password: (để trống)

Bấm **Connect** để xem bảng `USERS`, `TASKS`.

> Dữ liệu được lưu ra file `./data/tododb.mv.db` nên **không mất khi rebuild/restart**.

## Chạy bằng Docker

```bash
docker build -t todolist .
docker run -p 8080:8080 todolist
```

Mở http://localhost:8080.

## Deploy lên Railway

1. Đẩy code lên GitHub.
2. Vào Railway → **New Project → Deploy from GitHub repo** → chọn repo này. Railway tự phát hiện `Dockerfile` và build.
3. Railway tự cấp biến `PORT`, app đã đọc sẵn nên không cần chỉnh gì.

> **Giữ dữ liệu trên Railway:** filesystem của Railway sẽ reset mỗi lần deploy lại. Muốn dữ liệu tồn tại lâu dài, vào service → **Volumes → New Volume**, mount vào đường dẫn `/app/data` (nơi file H2 được lưu).

## Cấu trúc thư mục

```
src/main/java/com/diepau/todolist/
├── TodolistApplication.java      # điểm khởi động
├── config/SecurityConfig.java    # cấu hình bảo mật (ai được vào trang nào)
├── security/
│   ├── JwtService.java           # tạo & đọc JWT
│   └── JwtCookieFilter.java      # đọc cookie JWT ở mỗi request
├── model/                        # User, Task (bảng dữ liệu)
├── repository/                   # truy vấn database
├── service/                      # logic nghiệp vụ
└── controller/                   # nhận request, trả về trang
src/main/resources/
├── application.properties        # cấu hình DB & JWT
└── templates/                    # giao diện Thymeleaf (login, register, tasks, edit)
```

## Chạy test

```bash
./mvnw test
```

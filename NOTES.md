# Ghi chú code cần lưu ý

Tài liệu này giải thích những đoạn code quan trọng nhất, dễ hiểu cho người mới. Đọc file này là nắm được "bộ khung" của dự án.

## 1. Luồng hoạt động tổng quát

```
Trình duyệt  →  Controller  →  Service  →  Repository  →  Database (H2)
   (HTML)       (nhận request)  (logic)    (truy vấn)
```

- **Controller**: nhận request từ trình duyệt, gọi Service, trả về trang HTML (Thymeleaf).
- **Service**: chứa logic nghiệp vụ (kiểm tra, xử lý). Controller *không* nên chứa logic phức tạp.
- **Repository**: nói chuyện với database. Chỉ cần khai báo interface, Spring tự sinh code.
- **Model (Entity)**: mỗi class = một bảng trong database.

Cách tách 3 tầng này (Controller – Service – Repository) là chuẩn để code **dễ đọc, dễ bảo trì** — đúng thứ bài test đánh giá.

## 2. Cơ chế đăng nhập bằng JWT (phần khó nhất)

**JWT (JSON Web Token)** là một chuỗi ký tự đại diện cho "giấy thông hành" đã đăng nhập. Nó gồm 3 phần ngăn bởi dấu chấm: `header.payload.signature`.

- `payload` chứa thông tin (ở đây là *username*).
- `signature` (chữ ký) được tạo từ một chuỗi bí mật (`secret`). Kẻ gian không biết `secret` thì **không thể làm giả** token.

Luồng đăng nhập trong dự án này:

1. User nhập username + password → `AuthController.login()`.
2. `UserService.checkLogin()` so mật khẩu (đã mã hóa bcrypt).
3. Đúng → `JwtService.generateToken()` tạo token → lưu vào **cookie tên `jwt`**.
4. Các request sau, trình duyệt tự gửi kèm cookie này.
5. `JwtCookieFilter` chạy trước mỗi request, đọc cookie, kiểm tra token, rồi báo cho Spring Security biết "user này đã đăng nhập".

> **Vì sao lưu token trong cookie mà không phải header `Authorization`?**
> Vì giao diện là Thymeleaf (server render), trình duyệt mở trang trực tiếp. Cookie được gửi tự động ở mọi request nên không cần viết JavaScript. Đặt cookie **HttpOnly** để JavaScript không đọc được → chống tấn công XSS đánh cắp token.

Đoạn tạo token (jjwt phiên bản 0.12.x — cú pháp mới, khác các hướng dẫn cũ trên mạng):

```java
Jwts.builder()
    .subject(username)      // lưu username vào token
    .issuedAt(now)
    .expiration(expiry)     // hạn dùng
    .signWith(key)          // ký bằng secret
    .compact();
```

Đoạn đọc token:

```java
Jwts.parser()
    .verifyWith(key)                 // kiểm tra chữ ký
    .build()
    .parseSignedClaims(token)
    .getPayload()
    .getSubject();                   // lấy lại username
```

## 3. Mã hóa mật khẩu — TUYỆT ĐỐI không lưu mật khẩu gốc

```java
// Khi đăng ký:
String hashed = passwordEncoder.encode(rawPassword);  // -> $2a$10$....
// Khi đăng nhập:
passwordEncoder.matches(rawPassword, hashedTrongDB);  // so sánh
```

`BCryptPasswordEncoder` băm mật khẩu **một chiều** (không giải ngược được). Trong database chỉ thấy chuỗi băm, kể cả bạn cũng không đọc được mật khẩu gốc của user.

## 4. Cấu hình bảo mật (SecurityConfig)

Đây là nơi quyết định **trang nào cần đăng nhập**:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/login", "/register", "/css/**", "/h2-console/**").permitAll() // ai cũng vào
    .anyRequest().authenticated())   // còn lại phải đăng nhập
```

- `.sessionCreationPolicy(STATELESS)`: không dùng session của server, mỗi request tự xác thực lại bằng JWT.
- `.addFilterBefore(jwtCookieFilter, ...)`: cắm filter JWT vào dây chuyền bảo mật.
- `.frameOptions(sameOrigin)`: cho phép trang H2 console hiển thị (nó dùng iframe).

## 5. Chống người dùng "nghịch" URL (bảo mật dữ liệu)

Nếu chỉ xóa theo id, ai đó có thể gõ `/tasks/5/delete` để xóa việc của người khác. Nên mọi thao tác đều kiểm tra chủ sở hữu:

```java
public Task getOwned(Long id, String owner) {
    Task task = taskRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Khong tim thay cong viec"));
    if (!task.getOwner().equals(owner)) {          // không phải của mình
        throw new IllegalArgumentException("Ban khong co quyen voi cong viec nay");
    }
    return task;
}
```

## 6. Xử lý dữ liệu không hợp lệ

- Kiểm tra ở Controller: tiêu đề trống, username < 3 ký tự, mật khẩu < 6 ký tự → trả về thông báo lỗi.
- Ràng buộc ở Entity: `@NotBlank`, `@Size(max=200)` trên `Task.title`.
- `@ExceptionHandler` trong `TaskController` bắt lỗi chung, chuyển về trang danh sách kèm thông báo thay vì hiện trang lỗi 500 xấu xí.

## 7. Tìm kiếm & lọc không cần viết SQL

Spring Data JPA tự sinh câu truy vấn **dựa vào tên hàm** trong Repository:

```java
findByOwnerAndCompletedOrderByCreatedAtDesc(owner, false); // lọc việc chưa xong
findByOwnerAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(owner, keyword); // tìm theo tiêu đề
```

Đặt tên đúng quy tắc (`findBy` + tên trường + điều kiện) là Spring hiểu và tự viết SQL.

## 8. Đổi cách lưu dữ liệu

Hiện tại dùng **H2 trong RAM** (nhanh, không cần cài, nhưng tắt app là mất dữ liệu). Sửa 1 dòng trong `application.properties`:

```properties
# Lưu ra file, không mất khi tắt app:
spring.datasource.url=jdbc:h2:file:./data/tododb
```

Muốn đổi sang PostgreSQL/MySQL thật sau này: chỉ cần đổi `datasource.url`, thêm driver tương ứng vào `pom.xml` — **không phải sửa code Java** nhờ đã tách tầng Repository.

## 9. Những lỗi hay gặp

- **Vào trang cứ bị đá về /login**: chưa đăng nhập, hoặc cookie `jwt` hết hạn (mặc định 24h) — đăng nhập lại.
- **Đổi `app.jwt.secret` xong token cũ báo lỗi**: đúng rồi, vì chữ ký đổi. Đăng nhập lại để lấy token mới. `secret` phải dài tối thiểu 32 ký tự.
- **Port 8080 đang bị chiếm**: đổi `server.port` trong `application.properties`.

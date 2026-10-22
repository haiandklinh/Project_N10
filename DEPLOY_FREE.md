# Hướng dẫn triển khai backend chấm công miễn phí

## 1. Dịch vụ đề xuất

- **Render Free** chạy FastAPI: https://render.com/docs/deploy-fastapi
- **Aiven for MySQL Free** giữ dữ liệu SQL: https://aiven.io/docs/products/mysql/concepts/mysql-free-tier
- **Upstash Redis Free** cho OTP/mã mời: https://upstash.com/pricing/redis
- **Cloudinary Free** cho avatar/ảnh nếu chức năng đó được dùng: https://cloudinary.com/documentation/billing_and_plans

Theo tài liệu hiện tại, Render Free sẽ sleep sau 15 phút không có traffic và cần khoảng một phút để thức dậy; filesystem của service là ephemeral, vì vậy không lưu database/upload lâu dài trong ổ đĩa Render. Aiven Free MySQL có 1 GB RAM và 1 GB disk, có thể tắt dịch vụ khi không có hoạt động lâu. Upstash Free hiện nêu 250 MB dữ liệu, 10 GB bandwidth và 500.000 lệnh/tháng. Cloudinary Free hiện có 25 credits cho storage, bandwidth và transformations. Giới hạn có thể thay đổi, hãy kiểm tra trang chính thức trước khi deploy.

## 2. Chuẩn bị branch backend trên GitHub

1. Mở `https://github.com/haiandklinh/Project_N10` và chọn branch chứa backend (bạn đã nói backend ở một branch riêng, ví dụ `backend-hieu`).
2. Đảm bảo branch đó có các file `requirements.txt`, `app/main.py`, `core/config.py`, `db/init.py`, cùng `models/`, `repositories/`, `schemas/` và `services/`.
3. File `requirements.txt` trong ZIP bạn gửi được mã hóa UTF-16; bản trong gói này đã chuyển sang UTF-8. Nếu branch GitHub vẫn là bản cũ, thay file bằng bản UTF-8 trước khi deploy, nếu không `pip install -r requirements.txt` có thể lỗi đọc file.
4. Trong gói này, `core/config.py` có thêm `DB_SSL` (mặc định `false`). Trên cloud MySQL, đặt `DB_SSL=true` để bật TLS. Nếu bạn chọn deploy trực tiếp source cũ trên GitHub, hãy mang thay đổi `DB_SSL` này sang branch backend trước khi đặt biến môi trường.

## 3. Tạo MySQL miễn phí ở Aiven

1. Đăng ký Aiven và tạo service **MySQL** ở Free tier.
2. Chọn region gần backend nhất nếu có, chờ status sẵn sàng.
3. Trong phần connection information, lấy host, port, user và password. Dùng database `defaultdb` (hoặc database riêng bạn đã tạo).
4. Tạo `DATABASE_URL` theo mẫu:

   `mysql+asyncmy://USER:URL_ENCODED_PASSWORD@HOST:PORT/defaultdb?charset=utf8mb4`

   Thay các phần in hoa bằng thông tin Aiven. Password có các ký tự như `@`, `:`, `/`, `#`, `?` cần URL-encode trước khi đưa vào URL. Dùng `DB_SSL=true` ở Render; đừng đưa password thật vào GitHub.

Aiven Free có thể power off dịch vụ nếu không có hoạt động trong thời gian dài; khi deploy thất bại vì không kết nối được, kiểm tra trạng thái MySQL trong Aiven trước.

## 4. Tạo Redis miễn phí ở Upstash

1. Tạo Redis database tại https://console.upstash.com/.
2. Chọn region gần Render nhất nếu có.
3. Lấy URL/credentials kết nối TLS. Điền chuỗi kiểu `rediss://default:PASSWORD@HOST:PORT` vào biến `REDIS_URL` (dùng chính URL/endpoint được Upstash cung cấp; không tự đoán host/port).

Backend dùng Redis trong `core/redis_custom.py` để lưu OTP và mã mời có thời hạn, nên nếu thiếu `REDIS_URL`, backend có thể khởi động nhưng các API phụ thuộc Redis sẽ lỗi khi gọi.

## 5. Lấy khóa Cloudinary

Tạo tài khoản miễn phí tại https://cloudinary.com/ và lấy Cloud Name, API Key, API Secret trong dashboard. `core/config.py` khai báo ba biến này là bắt buộc, nên cần khai báo trên Render ngay cả khi hiện tại ít dùng upload ảnh. Nếu có tính năng ảnh/avatar, dùng credentials thật để chức năng đó hoạt động.

## 6. Tạo Web Service trên Render

1. Vào https://dashboard.render.com/ → **New** → **Web Service**.
2. Kết nối GitHub, chọn repo `haiandklinh/Project_N10` và branch backend.
3. **Root Directory**: chọn đúng thư mục chứa `requirements.txt`, `app/main.py`, `core/config.py`, `db/init.py`. Nếu branch có `backend/requirements.txt`, chọn `backend`; nếu các file ở ngay gốc branch, để trống.
4. **Build Command**: `pip install -r requirements.txt`
5. **Start Command**: `python -m db.init && uvicorn app.main:app --host 0.0.0.0 --port $PORT`
6. Chọn **Free** instance.
7. Trong Environment, thêm `PYTHON_VERSION=3.13.5` để dùng phiên bản Python cố định, tương thích tốt hơn với dependencies đã khóa trong requirements. Đây là biến của Render, không thêm vào file `.env` của ứng dụng.
8. Thêm các biến môi trường bên dưới trong mục Environment; không cần tạo file `.env` trong GitHub.
9. Create Web Service và xem **Logs** đến khi Uvicorn khởi động thành công trên `0.0.0.0`.

Start command chạy `db.init` dưới dạng module để Python tìm được các package ngang cấp như `core`, `models` và `db`, sau đó tạo các bảng còn thiếu trước khi khởi động Uvicorn. `create_all()` không thay thế hệ thống migration nếu sau này bạn đổi cấu trúc các bảng; với thay đổi schema lớn cần migration/back-up riêng.

### Các biến môi trường bắt buộc

Lấy database, Redis và Cloudinary values từ các dịch vụ đã tạo. Các giá trị tên notification có thể dùng tên như dưới đây, miễn backend/frontend thống nhất loại notification:

```text
DATABASE_URL=mysql+asyncmy://USER:URL_ENCODED_PASSWORD@HOST:PORT/defaultdb?charset=utf8mb4
DB_ECHO=false
DB_SSL=true
REDIS_URL=rediss://default:REDIS_PASSWORD@REDIS_HOST:REDIS_PORT
JWT_SECRET=<chuỗi ngẫu nhiên dài, riêng cho môi trường này>
JWT_ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=30
REFRESH_TOKEN_EXPIRE_DAYS=7
CLOUDINARY_CLOUD_NAME=<Cloudinary Cloud Name>
CLOUDINARY_API_KEY=<Cloudinary API Key>
CLOUDINARY_API_SECRET=<Cloudinary API Secret>
CHANGE_WORK_SHIFT_REQUEST=CHANGE_WORK_SHIFT_REQUEST
CHANGE_LEAVE_REQUEST=CHANGE_LEAVE_REQUEST
ATTENDANCE_ADJUSTMENT_REQUEST=ATTENDANCE_ADJUSTMENT_REQUEST
JOIN_COMPANY_REQUEST=JOIN_COMPANY_REQUEST
CHANGE_WORK_SHIFT_RESPONSE=CHANGE_WORK_SHIFT_RESPONSE
CHANGE_LEAVE_RESPONSE=CHANGE_LEAVE_RESPONSE
ATTENDANCE_ADJUSTMENT_RESPONSE=ATTENDANCE_ADJUSTMENT_RESPONSE
JOIN_COMPANY_RESPONSE=JOIN_COMPANY_RESPONSE
```

Tạo chuỗi JWT ngẫu nhiên trên máy Windows bằng lệnh PowerShell:

```powershell
python -c "import secrets; print(secrets.token_urlsafe(48))"
```

Copy kết quả vào `JWT_SECRET` của Render; không dùng chuỗi ví dụ này cho nhiều hệ thống và không commit nó vào repo.

## 7. Kiểm tra API

Sau khi deploy, lấy URL dạng `https://TEN-SERVICE.onrender.com` trong Render:

- `https://TEN-SERVICE.onrender.com/` → trả JSON `{"status":"ok","service":"attendance-api"}` nếu dùng bản code trong gói này.
- `https://TEN-SERVICE.onrender.com/docs` → Swagger UI để xem đường dẫn và gửi request thử.

Các router trong backend hiện được gắn trực tiếp (`/auth`, `/staffs`, `/managers`, `/attendances`, ...), **không có prefix `/api/v1`** trong `app/api/router.py`. Vì vậy đừng tự thêm `/api/v1` vào base URL trừ khi bạn sửa backend/frontend đồng thời.

## 8. Kết nối frontend Android ở branch `frontend-son`

1. Mở branch `frontend-son` trong Android Studio.
2. Tìm `BASE_URL`, `baseUrl(`, `Retrofit.Builder`, `10.0.2.2`, `localhost`, hoặc địa chỉ LAN cũ. Chỉ thay base URL ở nơi khai báo trung tâm thành `https://TEN-SERVICE.onrender.com/` (phải có dấu `/` cuối nếu thư viện Retrofit yêu cầu).
3. Giữ nguyên path của từng API nhưng đối chiếu với `/docs`; không dùng `/docs` làm base URL.
4. Nếu app dùng WebSocket notifications, URL cũng phải đổi sang `wss://TEN-SERVICE.onrender.com/ws/manager/notifications` hoặc `wss://TEN-SERVICE.onrender.com/ws/staff/notifications` và phải truyền access token theo cách backend đang yêu cầu.
5. Build app và thử đăng nhập, lấy profile, tạo/lấy lịch làm việc, check-in/check-out. Kiểm tra **Render Logs** nếu lỗi 500 hoặc không kết nối được.

Lưu ý: native Android thường không bị CORS như trình duyệt. Nếu dự án có thêm web client, mới cần cấu hình CORS tương ứng.

## 9. Các giới hạn cần biết

- Render Free sleep sau khoảng 15 phút không có inbound traffic; request đầu tiên sau đó có thể chậm khoảng một phút. WebSocket cũng có thể bị ảnh hưởng khi service idle.
- Database phải ở dịch vụ lưu trữ riêng; không lưu MySQL/SQLite hay ảnh tải lên vào ổ đĩa local của Render Free.
- Trong code hiện tại, `send_otp()` tạo OTP rồi trả `OTP` về response JSON; nó **chưa gửi SMS thật**. Đây là hành vi demo/test, không an toàn để làm luồng xác minh người dùng thực tế.
- Các gói Free phù hợp demo/môn học, không nên dùng làm backend production cho dữ liệu chấm công thật.

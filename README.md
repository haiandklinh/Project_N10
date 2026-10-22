# Deploy backend chấm công miễn phí (bản hỗ trợ triển khai)

Gói này được tạo từ file backend bạn gửi, giữ nguyên các router/service hiện có và chỉ thêm hỗ trợ cấu hình TLS cho MySQL, endpoint kiểm tra `/`, file mẫu biến môi trường và tài liệu deploy. Chưa kết nối vào tài khoản cloud/GitHub của bạn và chưa deploy trực tiếp.

## Stack được đề xuất

- Render Free: chạy FastAPI.
- Aiven for MySQL Free: lưu dữ liệu SQL; bật `DB_SSL=true`.
- Upstash Redis Free: OTP/mã mời; dùng URL `rediss://...`.
- Cloudinary Free: lưu ảnh/avatar nếu app dùng upload ảnh.

Các gói free phù hợp bài tập/demo, không phải cam kết SLA cho production. Xem giới hạn và trạng thái free hiện hành tại các đường dẫn trong `DEPLOY_FREE.md`.

## Cài đặt trên Render

1. Chọn repository `haiandklinh/Project_N10` và branch chứa backend (ví dụ `backend-hieu`).
2. Root Directory phải là thư mục chứa `requirements.txt`, `app/main.py`, `core/config.py` và `db/init.py`. Trong archive này, đó là `backend`.
3. Build Command: `pip install -r requirements.txt`
4. Start Command: `python -m db.init && uvicorn app.main:app --host 0.0.0.0 --port $PORT`
5. Thêm biến Render `PYTHON_VERSION=3.13.5` trong Environment (không thêm vào `.env` của ứng dụng).
6. Tạo đủ biến môi trường theo `backend/.env.example`, dùng thông tin thật từ Aiven, Upstash và Cloudinary.
7. Sau deploy, mở `/` để kiểm tra health và `/docs` để xem API.

Đọc `DEPLOY_FREE.md` để biết cách tạo dịch vụ, điền `DATABASE_URL` và đổi URL trong app Android.

package com.example.attendanceapp.ui.main.auth.register;

/**
 * Trạng thái xử lý gửi và xác thực OTP (UI State)
 */
public abstract class OtpState {

    // Khóa constructor để mô phỏng sealed class
    private OtpState() {}

    /** Trạng thái ban đầu */
    public static final class Idle extends OtpState {
        public static final Idle INSTANCE = new Idle();
        private Idle() {}
    }

    /** Trạng thái đang gửi / kiểm tra OTP */
    public static final class Loading extends OtpState {
        public static final Loading INSTANCE = new Loading();
        private Loading() {}
    }

    /** Trạng thái đã gửi mã OTP thành công tới số điện thoại */
    public static final class Sent extends OtpState {
        public final String phone;

        public Sent(String phone) {
            this.phone = phone;
        }
    }

    /** Trạng thái đã xác thực mã OTP thành công */
    public static final class Verified extends OtpState {
        public static final Verified INSTANCE = new Verified();
        private Verified() {}
    }

    /** Trạng thái lỗi (Trùng SĐT, gửi OTP thất bại, OTP không đúng,...) */
    public static final class Error extends OtpState {
        public final String message;

        public Error(String message) {
            this.message = message;
        }
    }
}
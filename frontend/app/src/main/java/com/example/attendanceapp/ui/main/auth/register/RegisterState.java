package com.example.attendanceapp.ui.main.auth.register;

import com.example.attendanceapp.domain.model.UserAccount;

/**
 * Trạng thái kết quả đăng ký (UI State)
 */
public abstract class RegisterState {

    // Khóa constructor để mô phỏng sealed class
    private RegisterState() {}

    /** Trạng thái ban đầu khi chưa thực hiện thao tác gì */
    public static final class Idle extends RegisterState {
        public static final Idle INSTANCE = new Idle();
        private Idle() {}
    }

    /** Trạng thái đang gọi API xử lý đăng ký */
    public static final class Loading extends RegisterState {
        public static final Loading INSTANCE = new Loading();
        private Loading() {}
    }

    /** Trạng thái đăng ký thành công */
    public static final class Success extends RegisterState {
        public final UserAccount user; // Nếu API không trả user, bạn có thể bỏ field này

        public Success() {
            this.user = null;
        }

        public Success(UserAccount user) {
            this.user = user;
        }
    }

    /** Trạng thái đăng ký thất bại */
    public static final class Error extends RegisterState {
        public final String message;

        public Error(String message) {
            this.message = message;
        }
    }
}
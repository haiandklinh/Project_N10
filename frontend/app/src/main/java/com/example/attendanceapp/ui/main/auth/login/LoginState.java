package com.example.attendanceapp.ui.main.auth.login;

import com.example.attendanceapp.domain.model.UserAccount;

/**
 * Trạng thái kết quả đăng nhập (UI State), dùng chung cho LoginViewModel và LoginSavedAccountViewModel
 */
public abstract class LoginState {

    // Khóa constructor để mô phỏng sealed class
    private LoginState() {}

    /** Trạng thái ban đầu khi chưa thực hiện thao tác gì */
    public static final class Idle extends LoginState {
        public static final Idle INSTANCE = new Idle();
        private Idle() {}
    }

    /** Trạng thái đang gọi API / xử lý đăng nhập */
    public static final class Loading extends LoginState {
        public static final Loading INSTANCE = new Loading();
        private Loading() {}
    }

    /** Trạng thái đăng nhập thành công */
    public static final class Success extends LoginState {
        public final UserAccount user;

        public Success(UserAccount user) {
            this.user = user;
        }
    }

    /** Trạng thái đăng nhập thất bại */
    public static final class Error extends LoginState {
        public final String message;

        public Error(String message) {
            this.message = message;
        }
    }
}
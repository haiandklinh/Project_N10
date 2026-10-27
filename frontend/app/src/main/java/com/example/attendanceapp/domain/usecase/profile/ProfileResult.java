package com.example.attendanceapp.domain.usecase.profile;

import com.example.attendanceapp.domain.model.UserAccount;

/** Kết quả tải hồ sơ sau khi chọn Role. */
public final class ProfileResult {

    private final UserAccount account; // null nếu chưa có hồ sơ

    private ProfileResult(UserAccount account) {
        this.account = account;
    }

    public static ProfileResult exists(UserAccount account) {
        return new ProfileResult(account);
    }

    public static ProfileResult notFound() {
        return new ProfileResult(null);
    }

    public boolean isFound() {
        return account != null;
    }

    public UserAccount getAccount() {
        return account;
    }
}
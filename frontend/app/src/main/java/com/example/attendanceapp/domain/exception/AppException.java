package com.example.attendanceapp.domain.exception;

import androidx.annotation.Nullable;

/**
 * Lỗi nghiệp vụ/mạng đã được chuẩn hóa để ViewModel chỉ việc hiển thị getMessage().
 * httpCode = 0 nghĩa là không có mã HTTP (mất mạng, lỗi không xác định).
 */
public class AppException extends RuntimeException {

    private final int httpCode;
    @Nullable private final String errorCode;

    public AppException(String message, int httpCode, @Nullable String errorCode) {
        super(message);
        this.httpCode = httpCode;
        this.errorCode = errorCode;
    }

    public int getHttpCode() { return httpCode; }

    /** Ví dụ: PHONE_ALREADY_EXISTS, INVALID_INVITATION_CODE (nếu backend trả về). */
    @Nullable public String getErrorCode() { return errorCode; }
}
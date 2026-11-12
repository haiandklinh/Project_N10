package com.example.attendanceapp.data.remote;

import com.example.attendanceapp.domain.exception.AppException;

import io.reactivex.rxjava3.core.Single;
import retrofit2.HttpException;

/** Phân biệt "chưa có hồ sơ" (404) với lỗi mạng / 500. */
public final class ProfileErrors {

    public static final String PROFILE_NOT_FOUND = "PROFILE_NOT_FOUND";

    private ProfileErrors() {}

    /** Dùng TRƯỚC ApiErrorMapper: đổi HttpException 404 thành AppException(PROFILE_NOT_FOUND). */
    public static <T> Single<T> mapNotFound(Throwable e) {
        if (e instanceof HttpException && ((HttpException) e).code() == 404) {
            return Single.error(new AppException("Chưa có hồ sơ", 404, PROFILE_NOT_FOUND));
        }
        return Single.error(e);
    }

    public static boolean isNotFound(Throwable e) {
        if (!(e instanceof AppException)) {
            return false;
        }
        AppException ex = (AppException) e;
        if (PROFILE_NOT_FOUND.equals(ex.getErrorCode()) || ex.getHttpCode() == 404) {
            return true;
        }
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase(java.util.Locale.ROOT) : "";
        return (ex.getHttpCode() == 400 || ex.getHttpCode() == 403 || ex.getHttpCode() == 422)
                && (msg.contains("not found") || msg.contains("không tìm thấy") || msg.contains("chưa có hồ sơ"));
    }
}
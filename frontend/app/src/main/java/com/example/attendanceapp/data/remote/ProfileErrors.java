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
        return e instanceof AppException
                && PROFILE_NOT_FOUND.equals(((AppException) e).getErrorCode());
    }
}
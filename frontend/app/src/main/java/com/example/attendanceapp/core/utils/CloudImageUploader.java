package com.example.attendanceapp.core.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Tiện ích xử lý URL ảnh đại diện.
 * Backend đã tích hợp lưu trữ qua Cloudinary và trả về trực tiếp đường dẫn ảnh Cloudinary.
 * Frontend chỉ sử dụng trực tiếp đường dẫn đó để hiển thị qua Glide/AvatarLoader,
 * không tự upload hoặc nén ảnh sang các dịch vụ bên thứ ba khác.
 */
public final class CloudImageUploader {

    private static final String TAG = "CloudImageUploader";

    private CloudImageUploader() {}

    /**
     * Bất đồng bộ RxJava Single
     */
    public static Single<String> uploadAvatarSingle(Context context, @Nullable String uriOrPath) {
        return Single.fromCallable(() -> uploadAvatarSync(context, uriOrPath))
                .subscribeOn(Schedulers.io());
    }

    /**
     * Chuẩn hóa đường dẫn ảnh đại diện (Đồng bộ).
     * Backend đã lưu bằng Cloudinary và trả về đường dẫn trực tiếp (HTTP/HTTPS).
     * Giữ nguyên URL hoặc đường dẫn được truyền vào, loại bỏ các giá trị placeholder ("null", "none", "string").
     */
    @Nullable
    public static String uploadAvatarSync(Context context, @Nullable String uriOrPath) {
        if (uriOrPath == null) {
            return null;
        }
        String clean = uriOrPath.trim();
        if (clean.isEmpty()
                || "null".equalsIgnoreCase(clean)
                || "none".equalsIgnoreCase(clean)
                || "string".equalsIgnoreCase(clean)) {
            return null;
        }

        Log.d(TAG, "Avatar path/URL: " + clean);
        return clean;
    }
}

package com.example.attendanceapp.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.attendanceapp.data.local.StaffAvatarCache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Tiện ích tải ảnh đại diện lên Cloud Storage (Catbox / Cloud CDN)
 * nhằm thu được URL công khai trực tiếp dạng HTTP/HTTPS ngắn (< 50 ký tự),
 * hoàn toàn vừa vặn với cột VARCHAR(255) trong CSDL Backend.
 */
public final class CloudImageUploader {

    private static final String TAG = "CloudImageUploader";
    private static final String CATBOX_API_URL = "https://catbox.moe/user/api.php";

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();

    private CloudImageUploader() {}

    /**
     * Tải ảnh đại diện lên đám mây (Bất đồng bộ RxJava Single)
     */
    public static Single<String> uploadAvatarSingle(Context context, @Nullable String uriOrPath) {
        return Single.fromCallable(() -> uploadAvatarSync(context, uriOrPath))
                .subscribeOn(Schedulers.io());
    }

    /**
     * Tải ảnh đại diện lên đám mây (Đồng bộ - chạy trên background thread)
     * Trả về URL trực tiếp dạng "https://files.catbox.moe/xxxxxx.jpg" (~35 ký tự)
     * Nếu không có mạng hoặc ảnh đã là URL, tự động xử lý an toàn.
     */
    @Nullable
    public static String uploadAvatarSync(Context context, @Nullable String uriOrPath) {
        if (uriOrPath == null || uriOrPath.trim().isEmpty()) {
            return null;
        }
        String clean = uriOrPath.trim();

        // 1. Nếu đã là link HTTP/HTTPS online thì không cần upload lại
        if (clean.startsWith("http://") || clean.startsWith("https://")) {
            return clean;
        }

        // 2. Nén ảnh và chuẩn bị file tạm để upload
        File compressedFile = compressImageToTempFile(context, clean);
        if (compressedFile == null || !compressedFile.exists()) {
            Log.w(TAG, "Không thể nén ảnh để tải lên đám mây, fallback đường dẫn gốc");
            return clean;
        }

        try {
            // 3. Upload lên Catbox API (Dịch vụ hosting ảnh trực tiếp, không cần API Key, lưu vĩnh viễn)
            RequestBody fileBody = RequestBody.create(
                    compressedFile,
                    MediaType.parse("image/jpeg")
            );

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("reqtype", "fileupload")
                    .addFormDataPart("fileToUpload", compressedFile.getName(), fileBody)
                    .build();

            Request request = new Request.Builder()
                    .url(CATBOX_API_URL)
                    .post(requestBody)
                    .build();

            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String publicUrl = response.body().string().trim();
                    if (publicUrl.startsWith("http://") || publicUrl.startsWith("https://")) {
                        Log.i(TAG, "Upload avatar thành công lên Cloud URL: " + publicUrl);
                        // Cập nhật vào Cache cục bộ để hiển thị ngay lập tức
                        StaffAvatarCache.saveAvatar(context, null, null, publicUrl);
                        return publicUrl;
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Lỗi khi upload avatar lên Catbox: " + e.getMessage());
        } finally {
            // Xóa file tạm nén sau khi upload xong
            try {
                if (compressedFile.exists()) {
                    compressedFile.delete();
                }
            } catch (Exception ignored) {}
        }

        // 4. Fallback: Nếu không có kết nối Internet ra ngoài (môi trường mạng nội bộ),
        // trả về Base64 / URI hiện tại để không gây crash app
        return clean;
    }

    /**
     * Đọc và nén ảnh đại diện từ URI/File path về kích thước chuẩn (tối đa 500x500, JPEG 85%)
     */
    @Nullable
    private static File compressImageToTempFile(Context context, String uriOrPath) {
        InputStream in = null;
        try {
            int rotationDegrees = 0;
            if (uriOrPath.startsWith("content://") && context != null) {
                Uri uri = Uri.parse(uriOrPath);
                in = context.getContentResolver().openInputStream(uri);
                try (InputStream exifIn = context.getContentResolver().openInputStream(uri)) {
                    if (exifIn != null) {
                        ExifInterface exif = new ExifInterface(exifIn);
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationDegrees = 90;
                        else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationDegrees = 180;
                        else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationDegrees = 270;
                    }
                } catch (Exception ignored) {}
            } else {
                File file;
                if (uriOrPath.startsWith("file://")) {
                    file = new File(Uri.parse(uriOrPath).getPath());
                } else {
                    file = new File(uriOrPath);
                }
                if (file.exists()) {
                    in = new java.io.FileInputStream(file);
                    try {
                        ExifInterface exif = new ExifInterface(file.getAbsolutePath());
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationDegrees = 90;
                        else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationDegrees = 180;
                        else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationDegrees = 270;
                    } catch (Exception ignored) {}
                }
            }

            if (in == null) return null;

            Bitmap bitmap = BitmapFactory.decodeStream(in);
            in.close();
            in = null;

            if (bitmap == null) return null;

            if (rotationDegrees != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotationDegrees);
                Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                if (rotated != bitmap) {
                    bitmap.recycle();
                    bitmap = rotated;
                }
            }

            int maxDim = 500;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > maxDim || height > maxDim) {
                float ratio = (float) width / (float) height;
                if (ratio > 1) {
                    width = maxDim;
                    height = Math.max(1, (int) (maxDim / ratio));
                } else {
                    height = maxDim;
                    width = Math.max(1, (int) (maxDim * ratio));
                }
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, true);
                if (scaled != bitmap) {
                    bitmap.recycle();
                    bitmap = scaled;
                }
            }

            File tempFile = new File(context.getCacheDir(), "upload_avatar_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
                fos.flush();
            }
            bitmap.recycle();

            return tempFile;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi nén ảnh avatar: " + e.getMessage());
            return null;
        } finally {
            if (in != null) {
                try { in.close(); } catch (Exception ignored) {}
            }
        }
    }
}

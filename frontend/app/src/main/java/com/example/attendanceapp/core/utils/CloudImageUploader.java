package com.example.attendanceapp.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.attendanceapp.BuildConfig;
import com.example.attendanceapp.data.local.StaffAvatarCache;

import org.json.JSONObject;

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
 * Tiện ích tải ảnh đại diện lên Backend Cloudinary API (/upload) để nhận URL công khai ngắn,
 * hoàn toàn vừa vặn với cột VARCHAR(255) trong CSDL Backend.
 */
public final class CloudImageUploader {

    private static final String TAG = "CloudImageUploader";

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private CloudImageUploader() {}

    public static Single<String> uploadAvatarSingle(Context context, @Nullable String uriOrPath) {
        return Single.fromCallable(() -> uploadAvatarSync(context, uriOrPath))
                .subscribeOn(Schedulers.io());
    }

    @Nullable
    public static String uploadAvatarSync(Context context, @Nullable String uriOrPath) {
        if (uriOrPath == null) {
            return "";
        }
        String clean = uriOrPath.trim();
        if (clean.isEmpty()
                || "null".equalsIgnoreCase(clean)
                || "none".equalsIgnoreCase(clean)
                || "string".equalsIgnoreCase(clean)) {
            return "";
        }

        // 1. Nếu đã là URL online (Cloudinary / CDN) và <= 255 ký tự thì giữ nguyên
        if (clean.startsWith("http://") || clean.startsWith("https://")) {
            return clean.length() <= 255 ? clean : "";
        }

        // 2. Nén ảnh thành tệp tạm thời
        File compressedFile = compressImageToTempFile(context, clean);
        if (compressedFile == null || !compressedFile.exists()) {
            Log.w(TAG, "Không thể đọc và nén ảnh, trả về rỗng để tránh vượt 255 ký tự");
            showToast(context, "Không thể đọc hoặc nén ảnh được chọn.");
            return "";
        }

        try {
            // 3. Upload lên Backend /upload API (Backend xử lý upload Cloudinary bảo mật)
            String backendUrl = uploadToBackend(context, compressedFile);
            if (backendUrl != null && !backendUrl.isEmpty() && backendUrl.length() <= 255) {
                Log.i(TAG, "Upload avatar thành công qua Backend: " + backendUrl);
                if (context != null) {
                    StaffAvatarCache.saveAvatar(context, null, null, backendUrl);
                }
                return backendUrl;
            }

        } catch (Exception e) {
            Log.w(TAG, "Lỗi upload ảnh qua Backend: " + e.getMessage());
            showToast(context, "Lỗi upload ảnh: " + e.getMessage());
        } finally {
            try {
                if (compressedFile.exists()) {
                    compressedFile.delete();
                }
            } catch (Exception ignored) {}
        }

        Log.w(TAG, "Upload qua Backend không thành công, trả về rỗng cho server để bảo vệ DB");
        return "";
    }

    @Nullable
    private static String uploadToBackend(Context context, File file) {
        try {
            RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/jpeg"));
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(), fileBody);

            RequestBody requestBody = builder.build();

            String uploadUrl = BuildConfig.BASE_URL + "upload";

            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .post(requestBody)
                    .build();

            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String bodyStr = response.body().string().trim();
                    JSONObject json = new JSONObject(bodyStr);
                    // Lấy url từ JSON response của backend (/upload)
                    String secureUrl = json.optString("url", "");
                    if (secureUrl.startsWith("http://") || secureUrl.startsWith("https://")) {
                        return secureUrl;
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "unknown";
                    Log.e(TAG, "Backend upload failed: code=" + response.code() + ", error=" + errorBody);
                    showToast(context, "Lỗi server (" + response.code() + "): " + errorBody);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Lỗi kết nối upload server: " + e.getMessage());
            showToast(context, "Lỗi kết nối server: " + e.getMessage());
        }
        return null;
    }

    private static void showToast(Context context, String message) {
        if (context == null) return;
        try {
            new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            );
        } catch (Exception ignored) {}
    }

    /**
     * Đọc và nén ảnh đại diện về kích thước chuẩn (tối đa 400x400, JPEG 85%)
     */
    @Nullable
    private static File compressImageToTempFile(Context context, String uriOrPath) {
        if (context == null || uriOrPath == null) return null;

        try {
            int rotationDegrees = 0;
            Uri uri = null;
            File localFile = null;

            if (uriOrPath.startsWith("content://")) {
                uri = Uri.parse(uriOrPath);
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
                if (uriOrPath.startsWith("file://")) {
                    localFile = new File(Uri.parse(uriOrPath).getPath());
                } else {
                    localFile = new File(uriOrPath);
                }
                if (localFile.exists()) {
                    try {
                        ExifInterface exif = new ExifInterface(localFile.getAbsolutePath());
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationDegrees = 90;
                        else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationDegrees = 180;
                        else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationDegrees = 270;
                    } catch (Exception ignored) {}
                }
            }

            // Downsampling
            int reqDim = 400;
            BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
            boundsOptions.inJustDecodeBounds = true;

            if (uri != null) {
                try (InputStream inBounds = context.getContentResolver().openInputStream(uri)) {
                    if (inBounds != null) {
                        BitmapFactory.decodeStream(inBounds, null, boundsOptions);
                    }
                }
            } else if (localFile != null && localFile.exists()) {
                try (InputStream inBounds = new java.io.FileInputStream(localFile)) {
                    BitmapFactory.decodeStream(inBounds, null, boundsOptions);
                }
            }

            int sampleSize = 1;
            if (boundsOptions.outWidth > reqDim || boundsOptions.outHeight > reqDim) {
                final int halfHeight = boundsOptions.outHeight / 2;
                final int halfWidth = boundsOptions.outWidth / 2;
                while ((halfHeight / sampleSize) >= reqDim && (halfWidth / sampleSize) >= reqDim) {
                    sampleSize *= 2;
                }
            }

            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            decodeOptions.inSampleSize = Math.max(1, sampleSize);

            Bitmap bitmap = null;
            if (uri != null) {
                try (InputStream in = context.getContentResolver().openInputStream(uri)) {
                    if (in != null) {
                        bitmap = BitmapFactory.decodeStream(in, null, decodeOptions);
                    }
                }
            } else if (localFile != null && localFile.exists()) {
                try (InputStream in = new java.io.FileInputStream(localFile)) {
                    bitmap = BitmapFactory.decodeStream(in, null, decodeOptions);
                }
            }

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

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > reqDim || height > reqDim) {
                float ratio = (float) width / (float) height;
                int targetW, targetH;
                if (ratio > 1) {
                    targetW = reqDim;
                    targetH = Math.max(1, (int) (reqDim / ratio));
                } else {
                    targetH = reqDim;
                    targetW = Math.max(1, (int) (reqDim * ratio));
                }
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true);
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
        }
    }
}

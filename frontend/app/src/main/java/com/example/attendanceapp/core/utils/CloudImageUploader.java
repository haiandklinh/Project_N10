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
 * Tiện ích tải ảnh đại diện lên Cloud Storage để nhận URL công khai ngắn (< 50 ký tự),
 * hoàn toàn vừa vặn với cột VARCHAR(255) trong CSDL Backend.
 */
public final class CloudImageUploader {

    private static final String TAG = "CloudImageUploader";
    private static final String CATBOX_API_URL = "https://catbox.moe/user/api.php";
    private static final String TMPFILES_API_URL = "https://tmpfiles.org/api/v1/upload";

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

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
     * Đảm bảo chuỗi trả về luôn <= 255 ký tự để không làm sập Database Backend (VARCHAR(255)).
     */
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

        // 1. Nếu đã là URL online (Cloudinary / Catbox / CDN) và <= 255 ký tự thì giữ nguyên
        if (clean.startsWith("http://") || clean.startsWith("https://")) {
            return clean.length() <= 255 ? clean : "";
        }

        // 2. Nén ảnh thành tệp tạm thời
        File compressedFile = compressImageToTempFile(context, clean);
        if (compressedFile == null || !compressedFile.exists()) {
            Log.w(TAG, "Không thể đọc và nén ảnh, trả về rỗng để tránh vượt 255 ký tự");
            return "";
        }

        try {
            // 3. Ưu tiên Upload lên Catbox (trả về https://files.catbox.moe/xxxxxx.jpg ~35 ký tự)
            String catboxUrl = uploadToCatbox(compressedFile);
            if (catboxUrl != null && !catboxUrl.isEmpty() && catboxUrl.length() <= 255) {
                Log.i(TAG, "Upload avatar thành công lên Catbox: " + catboxUrl);
                if (context != null) {
                    StaffAvatarCache.saveAvatar(context, null, null, catboxUrl);
                }
                return catboxUrl;
            }

            // 4. Fallback: Upload lên TmpFiles (trả về direct link https://tmpfiles.org/dl/...)
            String tmpUrl = uploadToTmpFiles(compressedFile);
            if (tmpUrl != null && !tmpUrl.isEmpty() && tmpUrl.length() <= 255) {
                Log.i(TAG, "Upload avatar thành công lên TmpFiles: " + tmpUrl);
                if (context != null) {
                    StaffAvatarCache.saveAvatar(context, null, null, tmpUrl);
                }
                return tmpUrl;
            }
        } catch (Exception e) {
            Log.w(TAG, "Lỗi upload ảnh lên cloud: " + e.getMessage());
        } finally {
            try {
                if (compressedFile.exists()) {
                    compressedFile.delete();
                }
            } catch (Exception ignored) {}
        }

        // 5. Nếu không có kết nối mạng ra ngoài, trả về rỗng để Backend không bị lỗi VARCHAR(255)
        Log.w(TAG, "Upload cloud không thành công, trả về rỗng cho server để bảo vệ DB");
        return "";
    }

    @Nullable
    private static String uploadToCatbox(File file) {
        try {
            RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/jpeg"));
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("reqtype", "fileupload")
                    .addFormDataPart("fileToUpload", file.getName(), fileBody)
                    .build();

            Request request = new Request.Builder()
                    .url(CATBOX_API_URL)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile)")
                    .post(requestBody)
                    .build();

            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String publicUrl = response.body().string().trim();
                    if (publicUrl.startsWith("http://") || publicUrl.startsWith("https://")) {
                        return publicUrl;
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Lỗi Catbox upload: " + e.getMessage());
        }
        return null;
    }

    @Nullable
    private static String uploadToTmpFiles(File file) {
        try {
            RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/jpeg"));
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(), fileBody)
                    .build();

            Request request = new Request.Builder()
                    .url(TMPFILES_API_URL)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile)")
                    .post(requestBody)
                    .build();

            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String bodyStr = response.body().string().trim();
                    JSONObject json = new JSONObject(bodyStr);
                    if ("success".equalsIgnoreCase(json.optString("status"))) {
                        JSONObject data = json.optJSONObject("data");
                        if (data != null) {
                            String url = data.optString("url");
                            if (url.contains("tmpfiles.org/")) {
                                return url.replace("tmpfiles.org/", "tmpfiles.org/dl/");
                            }
                            return url;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Lỗi TmpFiles upload: " + e.getMessage());
        }
        return null;
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

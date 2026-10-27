package com.example.attendanceapp.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Cache lưu trữ avatar của nhân viên theo Tên hoặc Số điện thoại
 * để đồng bộ nhanh và làm fallback khi backend trả về avatar null trong thông báo
 */
public final class StaffAvatarCache {

    private static final String PREF_NAME = "staff_avatar_cache";
    private static final String KEY_PREFIX_NAME = "avatar_by_name_";
    private static final String KEY_PREFIX_PHONE = "avatar_by_phone_";
    private static final String KEY_LATEST = "avatar_latest";

    private StaffAvatarCache() {}

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveAvatar(Context context, @Nullable String name, @Nullable String phone, @Nullable String avatarUri) {
        if (context == null || avatarUri == null || avatarUri.trim().isEmpty()) return;
        String cleanUri = avatarUri.trim();
        SharedPreferences.Editor editor = getPrefs(context).edit();

        editor.putString(KEY_LATEST, cleanUri);

        if (name != null && !name.trim().isEmpty()) {
            editor.putString(KEY_PREFIX_NAME + name.trim().toLowerCase(), cleanUri);
        }
        if (phone != null && !phone.trim().isEmpty()) {
            editor.putString(KEY_PREFIX_PHONE + phone.trim(), cleanUri);
        }
        editor.apply();
    }

    /**
     * Chuyển đổi Uri/File ảnh đại diện thành chuỗi Base64 thumbnail kích thước nhỏ (~4-6KB)
     * để gửi lên Backend lưu vào Notification, giúp Quản lý trên thiết bị khác cũng xem được ảnh.
     */
    @Nullable
    public static String encodeImageToBase64(@Nullable Context context, @Nullable String uriOrPath) {
        if (uriOrPath == null || uriOrPath.trim().isEmpty()) return null;
        String trimmed = uriOrPath.trim();
        if (trimmed.startsWith("data:image")) return trimmed;
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed;
        if (trimmed.equalsIgnoreCase("null") || trimmed.equalsIgnoreCase("none")) return null;

        java.io.InputStream in = null;
        try {
            if (trimmed.startsWith("content://") && context != null) {
                in = context.getContentResolver().openInputStream(android.net.Uri.parse(trimmed));
            } else {
                File file;
                if (trimmed.startsWith("file://")) {
                    file = new File(android.net.Uri.parse(trimmed).getPath());
                } else {
                    file = new File(trimmed);
                }
                if (file.exists()) {
                    in = new java.io.FileInputStream(file);
                }
            }

            if (in == null) return null;

            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(in);
            in.close();
            in = null;

            if (bitmap == null) return null;

            int maxDim = 160;
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
                android.graphics.Bitmap scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, width, height, true);
                if (scaled != bitmap) {
                    bitmap.recycle();
                    bitmap = scaled;
                }
            }

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, baos);
            byte[] bytes = baos.toByteArray();
            bitmap.recycle();

            return "data:image/jpeg;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        } finally {
            if (in != null) {
                try { in.close(); } catch (Exception ignored) {}
            }
        }
    }

    @Nullable
    public static String getAvatarByName(Context context, @Nullable String name) {
        if (context == null || name == null || name.trim().isEmpty()) return null;
        String uri = getPrefs(context).getString(KEY_PREFIX_NAME + name.trim().toLowerCase(), null);
        if (uri != null && isValidFile(uri)) {
            return uri;
        }
        return null;
    }

    @Nullable
    public static String getAvatarByPhone(Context context, @Nullable String phone) {
        if (context == null || phone == null || phone.trim().isEmpty()) return null;
        String uri = getPrefs(context).getString(KEY_PREFIX_PHONE + phone.trim(), null);
        if (uri != null && isValidFile(uri)) {
            return uri;
        }
        return null;
    }

    @Nullable
    public static String getLatestAvatar(Context context) {
        if (context == null) return null;
        String latest = getPrefs(context).getString(KEY_LATEST, null);
        if (latest != null && !latest.trim().isEmpty() && isValidFile(latest)) {
            return latest;
        }

        // Fallback: Quét tìm file ảnh mới nhất trong thư mục files/avatars
        try {
            File avatarDir = new File(context.getFilesDir(), "avatars");
            if (avatarDir.exists() && avatarDir.isDirectory()) {
                File[] files = avatarDir.listFiles((dir, fName) -> fName.endsWith(".jpg") || fName.endsWith(".png") || fName.endsWith(".jpeg"));
                if (files != null && files.length > 0) {
                    Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
                    return files[0].getAbsolutePath();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static boolean isValidFile(String pathOrUri) {
        if (pathOrUri == null || pathOrUri.isEmpty()) return false;
        if (pathOrUri.startsWith("http://") || pathOrUri.startsWith("https://")) return true;
        try {
            File file;
            if (pathOrUri.startsWith("file://")) {
                file = new File(android.net.Uri.parse(pathOrUri).getPath());
            } else {
                file = new File(pathOrUri);
            }
            return file.exists();
        } catch (Exception ignored) {
            return false;
        }
    }
}

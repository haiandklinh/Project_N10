package com.example.attendanceapp.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Base64;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.attendanceapp.R;
import com.example.attendanceapp.data.local.StaffAvatarCache;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Tiện ích đồng bộ và tải ảnh đại diện của nhân viên trên tất cả màn hình của Quản lý.
 * Hỗ trợ nhận diện mọi định dạng avatar từ API trả về:
 * - Base64 Data URI ("data:image/jpeg;base64,...") hoặc chuỗi Base64
 * - HTTP / HTTPS URL
 * - File URI ("file:///...")
 * - Đường dẫn tệp cục bộ (kể cả trường hợp đường dẫn lưu ở user space khác trên máy).
 */
public final class AvatarLoader {

    private AvatarLoader() {}

    public static void loadAvatar(@Nullable Context context, @Nullable String avatarUrl, @Nullable ImageView imageView) {
        loadAvatar(context, avatarUrl, null, imageView, R.drawable.ic_avatar);
    }

    public static void loadAvatar(@Nullable Context context, @Nullable String avatarUrl, @Nullable ImageView imageView, int placeholderResId) {
        loadAvatar(context, avatarUrl, null, imageView, placeholderResId);
    }

    public static void loadAvatar(@Nullable Context context, @Nullable String avatarUrl, @Nullable String staffName, @Nullable ImageView imageView) {
        loadAvatar(context, avatarUrl, staffName, imageView, R.drawable.ic_avatar);
    }

    public static void loadAvatar(@Nullable Context context, @Nullable String avatarUrl, @Nullable String staffName, @Nullable ImageView imageView, int placeholderResId) {
        if (imageView == null) return;
        if (context == null) {
            context = imageView.getContext();
        }
        if (context == null) {
            imageView.setImageResource(placeholderResId);
            return;
        }

        if (isInvalidAvatar(avatarUrl)) {
            // Tra cứu từ StaffAvatarCache theo Tên nhân viên hoặc avatar mới nhất
            String cached = StaffAvatarCache.getEffectiveAvatar(context, null, staffName, null);
            if (!isInvalidAvatar(cached)) {
                avatarUrl = cached;
            } else {
                imageView.setImageResource(placeholderResId);
                return;
            }
        }

        String trimmed = avatarUrl.trim();

        // 1. Base64 Image (thường trả về trong notification content của API)
        if (trimmed.startsWith("data:image") || (!trimmed.startsWith("http") && !trimmed.startsWith("file") && !trimmed.startsWith("content") && trimmed.length() > 150)) {
            try {
                String cleanBase64 = trimmed;
                if (cleanBase64.contains(",")) {
                    cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
                }
                byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
                if (decodedBytes != null && decodedBytes.length > 0) {
                    // Caching local file để các màn hình khác dùng filename có thể truy xuất nhanh
                    saveDecodedBytesToCache(context, decodedBytes);
                    if (staffName != null && !staffName.trim().isEmpty()) {
                        StaffAvatarCache.saveAvatar(context, staffName, null, trimmed);
                    }

                    Glide.with(context)
                            .asBitmap()
                            .load(decodedBytes)
                            .placeholder(placeholderResId)
                            .error(placeholderResId)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(imageView);
                    return;
                }
            } catch (Exception ignored) {}
        }

        // 2. HTTP / HTTPS URL (nếu tải lỗi 404/mạng thì fallback sang ảnh cục bộ đã lưu)
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            final Context appContext = context.getApplicationContext();
            Glide.with(context)
                    .load(trimmed)
                    .placeholder(placeholderResId)
                    .error(placeholderResId)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(
                                @Nullable GlideException e,
                                Object model,
                                Target<Drawable> target,
                                boolean isFirstResource
                        ) {
                            String localFallback = StaffAvatarCache.getEffectiveAvatar(appContext, null, staffName, null);
                            if (!isInvalidAvatar(localFallback)
                                    && !localFallback.startsWith("http://")
                                    && !localFallback.startsWith("https://")) {
                                imageView.post(() -> loadAvatar(appContext, localFallback, staffName, imageView, placeholderResId));
                                return true;
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(
                                Drawable resource,
                                Object model,
                                Target<Drawable> target,
                                DataSource dataSource,
                                boolean isFirstResource
                        ) {
                            if (staffName != null && !staffName.trim().isEmpty()) {
                                StaffAvatarCache.saveAvatar(appContext, staffName, null, trimmed);
                            }
                            return false;
                        }
                    })
                    .into(imageView);
            return;
        }

        if (staffName != null && !staffName.trim().isEmpty()) {
            StaffAvatarCache.saveAvatar(context, staffName, null, trimmed);
        }

        // 3. Content Uri
        if (trimmed.startsWith("content://")) {
            try {
                Glide.with(context)
                        .load(Uri.parse(trimmed))
                        .placeholder(placeholderResId)
                        .error(placeholderResId)
                        .into(imageView);
                return;
            } catch (Exception ignored) {}
        }

        // 4. File URI hoặc Local File Path (API trả về đường dẫn file:///...)
        File targetFile = null;
        if (trimmed.startsWith("file://")) {
            try {
                targetFile = new File(Uri.parse(trimmed).getPath());
            } catch (Exception ignored) {}
        } else if (trimmed.startsWith("/")) {
            targetFile = new File(trimmed);
        }

        if (targetFile != null && targetFile.exists() && targetFile.canRead()) {
            Glide.with(context)
                    .load(targetFile)
                    .placeholder(placeholderResId)
                    .error(placeholderResId)
                    .into(imageView);
            return;
        }

        // 5. Xử lý đường dẫn khác User Space (Ví dụ: path chứa /data/user/999/... nhưng Quản lý chạy ở /data/user/0/...)
        // Trích xuất tên file (ví dụ: avatar_1792874613381.jpg) và tìm trong thư mục files/avatars của ứng dụng hiện tại
        String fileName = null;
        if (targetFile != null) {
            fileName = targetFile.getName();
        } else if (trimmed.contains("/")) {
            fileName = trimmed.substring(trimmed.lastIndexOf('/') + 1);
        } else {
            fileName = trimmed;
        }

        if (fileName != null && !fileName.isEmpty() && (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".jpeg") || fileName.startsWith("avatar_"))) {
            // Tìm trong context.getFilesDir()/avatars/
            File localAvatarFile = new File(new File(context.getFilesDir(), "avatars"), fileName);
            if (localAvatarFile.exists() && localAvatarFile.canRead()) {
                Glide.with(context)
                        .load(localAvatarFile)
                        .placeholder(placeholderResId)
                        .error(placeholderResId)
                        .into(imageView);
                return;
            }

            // Tìm trong context.getFilesDir()
            File rootFile = new File(context.getFilesDir(), fileName);
            if (rootFile.exists() && rootFile.canRead()) {
                Glide.with(context)
                        .load(rootFile)
                        .placeholder(placeholderResId)
                        .error(placeholderResId)
                        .into(imageView);
                return;
            }

            // Tìm trong cacheDir/avatars
            File cacheAvatarFile = new File(new File(context.getCacheDir(), "avatars"), fileName);
            if (cacheAvatarFile.exists() && cacheAvatarFile.canRead()) {
                Glide.with(context)
                        .load(cacheAvatarFile)
                        .placeholder(placeholderResId)
                        .error(placeholderResId)
                        .into(imageView);
                return;
            }
        }

        // 6. Thử load trực tiếp chuỗi qua Glide làm fallback
        try {
            Glide.with(context)
                    .load(trimmed)
                    .placeholder(placeholderResId)
                    .error(placeholderResId)
                    .into(imageView);
        } catch (Exception e) {
            imageView.setImageResource(placeholderResId);
        }
    }

    private static boolean isInvalidAvatar(@Nullable String value) {
        if (value == null) return true;
        String trimmed = value.trim();
        return trimmed.isEmpty()
                || "null".equalsIgnoreCase(trimmed)
                || "none".equalsIgnoreCase(trimmed)
                || "string".equalsIgnoreCase(trimmed);
    }

    private static void saveDecodedBytesToCache(Context context, byte[] bytes) {
        if (context == null || bytes == null || bytes.length == 0) return;
        new Thread(() -> {
            try {
                File dir = new File(context.getFilesDir(), "avatars");
                if (!dir.exists()) dir.mkdirs();
                File target = new File(dir, "cached_avatar_" + Math.abs(java.util.Arrays.hashCode(bytes)) + ".jpg");
                if (!target.exists()) {
                    try (FileOutputStream fos = new FileOutputStream(target)) {
                        fos.write(bytes);
                        fos.flush();
                    }
                }
            } catch (Exception ignored) {}
        }).start();
    }
}

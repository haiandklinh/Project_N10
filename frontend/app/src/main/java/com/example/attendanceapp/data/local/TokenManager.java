package com.example.attendanceapp.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * TokenManager sử dụng EncryptedSharedPreferences để lưu token an toàn.
 * Mã hóa AES256-GCM bảo vệ token khỏi bị đọc khi device bị root.
 */
@Singleton
public class TokenManager {

    private static final String TAG = "TokenManager";
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    private final SharedPreferences sharedPreferences;

    @Inject
    public TokenManager(@ApplicationContext Context context) {
        this.sharedPreferences = initEncryptedSharedPreferences(context);
    }

    private SharedPreferences initEncryptedSharedPreferences(Context context) {
        try {
            return createEncryptedPrefs(context);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi giải mã Keystore/EncryptedSharedPreferences. Tiến hành xoá file cũ và tạo lại.", e);

            // 1. Xoá file SharedPreferences bị lệch Key
            deleteCorruptedPrefsFile(context);

            // 2. Thử tạo lại lần 2 sau khi xoá file hỏng
            try {
                return createEncryptedPrefs(context);
            } catch (Exception ex) {
                Log.e(TAG, "Không thể tạo EncryptedSharedPreferences, fallback sang SharedPreferences thường.", ex);
                // Fallback phòng trường hợp phần cứng thiết bị không hỗ trợ KeyStore mã hoá
                return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            }
        }
    }

    private SharedPreferences createEncryptedPrefs(Context context) throws Exception {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    private void deleteCorruptedPrefsFile(Context context) {
        try {
            File dir = new File(context.getApplicationInfo().dataDir + "/shared_prefs");
            File file = new File(dir, PREF_NAME + ".xml");
            if (file.exists()) {
                boolean deleted = file.delete();
                Log.d(TAG, "Kết quả xoá file XML hỏng: " + deleted);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xoá file SharedPreferences hỏng", e);
        }
    }

    public boolean hasToken() {
        String token = getAccessToken();
        return token != null && !token.trim().isEmpty();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }

    public void saveTokens(String accessToken, String refreshToken) {
        sharedPreferences.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply();
    }

    public void clearTokens() {
        sharedPreferences.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .apply();
    }
}
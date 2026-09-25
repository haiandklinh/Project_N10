package com.example.attendanceapp.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * LƯU Ý: đây là bản mẫu dựng lại theo Hilt, dựa trên giả định TokenManager của bạn
 * dùng SharedPreferences. Hãy ghép các trường/khóa/logic thật của bạn vào đây,
 * điểm quan trọng là:
 *  - Bỏ pattern getInstance(context) thủ công.
 *  - Dùng @Inject constructor + @ApplicationContext để Hilt tự cấp phát,
 *    tránh leak Activity context.
 */
@Singleton
public class TokenManager {

    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    private final SharedPreferences sharedPreferences;

    @Inject
    public TokenManager(@ApplicationContext Context context) {
        // @ApplicationContext đảm bảo luôn dùng application context,
        // không bao giờ leak Activity context.
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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
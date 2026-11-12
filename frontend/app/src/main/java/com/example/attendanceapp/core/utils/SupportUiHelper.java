package com.example.attendanceapp.core.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.pm.PackageInfoCompat;

import com.example.attendanceapp.BuildConfig;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/**
 * Helper dùng chung cho các màn hình Giới thiệu ứng dụng và Trung tâm trợ giúp
 * (hiển thị phiên bản, mở email/hotline hỗ trợ, tìm kiếm và đóng/mở FAQ).
 */
public final class SupportUiHelper {

    public static final String DEFAULT_SUPPORT_PHONE = "0987654321";
    public static final String DEFAULT_SUPPORT_EMAIL = "attendanceapp2026@gmail.com";

    private SupportUiHelper() {
    }

    public static final class FaqItem {
        public final View card;
        public final View answerView;
        public final String searchableText;

        public FaqItem(@NonNull View card, @NonNull View answerView, @NonNull String question, @NonNull String answer) {
            this.card = card;
            this.answerView = answerView;
            this.searchableText = normalize(question + " " + answer);
        }
    }

    public static void bindAppVersion(@NonNull Context context, @NonNull TextView versionView) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String versionName = pInfo.versionName != null ? pInfo.versionName : BuildConfig.VERSION_NAME;
            long versionCode = PackageInfoCompat.getLongVersionCode(pInfo);
            versionView.setText("Phiên bản " + versionName + " (Build " + versionCode + ")");
        } catch (Exception e) {
            versionView.setText("Phiên bản " + BuildConfig.VERSION_NAME + " (Build " + BuildConfig.VERSION_CODE + ")");
        }
    }

    @NonNull
    public static String resolveOrDefault(@Nullable String value, @NonNull String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    public static void dialPhone(@NonNull Context context, @NonNull String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber.trim()));
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Không thể thực hiện cuộc gọi", Toast.LENGTH_SHORT).show();
        }
    }

    public static void sendSupportEmail(@NonNull Context context, @NonNull String email, @NonNull String subject) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + email.trim()));
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Không tìm thấy ứng dụng email trên thiết bị", Toast.LENGTH_SHORT).show();
        }
    }

    public static void setupFaqSearchAndToggle(
            @NonNull EditText searchInput,
            @Nullable View emptyView,
            @NonNull List<FaqItem> faqItems
    ) {
        for (FaqItem item : faqItems) {
            item.card.setOnClickListener(v -> {
                boolean isVisible = item.answerView.getVisibility() == View.VISIBLE;
                item.answerView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            });
        }

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = normalize(s != null ? s.toString() : "");
                int visibleCount = 0;
                for (FaqItem item : faqItems) {
                    boolean matches = query.isEmpty() || item.searchableText.contains(query);
                    item.card.setVisibility(matches ? View.VISIBLE : View.GONE);
                    if (matches) {
                        visibleCount++;
                    }
                }
                if (emptyView != null) {
                    emptyView.setVisibility(visibleCount == 0 ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private static String normalize(@NonNull String input) {
        String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}

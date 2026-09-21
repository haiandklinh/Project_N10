package com.example.attendanceapp.ui.employee.profile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.attendanceapp.databinding.LayoutDarkModeCurtainBinding;

public class DarkModeAnimationHelper {

    public static final String EXTRA_START_OPEN_CURTAIN = "extra_start_open_curtain";

    /**
     * BƯỚC 1: Đóng rèm kín màn hình -> Hiện logo -> Đổi Mode & Recreate
     */
    public static void startCloseCurtainAnimation(Activity activity, boolean isDarkMode) {
        ViewGroup rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        LayoutDarkModeCurtainBinding binding = LayoutDarkModeCurtainBinding.inflate(
                LayoutInflater.from(activity), rootView, false);

        int themeSurfaceColor = getThemeSurfaceColor(activity);
        binding.curtainLeft.setBackgroundColor(themeSurfaceColor);
        binding.curtainRight.setBackgroundColor(themeSurfaceColor);

        rootView.addView(binding.getRoot());

        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        float halfWidth = screenWidth / 2f;

        binding.curtainLeft.setTranslationX(-halfWidth);
        binding.curtainRight.setTranslationX(halfWidth);

        // 1. Đóng rèm vào giữa (600ms)
        binding.curtainLeft.animate()
                .translationX(0)
                .setDuration(600)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        binding.curtainRight.animate()
                .translationX(0)
                .setDuration(600)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // 2. Hiện Card Logo ở giữa (400ms)
                        binding.cardDarkModeLogo.animate()
                                .alpha(1.0f)
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(400)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        // 3. Đánh dấu flag và Recreate Activity để đổi Theme
                                        activity.getIntent().putExtra(EXTRA_START_OPEN_CURTAIN, true);
                                        AppCompatDelegate.setDefaultNightMode(
                                                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                                    }
                                })
                                .start();
                    }
                })
                .start();
    }

    /**
     * BƯỚC 2: Gọi sau khi Activity recreate -> Kéo rèm ra 2 bên hiển thị màn hình mới
     */
    public static void checkAndRunOpenCurtainAnimation(Activity activity) {
        Intent intent = activity.getIntent();
        if (intent != null && intent.getBooleanExtra(EXTRA_START_OPEN_CURTAIN, false)) {
            // Xóa cờ để không bị lặp lại
            intent.removeExtra(EXTRA_START_OPEN_CURTAIN);

            ViewGroup rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
            LayoutDarkModeCurtainBinding binding = LayoutDarkModeCurtainBinding.inflate(
                    LayoutInflater.from(activity), rootView, false);

            // Gán màu Surface chuẩn của Theme MỚI vừa đổi
            int themeSurfaceColor = getThemeSurfaceColor(activity);
            binding.curtainLeft.setBackgroundColor(themeSurfaceColor);
            binding.curtainRight.setBackgroundColor(themeSurfaceColor);

            rootView.addView(binding.getRoot());

            int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
            float halfWidth = screenWidth / 2f;

            // Rèm ban đầu ở trạng thái ĐÓNG KÍN
            binding.curtainLeft.setTranslationX(0);
            binding.curtainRight.setTranslationX(0);
            binding.cardDarkModeLogo.setAlpha(1.0f);
            binding.cardDarkModeLogo.setScaleX(1.0f);
            binding.cardDarkModeLogo.setScaleY(1.0f);

            // 1. Ẩn Logo (300ms)
            binding.cardDarkModeLogo.animate()
                    .alpha(0f)
                    .scaleX(0.3f)
                    .scaleY(0.3f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            // 2. KÉO RÈM RA 2 BÊN CHẬM RÃI (700ms)
                            binding.curtainLeft.animate()
                                    .translationX(-halfWidth)
                                    .setDuration(700)
                                    .setInterpolator(new AccelerateDecelerateInterpolator())
                                    .start();

                            binding.curtainRight.animate()
                                    .translationX(halfWidth)
                                    .setDuration(700)
                                    .setInterpolator(new AccelerateDecelerateInterpolator())
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            rootView.removeView(binding.getRoot());
                                        }
                                    })
                                    .start();
                        }
                    })
                    .start();
        }
    }

    private static int getThemeSurfaceColor(Activity activity) {
        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true);
        return typedValue.data;
    }
}
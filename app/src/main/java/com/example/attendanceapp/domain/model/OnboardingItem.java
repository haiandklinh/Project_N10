package com.example.attendanceapp.domain.model;

import androidx.annotation.DrawableRes;

/**
 * Model dữ liệu cho một trang onboarding.
 */
public class OnboardingItem {

    @DrawableRes
    private final int imageRes;
    private final String title;
    private final String description;

    public OnboardingItem(@DrawableRes int imageRes, String title, String description) {
        this.imageRes = imageRes;
        this.title = title;
        this.description = description;
    }

    public int getImageRes() {
        return imageRes;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
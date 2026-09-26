package com.example.attendanceapp.core.utils;

import java.util.Locale;

// utils/TextFormatUtils.java
public final class TextFormatUtils {

    private TextFormatUtils() {} // không cho tạo instance, chỉ dùng static

    //Viết hoa chữu cái đầu
    public static String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) return text;
        //Cắt lấy chữ cái đầu tiên của chuỗi text (từ vị trí 0 đến trước vị trí 1).
        return text.substring(0, 1).toUpperCase(new Locale("vi", "VN")) + text.substring(1);
    }
}
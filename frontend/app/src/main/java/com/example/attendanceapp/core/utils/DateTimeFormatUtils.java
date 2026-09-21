package com.example.attendanceapp.core.utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DateTimeFormatUtils {

    // Thống nhất cách khởi tạo Locale chuẩn cho Tiếng Việt
    private static final Locale VIETNAM_LOCALE = Locale.forLanguageTag("vi-VN");

    private static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("d 'tháng' M", VIETNAM_LOCALE);
    private static final DateTimeFormatter TIME_24H_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FULL_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, dd 'tháng' MM, yyyy", VIETNAM_LOCALE);

    private DateTimeFormatUtils() {}

    /**
     * SỬA LỖI 1: Thêm 'static' và chuyển modifier thành 'public' (nếu muốn dùng ở ngoài)
     * hoặc giữ 'private static' (nếu chỉ dùng nội bộ lớp này).
     */
    public static String formatDateDayMonth(LocalDate date) {
        if (date == null) {
            return "";
        }
        if (date.isEqual(LocalDate.now())) {
            return "Hôm nay";
        }
        return date.format(SHORT_DATE_FORMATTER);
    }

    public static String formatTodayLabel(LocalDate date) {
        if (date == null) return "";
        return TextFormatUtils.capitalizeFirstLetter(date.format(FULL_DATE_FORMATTER));
    }

    public static String formatTime(LocalTime time) {
        if (time == null) return "--:--";
        return time.format(TIME_24H_FORMATTER);
    }

    public static String formatWorkedHours(LocalTime checkInTime, LocalTime checkOutTime) {
        if (checkInTime == null) return "0giờ 00phút";

        LocalTime now = LocalTime.now();
        LocalTime endTime = (checkOutTime != null) ? checkOutTime : (now.isAfter(checkInTime) ? now : checkInTime);

        Duration duration = Duration.between(checkInTime, endTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;

        return String.format(Locale.getDefault(), "%dgiờ %02dphút", hours, minutes);
    }
}
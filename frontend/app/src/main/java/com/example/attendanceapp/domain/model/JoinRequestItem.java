package com.example.attendanceapp.domain.model;

import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Yêu cầu tham gia doanh nghiệp của một nhân viên (nhập mã mời) đang chờ.
 * Quản lý phê duyệt. Xem use case "Phê duyệt yêu cầu tham gia".
 */
public class JoinRequestItem {

    private final String id;
    private final String fullName;
    private final String avatarUrl;
    private final String phoneNumber;
    private final String invitationCode;
    private final long requestedAtTimestamp; // Lưu theo milliseconds
    private String requestedAtLabel;          // "5 phút trước", "Hôm qua"...
    private ApprovalStatus status;

    // Constructor nhận timestamp millis
    public JoinRequestItem(String id, String fullName, String avatarUrl, String phoneNumber,
                           String invitationCode, long requestedAtTimestamp, ApprovalStatus status) {
        this.id = id;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.phoneNumber = phoneNumber;
        this.invitationCode = invitationCode;
        this.requestedAtTimestamp = requestedAtTimestamp;
        this.requestedAtLabel = formatRelativeTime(requestedAtTimestamp);
        this.status = status;
    }

    // Constructor bổ sung: Nhận chuỗi ISO-8601 (ví dụ từ API: "2026-10-30T09:15:00Z")
    public JoinRequestItem(String id, String fullName, String avatarUrl, String phoneNumber,
                           String invitationCode, String isoTimeString, ApprovalStatus status) {
        this.id = id;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.phoneNumber = phoneNumber;
        this.invitationCode = invitationCode;
        this.requestedAtTimestamp = parseIsoToMillis(isoTimeString);
        this.requestedAtLabel = formatRelativeTime(this.requestedAtTimestamp);
        this.status = status;
    }

    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getInvitationCode() { return invitationCode; }
    public long getRequestedAtTimestamp() { return requestedAtTimestamp; }

    public String getRequestedAtLabel() {
        if (requestedAtTimestamp > 0) {
            return formatRelativeTime(requestedAtTimestamp);
        }
        return requestedAtLabel;
    }

    public void setRequestedAtLabel(String requestedAtLabel) {
        this.requestedAtLabel = requestedAtLabel;
    }

    public ApprovalStatus getStatus() { return status; }
    public void setStatus(ApprovalStatus status) { this.status = status; }

    /**
     * Chuyển đổi timestamp (millis) sang chuỗi thời gian tương đối
     */
    public static String formatRelativeTime(long timeInMillis) {
        if (timeInMillis <= 0) return "";

        long now = System.currentTimeMillis();
        long diff = now - timeInMillis;

        if (diff < 0) diff = 0;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) {
            return "Vừa xong";
        } else if (minutes < 60) {
            return minutes + " phút trước";
        } else if (hours < 24) {
            return hours + " giờ trước";
        } else if (days < 30) {
            return days + " ngày trước";
        } else {
            return DateUtils.getRelativeTimeSpanString(
                    timeInMillis,
                    now,
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString();
        }
    }

    /**
     * Parse chuỗi ISO-8601 sang milliseconds
     */
    public static long parseIsoToMillis(String isoTimeString) {
        if (isoTimeString == null || isoTimeString.isEmpty()) {
            return System.currentTimeMillis();
        }

        String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss"
        };

        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = sdf.parse(isoTimeString);
                if (date != null) {
                    return date.getTime();
                }
            } catch (ParseException ignored) {
            }
        }
        return System.currentTimeMillis();
    }
}
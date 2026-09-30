package com.example.attendanceapp.domain.model;

/**
 * Yêu cầu tham gia doanh nghiệp của một nhân viên (nhập mã mời) đang chờ
 * Quản lý phê duyệt. Xem use case "Phê duyệt yêu cầu tham gia".
 */
public class JoinRequestItem {

    private final String id;
    private final String fullName;
    private final String avatarUrl;
    private final String phoneNumber;
    private final String invitationCode;
    private final String requestedAtLabel; // "5 phút trước", "Hôm qua"...
    private ApprovalStatus status;

    public JoinRequestItem(String id, String fullName, String avatarUrl, String phoneNumber,
                           String invitationCode, String requestedAtLabel, ApprovalStatus status) {
        this.id = id;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.phoneNumber = phoneNumber;
        this.invitationCode = invitationCode;
        this.requestedAtLabel = requestedAtLabel;
        this.status = status;
    }

    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getInvitationCode() { return invitationCode; }
    public String getRequestedAtLabel() { return requestedAtLabel; }
    public ApprovalStatus getStatus() { return status; }
    public void setStatus(ApprovalStatus status) { this.status = status; }
}
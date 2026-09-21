package com.example.attendanceapp.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceAdjustment {
    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    private String id;
    private String reasonType; // Ví dụ: "Thiếu check-in", "Thiếu check-out", "Bổ sung công"
    private LocalDate requestDate; // Ngày cần điều chỉnh (VD: 10/09/2026)
    private LocalTime checkInTime; // Thời gian check-in mới/đề xuất (có thể null)
    private LocalTime checkOutTime; // Thời gian check-out mới/đề xuất (có thể null)
    private String reasonDetail; // Lý do chi tiết
    private LocalDate createdDate; // Ngày gửi yêu cầu (VD: 11/09/2026)
    private Status status;

    public AttendanceAdjustment(String id, String reasonType, LocalDate requestDate, LocalTime checkInTime, LocalTime checkOutTime, String reasonDetail, LocalDate createdDate, Status status) {
        this.id = id;
        this.reasonType = reasonType;
        this.requestDate = requestDate;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.reasonDetail = reasonDetail;
        this.createdDate = createdDate;
        this.status = status;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getReasonType() { return reasonType; }
    public LocalDate getRequestDate() { return requestDate; }
    public LocalTime getCheckInTime() { return checkInTime; }
    public LocalTime getCheckOutTime() { return checkOutTime; }
    public String getReasonDetail() { return reasonDetail; }
    public LocalDate getCreatedDate() { return createdDate; }
    public Status getStatus() { return status; }
}
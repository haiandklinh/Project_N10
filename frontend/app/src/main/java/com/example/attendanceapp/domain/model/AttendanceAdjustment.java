package com.example.attendanceapp.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceAdjustment {

    private final String id;
    private final AdjustmentReasonType reasonType; // Enum thay cho String
    private final LocalDate requestDate;            // Ngày cần điều chỉnh
    private final LocalTime checkInTime;           // Giờ check-in đề xuất
    private final LocalTime checkOutTime;          // Giờ check-out đề xuất
    private final String reasonDetail;             // Lý do chi tiết
    private final LocalDate createdDate;           // Ngày gửi đơn
    private ApprovalStatus status;                 // Enum ApprovalStatus dùng chung

    public AttendanceAdjustment(String id, AdjustmentReasonType reasonType, LocalDate requestDate,
                                LocalTime checkInTime, LocalTime checkOutTime,
                                String reasonDetail, LocalDate createdDate, ApprovalStatus status) {
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
    public AdjustmentReasonType getReasonType() { return reasonType; }
    public LocalDate getRequestDate() { return requestDate; }
    public LocalTime getCheckInTime() { return checkInTime; }
    public LocalTime getCheckOutTime() { return checkOutTime; }
    public String getReasonDetail() { return reasonDetail; }
    public LocalDate getCreatedDate() { return createdDate; }
    public ApprovalStatus getStatus() { return status; }
    public void setStatus(ApprovalStatus status) { this.status = status; }
}
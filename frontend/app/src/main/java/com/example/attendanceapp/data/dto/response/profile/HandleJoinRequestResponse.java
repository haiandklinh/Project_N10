package com.example.attendanceapp.data.dto.response.profile;

import com.google.gson.annotations.SerializedName;
import androidx.annotation.Nullable;

public class HandleJoinRequestResponse {

    @SerializedName("request_id")
    private String requestId;

    @SerializedName("status")
    private String status; // "APPROVED" hoặc "REJECTED"

    @Nullable
    @SerializedName("message")
    private String message; // Thông báo từ Server (VD: "Đã duyệt nhân viên thành công")

    public HandleJoinRequestResponse() {
    }

    public HandleJoinRequestResponse(String requestId, String status) {
        this.requestId = requestId;
        this.status = status;
    }

    public HandleJoinRequestResponse(String requestId, String status, @Nullable String message) {
        this.requestId = requestId;
        this.status = status;
        this.message = message;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    public void setMessage(@Nullable String message) {
        this.message = message;
    }
}
package com.example.attendanceapp.data.dto.response.profile;

import com.google.gson.annotations.SerializedName;

public class HandleJoinRequestResponse {
    @SerializedName("request_id")
    private String requestId;

    @SerializedName("status")
    private String status; // APPROVED | REJECTED

    public HandleJoinRequestResponse() {
    }

    public HandleJoinRequestResponse(String requestId, String status) {
        this.requestId = requestId;
        this.status = status;
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
}

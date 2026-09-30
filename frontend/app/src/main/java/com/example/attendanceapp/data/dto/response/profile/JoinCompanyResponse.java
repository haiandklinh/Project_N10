package com.example.attendanceapp.data.dto.response.profile;

import com.google.gson.annotations.SerializedName;

public class JoinCompanyResponse {
    @SerializedName("request_id")
    private String requestId;
    private String status;
    private String message;

    public String getRequestId() { return requestId; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
}
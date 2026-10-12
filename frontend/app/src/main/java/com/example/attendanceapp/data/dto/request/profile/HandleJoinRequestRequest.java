package com.example.attendanceapp.data.dto.request.profile;

import com.google.gson.annotations.SerializedName;

public class HandleJoinRequestRequest {
    @SerializedName("action")
    private final String action; // "APPROVE" | "REJECT"
    public HandleJoinRequestRequest(boolean approve) {
        this.action = approve ? "APPROVE" : "REJECT";
    }
}
package com.example.attendanceapp.data.dto.response.attendance;

import com.google.gson.annotations.SerializedName;

public class AttendanceMessageResponse {
    @SerializedName("message")
    private String message;

    public AttendanceMessageResponse() {
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
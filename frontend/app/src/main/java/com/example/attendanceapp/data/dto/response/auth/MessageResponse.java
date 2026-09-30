package com.example.attendanceapp.data.dto.response.auth;
import com.google.gson.annotations.SerializedName;

public class MessageResponse {

    @SerializedName("message")
    private String message;
    private String detail; // Trong trường hợp nhận lỗi 400/401

    public MessageResponse() {
    }

    public MessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
    public String getDetail() { return detail; }
}

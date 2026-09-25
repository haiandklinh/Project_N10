package com.example.attendanceapp.data.dto.response;

public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public T getData() { return data; }

    // Helper tiện dùng, không map JSON
    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }
}
package com.example.attendanceapp.data.dto.response.profile;

import com.google.gson.annotations.SerializedName;

//Đây là DTO (Data Transfer Object) dùng để hứng dữ liệu trả về
// từ API lấy/tạo mã OTP (mã mời gia nhập công ty hoặc mã xác thực) dành cho Quản lý (Manager).
public class GetOtpResponse {
    private String otp;

    //Thời gian hiệu lực của mã OTP tính theo giây
    @SerializedName("expires_in")
    private int expiresIn;

    //message: Thông báo trả về từ server (ví dụ: "Tạo mã OTP thành công").
    private String message;

    public String getOtp() { return otp; }
    public int getExpiresIn() { return expiresIn; }
    public String getMessage() { return message; }
}
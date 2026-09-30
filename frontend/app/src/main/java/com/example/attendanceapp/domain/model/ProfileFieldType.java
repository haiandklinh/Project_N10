package com.example.attendanceapp.domain.model;

public enum ProfileFieldType {
    FULL_NAME("Họ và tên"),
    PHONE_NUMBER("Số điện thoại"),
    BANK_ACCOUNT("Số tài khoản ngân hàng"),
    IDENTITY_CARD("Căn cước công dân"),
    ADDRESS("Địa chỉ thường trú");

    private final String title;

    ProfileFieldType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
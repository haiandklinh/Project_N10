package com.example.attendanceapp.data.dto.response;

import com.google.gson.annotations.SerializedName;

public class CheckPhoneResponse {
    @SerializedName("exist")
    private boolean exist;

    public CheckPhoneResponse() {}

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }
}
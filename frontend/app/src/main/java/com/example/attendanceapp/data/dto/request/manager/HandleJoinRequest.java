package com.example.attendanceapp.data.dto.request.manager;

import com.google.gson.annotations.SerializedName;

public class HandleJoinRequest {
    @SerializedName("id_staff")
    private String staffId;

    @SerializedName("position")
    private String position;

    @SerializedName("status")
    private boolean status; // true: Đồng ý gia nhập / Accepted, false: Từ chối / Rejected

    public HandleJoinRequest() {
    }

    public HandleJoinRequest(String staffId, String position, boolean status) {
        this.staffId = staffId;
        this.position = position;
        this.status = status;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
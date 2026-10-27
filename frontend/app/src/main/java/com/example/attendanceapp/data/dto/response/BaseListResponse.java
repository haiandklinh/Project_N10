package com.example.attendanceapp.data.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BaseListResponse<T> {
    @SerializedName(value = "data", alternate = {"notifications", "items", "results"})
    private List<T> data;

    public BaseListResponse() {
    }

    public BaseListResponse(List<T> data) {
        this.data = data;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}

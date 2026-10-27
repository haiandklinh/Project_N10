package com.example.attendanceapp.data.dto.response.attendance;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AttendanceRecordPageResponse {
    @SerializedName("data")
    private List<AttendanceRecordItemResponse> data;

    @SerializedName("total_items")
    private int totalItems;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("current_page")
    private int currentPage;

    @SerializedName("size")
    private int size;

    public AttendanceRecordPageResponse() {
    }

    public List<AttendanceRecordItemResponse> getData() { return data; }
    public void setData(List<AttendanceRecordItemResponse> data) { this.data = data; }

    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
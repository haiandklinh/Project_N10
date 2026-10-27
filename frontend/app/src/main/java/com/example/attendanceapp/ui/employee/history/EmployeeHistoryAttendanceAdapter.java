package com.example.attendanceapp.ui.employee.history;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.ItemHistoryAttendanceBinding; // Tên binding theo layout XML 2 của bạn
import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.HistoryAttendance;
import com.google.android.material.color.MaterialColors;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class EmployeeHistoryAttendanceAdapter
        extends RecyclerView.Adapter<EmployeeHistoryAttendanceAdapter.HistoryAttendanceViewHolder> {

    private final List<HistoryAttendance> attendanceList;

    // Format: "Thứ Hai, 14/09/2026"
    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale.forLanguageTag("vi"));

    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("HH:mm");

    public EmployeeHistoryAttendanceAdapter(List<HistoryAttendance> attendanceList) {
        this.attendanceList = attendanceList;
    }

    public void updateData(List<HistoryAttendance> newItems) {
        this.attendanceList.clear();
        if (newItems != null) {
            this.attendanceList.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryAttendanceViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        ItemHistoryAttendanceBinding binding =
                ItemHistoryAttendanceBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );

        return new HistoryAttendanceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull HistoryAttendanceViewHolder holder,
            int position) {

        HistoryAttendance item = attendanceList.get(position);

        // 1. Ngày tháng kèm ca làm việc
        String dayText = formatDate(item.getDate());
        if (item.getShiftName() != null && !item.getShiftName().trim().isEmpty()) {
            dayText += " · " + item.getShiftName().trim();
        }
        holder.binding.tvRecentDay.setText(dayText);

        // 2. Check-in & Check-out
        holder.binding.tvRecentCheckIn.setText(formatTime(item.getCheckIn()));
        holder.binding.tvRecentCheckOut.setText(formatTime(item.getCheckOut()));

        // 3. Số giờ làm việc (Ẩn card nếu chưa check-out đủ)
        if (item.getCheckIn() != null && item.getCheckOut() != null) {
            holder.binding.cardTotalHours.setVisibility(View.VISIBLE);
            holder.binding.tvTotalHours.setText(item.getShortFormattedTotalTime());
        } else {
            holder.binding.cardTotalHours.setVisibility(View.GONE);
        }

        // 4. Vị trí ca làm việc thực tế
        holder.binding.cardRecentRecord.setVisibility(View.VISIBLE);
        String locationText = item.getLocation();
        if (locationText == null || locationText.trim().isEmpty()) {
            locationText = "Vị trí ca làm việc thực tế";
        }
        holder.binding.layoutLocationInfo.setVisibility(View.VISIBLE);
        holder.binding.tvLocationText.setText(locationText);

        // 5. Trạng thái & Dynamic Colors
        bindStatus(holder, item.getStatus());
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "";
        String formatted = date.format(dateFormatter);
        return formatted.substring(0, 1).toUpperCase(Locale.forLanguageTag("vi")) + formatted.substring(1);
    }

    private String formatTime(LocalTime time) {
        if (time == null) return "-- : --";
        return time.format(timeFormatter);
    }

    private void bindStatus(HistoryAttendanceViewHolder holder, AttendanceStatus status) {
        int colorAttr;
        int containerAttr;
        String text;

        switch (status) {
            case PRESENT:
                colorAttr = R.attr.colorAttendancePresent;
                containerAttr = R.attr.colorAttendancePresentContainer;
                text = "Đúng giờ";
                break;
            case LATE:
                colorAttr = R.attr.colorAttendanceLate;
                containerAttr = R.attr.colorAttendanceLateContainer;
                text = "Đi muộn";
                break;
            case EARLY_LEAVE:
                colorAttr = R.attr.colorAttendanceEarlyLeave;
                containerAttr = R.attr.colorAttendanceEarlyLeaveContainer;
                text = "Về sớm";
                break;
            case ABSENT:
                colorAttr = R.attr.colorAttendanceAbsent;
                containerAttr = R.attr.colorAttendanceAbsentContainer;
                text = "Vắng mặt";
                break;
            case LEAVE:
                colorAttr = R.attr.colorAttendanceLeave;
                containerAttr = R.attr.colorAttendanceLeaveContainer;
                text = "Nghỉ phép";
                break;
            case NOT_CHECKED:
                colorAttr = R.attr.colorAttendanceNotChecked;
                containerAttr = R.attr.colorAttendanceNotCheckedContainer;
                text = "Chưa chấm";
                break;
            case WEEKEND:
                colorAttr = R.attr.colorAttendanceWeekend;
                containerAttr = R.attr.colorAttendanceWeekendContainer;
                text = "Cuối tuần";
                break;
            default:
                colorAttr = R.attr.colorAttendanceNotChecked;
                containerAttr = R.attr.colorAttendanceNotCheckedContainer;
                text = "Chưa xác định";
                break;
        }

        // Lấy màu động từ Theme
        int color = MaterialColors.getColor(holder.itemView, colorAttr);
        int containerColor = MaterialColors.getColor(holder.itemView, containerAttr);

        // Chỉ đổi màu cho Chip trạng thái
        holder.binding.tagStatus.setText(text);
        holder.binding.tagStatus.setChipBackgroundColor(ColorStateList.valueOf(containerColor));
        holder.binding.tagStatus.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return attendanceList != null ? attendanceList.size() : 0;
    }

    static class HistoryAttendanceViewHolder extends RecyclerView.ViewHolder {
        private final ItemHistoryAttendanceBinding binding;

        public HistoryAttendanceViewHolder(ItemHistoryAttendanceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
package com.example.attendanceapp.ui.manager.attendance_history;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.ItemManagerAttendanceHistoryBinding;
import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.ManagerAttendanceHistoryItem;
import com.google.android.material.color.MaterialColors;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManagerAttendanceHistoryAdapter
        extends RecyclerView.Adapter<ManagerAttendanceHistoryAdapter.ViewHolder> {

    private final List<ManagerAttendanceHistoryItem> list = new ArrayList<>();

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale.forLanguageTag("vi"));

    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("HH:mm");

    public ManagerAttendanceHistoryAdapter() {
    }

    public void updateData(List<ManagerAttendanceHistoryItem> newItems) {
        this.list.clear();
        if (newItems != null) {
            this.list.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemManagerAttendanceHistoryBinding binding =
                ItemManagerAttendanceHistoryBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ManagerAttendanceHistoryItem item = list.get(position);

        // 1. Tên nhân viên
        holder.binding.tvEmployeeName.setText(item.getStaffName());

        // 2. Avatar
        Glide.with(holder.itemView.getContext())
                .load(item.getAvatar())
                .placeholder(R.drawable.ic_avatar)
                .error(R.drawable.ic_avatar)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.binding.ivEmployeeAvatar);

        // 3. Ngày tháng kèm ca làm việc
        String dateText = formatDate(item.getDate());
        if (item.getShiftName() != null && !item.getShiftName().trim().isEmpty()) {
            dateText += " · " + item.getShiftName().trim();
        }
        holder.binding.tvDateShift.setText(dateText);

        // 4. Giờ check-in & check-out
        holder.binding.tvCheckIn.setText(formatTime(item.getCheckIn()));
        holder.binding.tvCheckOut.setText(formatTime(item.getCheckOut()));

        // 5. Số giờ làm việc
        if (item.getCheckIn() != null && item.getCheckOut() != null) {
            holder.binding.cardTotalHours.setVisibility(View.VISIBLE);
            holder.binding.tvTotalHours.setText(item.getShortFormattedTotalTime());
        } else {
            holder.binding.cardTotalHours.setVisibility(View.GONE);
        }

        // 6. Chip trạng thái
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

    private void bindStatus(ViewHolder holder, AttendanceStatus status) {
        if (status == null) status = AttendanceStatus.NOT_CHECKED;

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
            case WEEKEND:
                colorAttr = R.attr.colorAttendanceWeekend;
                containerAttr = R.attr.colorAttendanceWeekendContainer;
                text = "Cuối tuần";
                break;
            case NOT_CHECKED:
            default:
                colorAttr = R.attr.colorAttendanceNotChecked;
                containerAttr = R.attr.colorAttendanceNotCheckedContainer;
                text = "Chưa chấm";
                break;
        }

        int color = MaterialColors.getColor(holder.itemView, colorAttr);
        int containerColor = MaterialColors.getColor(holder.itemView, containerAttr);

        holder.binding.tagStatus.setText(text);
        holder.binding.tagStatus.setChipBackgroundColor(ColorStateList.valueOf(containerColor));
        holder.binding.tagStatus.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemManagerAttendanceHistoryBinding binding;

        public ViewHolder(ItemManagerAttendanceHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

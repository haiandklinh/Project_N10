package com.example.attendanceapp.ui.employee.home.recent;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.core.utils.DateTimeFormatUtils;
import com.example.attendanceapp.databinding.ItemHomeRecentAttendanceBinding;
import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.RecentAttendance;

import java.util.ArrayList;
import java.util.List;

public class EmployeeRecentAttendanceAdapter extends RecyclerView.Adapter<EmployeeRecentAttendanceAdapter.RecentAttendanceViewHolder> {

    private final List<RecentAttendance> items = new ArrayList<>();

    public EmployeeRecentAttendanceAdapter() {
    }

    public void submitList(List<RecentAttendance> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecentAttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeRecentAttendanceBinding binding = ItemHomeRecentAttendanceBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new RecentAttendanceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentAttendanceViewHolder holder, int position) {
        RecentAttendance item = items.get(position);
        if (item == null) return;

        // 1. Ngày
        holder.binding.tvRecentDay.setText(DateTimeFormatUtils.formatDateDayMonth(item.getDate()));
        // 2. Check-in
        holder.binding.tvRecentCheckIn.setText(DateTimeFormatUtils.formatTime(item.getCheckIn()));
        // 3. Check-out
        holder.binding.tvRecentCheckOut.setText(DateTimeFormatUtils.formatTime(item.getCheckOut()));
        // 4. Bind Chip Trạng thái & Icon từ enum AttendanceStatus
        bindStatus(holder, item.getStatus());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void bindStatus(RecentAttendanceViewHolder holder, AttendanceStatus status) {
        if (status == null) {
            status = AttendanceStatus.NOT_CHECKED;
        }
        int iconRes = status.getIconRes();
        int textColor = ColorUtil.getThemeColor(holder.itemView, status.getColorAttr());
        int containerColor = ColorUtil.getThemeColor(holder.itemView, status.getContainerColorAttr());

        holder.binding.tagStatus.setText(status.getDisplayName());
        holder.binding.tagStatus.setTextColor(textColor);
        holder.binding.tagStatus.setChipBackgroundColor(ColorStateList.valueOf(containerColor));

        holder.binding.ivStatusIcon.setImageResource(iconRes);
        holder.binding.ivStatusIcon.setColorFilter(textColor);
        holder.binding.statusIconContainer.setCardBackgroundColor(containerColor);
    }

    static class RecentAttendanceViewHolder extends RecyclerView.ViewHolder {
        final ItemHomeRecentAttendanceBinding binding;
        public RecentAttendanceViewHolder(ItemHomeRecentAttendanceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

package com.example.attendanceapp.ui.employee.home.recent;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.databinding.ItemHomeRecentAttendanceBinding;
import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.RecentAttendance;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.core.utils.DateTimeFormatUtils;

public class RecentAttendanceAdapter extends ListAdapter<RecentAttendance, RecentAttendanceAdapter.RecentAttendanceViewHolder> {
    public RecentAttendanceAdapter() {
        super(new DiffUtil.ItemCallback<RecentAttendance>() {
            @Override
            public boolean areItemsTheSame(@NonNull RecentAttendance oldItem, @NonNull RecentAttendance newItem) {
                return oldItem.getDate().equals(newItem.getDate());
            }

            @Override
            public boolean areContentsTheSame(@NonNull RecentAttendance oldItem, @NonNull RecentAttendance newItem) {
                return oldItem.getCheckIn().equals(newItem.getCheckIn())
                        && oldItem.getCheckOut().equals(newItem.getCheckOut())
                        && oldItem.getStatus().equals(newItem.getStatus());
            }
        });
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

        RecentAttendance item = getItem(position);
        // 1. Ngày
        holder.binding.tvRecentDay.setText(DateTimeFormatUtils.formatDateDayMonth(item.getDate()));
        // 2. Check-in
        holder.binding.tvRecentCheckIn.setText(DateTimeFormatUtils.formatTime(item.getCheckIn()));
        // 3. Check-out
        holder.binding.tvRecentCheckOut.setText(DateTimeFormatUtils.formatTime(item.getCheckOut()));
        // 4. Bind Chip Trạng thái & Icon từ enum AttendanceStatus
        bindStatus(holder, item.getStatus());
    }


    private void bindStatus(RecentAttendanceViewHolder holder, AttendanceStatus status) {
        if (status == null) {
            status = AttendanceStatus.NOT_CHECKED;
        }
        // Lấy màu từ ColorUtil dựa trên thuộc tính trong enum AttendanceStatus
        int iconRes = status.getIconRes();
        int textColor = ColorUtil.getThemeColor(holder.itemView, status.getColorAttr());
        int containerColor = ColorUtil.getThemeColor(holder.itemView, status.getContainerColorAttr());

        // Bind text hiển thị từ getDisplayName() của enum
        holder.binding.tagStatus.setText(status.getDisplayName());
        holder.binding.tagStatus.setTextColor(textColor);
        holder.binding.tagStatus.setChipBackgroundColor(ColorStateList.valueOf(containerColor));

        // Bind Icon & Màu sắc cho Container
        holder.binding.ivStatusIcon.setImageResource(iconRes);
        holder.binding.ivStatusIcon.setColorFilter(textColor);
        holder.binding.statusIconContainer.setCardBackgroundColor(containerColor);
    }


    static class RecentAttendanceViewHolder extends RecyclerView.ViewHolder {
        private final ItemHomeRecentAttendanceBinding binding;
        public RecentAttendanceViewHolder(ItemHomeRecentAttendanceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
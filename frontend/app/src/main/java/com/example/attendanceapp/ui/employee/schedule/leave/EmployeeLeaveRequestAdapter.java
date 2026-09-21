package com.example.attendanceapp.ui.employee.schedule.leave;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.ItemLeaveRequestBinding; // Cần tạo layout item này
import com.example.attendanceapp.domain.model.LeaveRequest;
import com.example.attendanceapp.core.utils.ColorUtil;

import java.util.List;

public class EmployeeLeaveRequestAdapter extends RecyclerView.Adapter<EmployeeLeaveRequestAdapter.ViewHolder> {

    private final List<LeaveRequest> list;

    public EmployeeLeaveRequestAdapter(List<LeaveRequest> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLeaveRequestBinding binding = ItemLeaveRequestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaveRequest item = list.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.tvRequestType.setText(item.getType());
        holder.binding.tvTargetDate.setText(item.getTargetDate());
        holder.binding.tvReason.setText(item.getReason());

        // Xử lý Badge trạng thái (APPROVED / PENDING / REJECTED)
        switch (item.getStatus()) {
            case "APPROVED":
                holder.binding.chipStatus.setText("Đã duyệt");
                holder.binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(
                        ColorUtil.getThemeColor(context, R.attr.colorAttendancePresentContainer)));
                holder.binding.chipStatus.setTextColor(
                        ColorUtil.getThemeColor(context, R.attr.colorAttendancePresent));
                break;
            case "REJECTED":
                holder.binding.chipStatus.setText("Từ chối");
                holder.binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(
                        ColorUtil.getThemeColor(context, R.attr.colorAttendanceLateContainer)));
                holder.binding.chipStatus.setTextColor(
                        ColorUtil.getThemeColor(context, R.attr.colorAttendanceLate));
                break;
            default: // PENDING
                holder.binding.chipStatus.setText("Chờ duyệt");
                holder.binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(0xFFFFF8E1));
                holder.binding.chipStatus.setTextColor(0xFFF57F17);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemLeaveRequestBinding binding;

        public ViewHolder(ItemLeaveRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
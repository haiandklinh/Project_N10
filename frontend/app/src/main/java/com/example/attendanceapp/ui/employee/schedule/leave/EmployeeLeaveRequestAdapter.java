package com.example.attendanceapp.ui.employee.schedule.leave;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.databinding.ItemLeaveRequestBinding;
import com.example.attendanceapp.domain.model.ApprovalRequestType;
import com.example.attendanceapp.domain.model.ApprovalStatus;
import com.example.attendanceapp.domain.model.LeaveRequest;

import java.util.ArrayList;
import java.util.List;

public class EmployeeLeaveRequestAdapter extends RecyclerView.Adapter<EmployeeLeaveRequestAdapter.ViewHolder> {

    private final List<LeaveRequest> list;

    public EmployeeLeaveRequestAdapter(List<LeaveRequest> list) {
        this.list = list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    public void updateData(List<LeaveRequest> newList) {
        list.clear();
        if (newList != null) {
            list.addAll(newList);
        }
        notifyDataSetChanged();
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

        // 1. Gán loại yêu cầu (Tiêu đề và Icon nếu layout có ImageView)
        ApprovalRequestType type = item.getType();
        if (type != null) {
            holder.binding.tvRequestType.setText(type.getTitle());
        } else {
            holder.binding.tvRequestType.setText("");
        }

        // 2. Gán ngày áp dụng và lý do
        holder.binding.tvTargetDate.setText(item.getTargetDate() != null ? item.getTargetDate() : "");
        holder.binding.tvReason.setText(item.getReason() != null ? item.getReason() : "");

        // 3. Xử lý Chip trạng thái (Lấy trực tiếp nhãn và màu từ Enum ApprovalStatus)
        ApprovalStatus status = item.getStatus() != null ? item.getStatus() : ApprovalStatus.PENDING;
        holder.binding.chipStatus.setText(status.getLabel());
        holder.binding.chipStatus.setTextColor(ContextCompat.getColor(context, status.getTextColorRes()));
        holder.binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(
                ContextCompat.getColor(context, status.getContainerColorRes())
        ));
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
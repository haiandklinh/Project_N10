package com.example.attendanceapp.ui.manager.schedule;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.ItemManagerScheduleEmployeeBinding;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;

import java.util.ArrayList;
import java.util.List;

public class ManagerScheduleEmployeeAdapter extends RecyclerView.Adapter<ManagerScheduleEmployeeAdapter.ViewHolder> {

    public interface OnAssignClickListener {
        void onAssignClick(ManagerEmployeeItem item, int position);
    }

    private final List<ManagerEmployeeItem> list;
    private final OnAssignClickListener listener;

    public ManagerScheduleEmployeeAdapter(List<ManagerEmployeeItem> list, OnAssignClickListener listener) {
        this.list = list != null ? new ArrayList<>(list) : new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<ManagerEmployeeItem> newList) {
        list.clear();
        if (newList != null) list.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemManagerScheduleEmployeeBinding binding = ItemManagerScheduleEmployeeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ManagerEmployeeItem item = list.get(position);

        // 1. Gán Tên nhân viên
        holder.binding.tvName.setText(item.getFullName());

        // 2. Gán Mã NV
        String employeeCode = item.getEmployeeCode() != null ? item.getEmployeeCode() : "";
        holder.binding.tvCode.setText(employeeCode);

        // 3. Gán Chức vụ
        String positionName = item.getPosition() != null ? item.getPosition() : "";
        holder.binding.tvPosition.setText(positionName);

        if (employeeCode.isEmpty() || positionName.isEmpty()) {
            holder.binding.tvDivider.setVisibility(View.GONE);
        } else {
            holder.binding.tvDivider.setVisibility(View.VISIBLE);
        }

        // 4. LOAD AVATAR BẰNG GLIDE
        if (item.getAvatarUrl() != null && !item.getAvatarUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getAvatarUrl())
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_error)
                    .into(holder.binding.ivAvatar); // Đảm bảo ID này đúng với XML của bạn
        } else {
            holder.binding.ivAvatar.setImageResource(R.drawable.ic_avatar);
        }

        // 5. Thông tin Ca làm việc (Chip bên phải)
        boolean hasShift = item.getAssignedShiftName() != null && !item.getAssignedShiftName().isEmpty();

        if (hasShift) {
            holder.binding.chipShiftInfo.setText(item.getAssignedShiftName());

            int primaryColor = ColorUtil.getThemeColor(holder.itemView.getContext(), R.attr.colorPrimary);
            int containerColor = ColorUtil.getThemeColor(holder.itemView.getContext(), R.attr.colorAttendancePresentContainer);

            holder.binding.chipShiftInfo.setChipBackgroundColor(ColorStateList.valueOf(containerColor));
            holder.binding.chipShiftInfo.setTextColor(ColorStateList.valueOf(primaryColor));
        } else {
            holder.binding.chipShiftInfo.setText("Chưa xếp");

            int notCheckedColor = ColorUtil.getThemeColor(holder.itemView.getContext(), R.attr.colorAttendanceNotChecked);
            int notCheckedContainer = ColorUtil.getThemeColor(holder.itemView.getContext(), R.attr.colorAttendanceNotCheckedContainer);

            holder.binding.chipShiftInfo.setChipBackgroundColor(ColorStateList.valueOf(notCheckedContainer));
            holder.binding.chipShiftInfo.setTextColor(ColorStateList.valueOf(notCheckedColor));
        }

        holder.binding.cardRoot.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAssignClick(item, holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemManagerScheduleEmployeeBinding binding;

        public ViewHolder(ItemManagerScheduleEmployeeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
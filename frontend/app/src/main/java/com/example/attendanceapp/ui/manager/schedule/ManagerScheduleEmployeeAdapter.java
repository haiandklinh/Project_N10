package com.example.attendanceapp.ui.manager.schedule;

import android.content.Context;
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
        Context context = holder.itemView.getContext();

        // 1. Gán Tên nhân viên
        holder.binding.tvName.setText(item.getFullName());

        // 2. Gán Mã NV & Chức vụ
        String employeeCode = item.getEmployeeCode() != null ? item.getEmployeeCode() : "";
        String positionName = item.getPosition() != null ? item.getPosition() : "";

        holder.binding.tvCode.setText(employeeCode);
        holder.binding.tvPosition.setText(positionName);

        if (employeeCode.isEmpty() || positionName.isEmpty()) {
            holder.binding.tvDivider.setVisibility(View.GONE);
        } else {
            holder.binding.tvDivider.setVisibility(View.VISIBLE);
        }

        // 3. LOAD AVATAR ĐỒNG BỘ TỪ API
        com.example.attendanceapp.core.utils.AvatarLoader.loadAvatar(context, item.getAvatarUrl(), holder.binding.ivAvatar);

        // 4. Thông tin Ca làm việc (Chip bên phải)
        boolean hasShift = item.getAssignedShiftName() != null && !item.getAssignedShiftName().isEmpty();

        if (hasShift) {
            holder.binding.chipShiftInfo.setText(item.getAssignedShiftName());

            int primaryColor = ColorUtil.getThemeColor(context, R.attr.colorPrimary);
            int containerColor = ColorUtil.getThemeColor(context, R.attr.colorPrimaryContainer);

            holder.binding.chipShiftInfo.setChipBackgroundColor(ColorStateList.valueOf(containerColor));
            holder.binding.chipShiftInfo.setTextColor(ColorStateList.valueOf(primaryColor));
        } else {
            holder.binding.chipShiftInfo.setText("Chưa xếp");

            int outlineColor = ColorUtil.getThemeColor(context, com.google.android.material.R.attr.colorOutline);
            int surfaceVariantColor = ColorUtil.getThemeColor(context, com.google.android.material.R.attr.colorSurfaceVariant);

            holder.binding.chipShiftInfo.setChipBackgroundColor(ColorStateList.valueOf(surfaceVariantColor));
            holder.binding.chipShiftInfo.setTextColor(ColorStateList.valueOf(outlineColor));
        }

        // 5. Sự kiện Click an toàn
        holder.binding.cardRoot.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION && listener != null) {
                listener.onAssignClick(item, currentPos);
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
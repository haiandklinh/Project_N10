package com.example.attendanceapp.ui.manager.schedule;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.ItemManagerScheduleEmployeeBinding;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;

import java.util.List;

public class ManagerScheduleEmployeeAdapter extends RecyclerView.Adapter<ManagerScheduleEmployeeAdapter.ViewHolder> {

    public interface OnAssignClickListener {
        void onAssignClick(ManagerEmployeeItem item, int position);
    }

    private final List<ManagerEmployeeItem> list;
    private final OnAssignClickListener listener;

    public ManagerScheduleEmployeeAdapter(List<ManagerEmployeeItem> list, OnAssignClickListener listener) {
        this.list = new java.util.ArrayList<>(list);
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

        holder.binding.tvName.setText(item.getFullName());
        holder.binding.tvPosition.setText(item.getPosition() + " · " + item.getDepartment());

        boolean hasShift = item.getAssignedShiftName() != null;
        if (hasShift) {
            holder.binding.chipShiftStatus.setText(item.getAssignedShiftName() + " · " + item.getAssignedShiftTime());
            holder.binding.chipShiftStatus.setChipBackgroundColor(ColorStateList.valueOf(
                    ColorUtil.getThemeColor(holder.itemView.getContext(), R.attr.colorAttendancePresentContainer)));
            holder.binding.chipShiftStatus.setTextColor(
                    ColorUtil.getThemeColor(holder.itemView.getContext(), R.attr.colorAttendancePresent));
            holder.binding.btnAssignShift.setText("Đổi ca");
        } else {
            holder.binding.chipShiftStatus.setText("Chưa xếp ca");
            holder.binding.chipShiftStatus.setChipBackgroundColor(ColorStateList.valueOf(
                    ColorUtil.getThemeColor(holder.itemView.getContext(), R.attr.colorAttendanceNotCheckedContainer)));
            holder.binding.chipShiftStatus.setTextColor(
                    ColorUtil.getThemeColor(holder.itemView.getContext(), R.attr.colorAttendanceNotChecked));
            holder.binding.btnAssignShift.setText("Xếp ca");
        }

        holder.binding.btnAssignShift.setOnClickListener(v -> {
            if (listener != null) listener.onAssignClick(item, holder.getBindingAdapterPosition());
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

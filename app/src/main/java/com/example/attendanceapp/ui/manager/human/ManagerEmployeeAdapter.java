package com.example.attendanceapp.ui.manager.human;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.ItemManagerEmployeeBinding;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;

import java.util.List;

public class ManagerEmployeeAdapter extends RecyclerView.Adapter<ManagerEmployeeAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ManagerEmployeeItem item);
    }

    private final List<ManagerEmployeeItem> list;
    private final OnItemClickListener listener;

    public ManagerEmployeeAdapter(List<ManagerEmployeeItem> list, OnItemClickListener listener) {
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
        ItemManagerEmployeeBinding binding = ItemManagerEmployeeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ManagerEmployeeItem item = list.get(position);

        holder.binding.tvName.setText(item.getFullName());
        holder.binding.tvPosition.setText(item.getPosition() + " · " + item.getDepartment());
        holder.binding.tvPhone.setText(item.getPhoneNumber());

        boolean isActive = ManagerEmployeeItem.STATUS_ACTIVE.equals(item.getStatus());
        holder.binding.chipStatus.setText(isActive ? "Đang làm" : "Đang nghỉ phép");
        int bgAttr = isActive ? R.attr.colorAttendancePresentContainer : R.attr.colorAttendanceLeaveContainer;
        int textAttr = isActive ? R.attr.colorAttendancePresent : R.attr.colorAttendanceLeave;
        holder.binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(
                ColorUtil.getThemeColor(holder.itemView.getContext(), bgAttr)));
        holder.binding.chipStatus.setTextColor(ColorUtil.getThemeColor(holder.itemView.getContext(), textAttr));

        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemManagerEmployeeBinding binding;

        public ViewHolder(ItemManagerEmployeeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

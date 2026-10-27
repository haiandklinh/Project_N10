package com.example.attendanceapp.ui.manager.schedule.assign;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.ItemAssignedEmployeeChipBinding;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;

import java.util.ArrayList;
import java.util.List;

public class AssignedEmployeeAdapter extends RecyclerView.Adapter<AssignedEmployeeAdapter.ViewHolder> {

    public interface OnRemoveClickListener {
        void onRemoveClick(ManagerEmployeeItem employee, int position);
    }

    private final List<ManagerEmployeeItem> list;
    private final OnRemoveClickListener listener;

    public AssignedEmployeeAdapter(List<ManagerEmployeeItem> list, OnRemoveClickListener listener) {
        this.list = list != null ? new ArrayList<>(list) : new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<ManagerEmployeeItem> newList) {
        list.clear();
        if (newList != null) {
            list.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAssignedEmployeeChipBinding binding = ItemAssignedEmployeeChipBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ManagerEmployeeItem item = list.get(position);

        holder.binding.tvName.setText(item.getFullName() != null ? item.getFullName() : "");

        // Load Avatar đồng bộ từ API
        com.example.attendanceapp.core.utils.AvatarLoader.loadAvatar(holder.itemView.getContext(), item.getAvatarUrl(), holder.binding.ivAvatar);

        holder.binding.btnRemove.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (listener != null && currentPosition != RecyclerView.NO_POSITION) {
                listener.onRemoveClick(item, currentPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemAssignedEmployeeChipBinding binding;

        public ViewHolder(ItemAssignedEmployeeChipBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
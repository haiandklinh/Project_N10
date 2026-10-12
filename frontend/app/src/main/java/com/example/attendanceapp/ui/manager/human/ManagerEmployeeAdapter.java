package com.example.attendanceapp.ui.manager.human;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.ItemManagerEmployeeBinding;
import com.example.attendanceapp.domain.model.EmployeeStatus;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;

import java.util.ArrayList;
import java.util.List;

public class ManagerEmployeeAdapter extends RecyclerView.Adapter<ManagerEmployeeAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ManagerEmployeeItem item);
    }

    private final List<ManagerEmployeeItem> list;
    private final OnItemClickListener listener;

    public ManagerEmployeeAdapter(List<ManagerEmployeeItem> list, OnItemClickListener listener) {
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
        ItemManagerEmployeeBinding binding = ItemManagerEmployeeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ManagerEmployeeItem item = list.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.tvName.setText(item.getFullName());
        holder.binding.tvEmployeeCode.setText(item.getEmployeeCode());
        holder.binding.tvPosition.setText(item.getPosition());
        holder.binding.tvDepartment.setText(item.getDepartment());

        // Binding thông tin trạng thái trực tiếp từ Enum EmployeeStatus
        EmployeeStatus status = item.getStatus() != null ? item.getStatus() : EmployeeStatus.ACTIVE;
        holder.binding.chipStatus.setText(status.getLabel());
        holder.binding.chipStatus.setTextColor(ContextCompat.getColor(context, status.getTextColorRes()));
        holder.binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(
                ContextCompat.getColor(context, status.getContainerColorRes())
        ));

        // Thêm Glide hoặc Coil/Picasso để load Avatar trong onBindViewHolder
        if (item.getAvatarUrl() != null && !item.getAvatarUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(context)
                    .load(item.getAvatarUrl())
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .into(holder.binding.ivAvatar);
        } else {
            holder.binding.ivAvatar.setImageResource(R.drawable.ic_avatar);
        }

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
package com.example.attendanceapp.ui.manager.schedule.select_employee;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.databinding.ItemSelectEmployeeBinding;
import com.example.attendanceapp.domain.model.EmployeeStatus;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SelectEmployeeAdapter extends RecyclerView.Adapter<SelectEmployeeAdapter.ViewHolder> {

    public interface OnEmployeeCheckListener {
        void onCheckChanged(ManagerEmployeeItem employee, boolean isChecked);
    }

    private final List<ManagerEmployeeItem> list;
    private final Set<String> selectedEmployeeIds;
    private final OnEmployeeCheckListener listener;

    public SelectEmployeeAdapter(List<ManagerEmployeeItem> list, Set<String> selectedEmployeeIds, OnEmployeeCheckListener listener) {
        this.list = list != null ? new ArrayList<>(list) : new ArrayList<>();
        this.selectedEmployeeIds = selectedEmployeeIds;
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
        ItemSelectEmployeeBinding binding = ItemSelectEmployeeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ManagerEmployeeItem item = list.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.tvName.setText(item.getFullName());

        String codePosition = (item.getEmployeeCode() != null ? item.getEmployeeCode() : "")
                + " · " + (item.getPosition() != null ? item.getPosition() : "");
        holder.binding.tvCodePosition.setText(codePosition);

        // Checkbox state
        boolean isChecked = selectedEmployeeIds.contains(item.getUserId());
        holder.binding.cbSelect.setOnCheckedChangeListener(null);
        holder.binding.cbSelect.setChecked(isChecked);

        // Badge Status - Sử dụng trực tiếp Enum EmployeeStatus
        EmployeeStatus status = item.getStatus() != null ? item.getStatus() : EmployeeStatus.ACTIVE;
        holder.binding.chipStatus.setText(status.getLabel());
        holder.binding.chipStatus.setTextColor(ContextCompat.getColor(context, status.getTextColorRes()));
        holder.binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(
                ContextCompat.getColor(context, status.getContainerColorRes())
        ));

        // Event click item hoặc checkbox
        holder.binding.cbSelect.setOnCheckedChangeListener((buttonView, checked) -> {
            if (listener != null) listener.onCheckChanged(item, checked);
        });

        holder.binding.cardRoot.setOnClickListener(v -> {
            boolean nextState = !holder.binding.cbSelect.isChecked();
            holder.binding.cbSelect.setChecked(nextState);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSelectEmployeeBinding binding;

        public ViewHolder(ItemSelectEmployeeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
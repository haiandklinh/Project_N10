package com.example.attendanceapp.ui.manager.schedule.assign;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.databinding.ItemAssignedEmployeeChipBinding;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;

import java.util.List;

public class AssignedEmployeeSuccessAdapter extends RecyclerView.Adapter<AssignedEmployeeSuccessAdapter.ViewHolder> {

    private final List<ManagerEmployeeItem> list;

    public AssignedEmployeeSuccessAdapter(List<ManagerEmployeeItem> list) {
        this.list = list;
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

        holder.binding.tvName.setText(item.getFullName());

        // ẨN NÚT XÓA TRÊN MÀN HÌNH THÀNH CÔNG
        holder.binding.btnRemove.setVisibility(View.GONE);
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
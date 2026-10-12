package com.example.attendanceapp.ui.manager.human;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.ItemDepartmentBinding;

import java.util.ArrayList;
import java.util.List;

public class ManagerDepartmentAdapter extends RecyclerView.Adapter<ManagerDepartmentAdapter.ViewHolder> {

    public interface OnActionListener {
        void onEdit(String departmentId);
        void onDelete(String departmentId);
    }

    private final List<String> list; // Using String for mock, change to Department model later
    private final OnActionListener listener;

    public ManagerDepartmentAdapter(List<String> list, OnActionListener listener) {
        this.list = list != null ? new ArrayList<>(list) : new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<String> newList) {
        list.clear();
        if (newList != null) list.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDepartmentBinding binding = ItemDepartmentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String departmentName = list.get(position);
        android.content.Context context = holder.itemView.getContext();
        holder.binding.tvName.setText(departmentName);
        holder.binding.tvDescription.setText(String.format(context.getString(R.string.human_total_count), 0).replace("nhân viên", "nhân sự"));
        
        holder.binding.btnMenu.setOnClickListener(v -> {
            // Show popup menu for edit/delete
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemDepartmentBinding binding;
        public ViewHolder(ItemDepartmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

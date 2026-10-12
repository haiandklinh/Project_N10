package com.example.attendanceapp.ui.manager.human;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.databinding.ItemPositionBinding;

import java.util.ArrayList;
import java.util.List;

public class ManagerPositionAdapter extends RecyclerView.Adapter<ManagerPositionAdapter.ViewHolder> {

    public interface OnActionListener {
        void onEdit(String positionId);
        void onDelete(String positionId);
    }

    private final List<String> list; // Using String for mock
    private final OnActionListener listener;

    public ManagerPositionAdapter(List<String> list, OnActionListener listener) {
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
        ItemPositionBinding binding = ItemPositionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String positionName = list.get(position);
        android.content.Context context = holder.itemView.getContext();
        holder.binding.tvName.setText(positionName);
        holder.binding.tvCount.setText(String.format("%d nhân viên đang giữ chức vụ này", 0));
        
        holder.binding.btnMenu.setOnClickListener(v -> {
            // Show popup menu
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemPositionBinding binding;
        public ViewHolder(ItemPositionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

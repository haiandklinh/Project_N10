package com.example.attendanceapp.ui.manager.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.databinding.ItemHomeCategoryBinding;

import java.util.List;

public class ManagerQuickActionAdapter extends RecyclerView.Adapter<ManagerQuickActionAdapter.ViewHolder> {

    private final List<ManagerQuickAction> list;

    public ManagerQuickActionAdapter(List<ManagerQuickAction> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeCategoryBinding binding = ItemHomeCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ManagerQuickAction item = list.get(position);
        holder.binding.tvCategoryName.setText(item.getTitle());
        holder.binding.ivCategoryIcon.setImageResource(item.getIconResId());
        holder.binding.getRoot().setOnClickListener(v -> item.performClick());
        holder.binding.cardIcon.setOnClickListener(v -> item.performClick());
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemHomeCategoryBinding binding;

        public ViewHolder(ItemHomeCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

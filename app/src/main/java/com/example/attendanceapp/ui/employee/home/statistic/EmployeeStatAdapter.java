package com.example.attendanceapp.ui.employee.home.statistic;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.databinding.ItemStatCardBinding;

import java.util.ArrayList;
import java.util.List;

public class EmployeeStatAdapter extends RecyclerView.Adapter<EmployeeStatAdapter.StatViewHolder> {

    private final List<EmployeeStatItem> items = new ArrayList<>();

    public void setItems(List<EmployeeStatItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStatCardBinding binding = ItemStatCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new StatViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StatViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class StatViewHolder extends RecyclerView.ViewHolder {
        private final ItemStatCardBinding binding;

        public StatViewHolder(ItemStatCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(EmployeeStatItem item) {
            binding.cardRoot.setCardBackgroundColor(item.getBgColor());
            binding.ivStatIcon.setImageResource(item.getIconRes());
            binding.tvStatValue.setText(item.getValue());
            binding.tvStatValue.setTextColor(item.getTextColor());
            binding.tvStatUnit.setText(item.getUnit());
            binding.tvStatUnit.setTextColor(item.getTextColor());
            binding.tvStatLabel.setText(item.getLabel());
        }
    }
}
package com.example.attendanceapp.ui.manager.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.attendanceapp.databinding.ItemShiftTodayBinding;
import java.util.List;

public class TodayShiftAdapter extends RecyclerView.Adapter<TodayShiftAdapter.ViewHolder> {

    private final List<TodayShiftSummary> shifts;

    public TodayShiftAdapter(List<TodayShiftSummary> shifts) {
        this.shifts = shifts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemShiftTodayBinding binding = ItemShiftTodayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(shifts.get(position));
    }

    @Override
    public int getItemCount() {
        return shifts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemShiftTodayBinding binding;

        ViewHolder(ItemShiftTodayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TodayShiftSummary shift) {
            binding.tvShiftName.setText(shift.getShiftName() + " · " + shift.getTimeRange());
            binding.tvShiftLocation.setText(shift.getLocation() + " · " + shift.getEmployeeCount() + " NV");
            binding.tagShiftStatus.setText(shift.getStatus());
        }
    }
}
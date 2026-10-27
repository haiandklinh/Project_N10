package com.example.attendanceapp.ui.manager.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.databinding.ItemShiftTodayBinding;

import java.util.ArrayList;
import java.util.List;

public class TodayShiftAdapter extends RecyclerView.Adapter<TodayShiftAdapter.ViewHolder> {

    public interface OnShiftClickListener {
        void onShiftClick(TodayShiftSummary shift);
    }

    private final List<TodayShiftSummary> shifts = new ArrayList<>();
    private final OnShiftClickListener clickListener;

    public TodayShiftAdapter(List<TodayShiftSummary> shifts) {
        this(shifts, null);
    }

    public TodayShiftAdapter(List<TodayShiftSummary> shifts, OnShiftClickListener clickListener) {
        if (shifts != null) {
            this.shifts.addAll(shifts);
        }
        this.clickListener = clickListener;
    }

    public void updateData(List<TodayShiftSummary> newShifts) {
        this.shifts.clear();
        if (newShifts != null) {
            this.shifts.addAll(newShifts);
        }
        notifyDataSetChanged();
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
        holder.bind(shifts.get(position), clickListener);
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

        void bind(TodayShiftSummary shift, OnShiftClickListener listener) {
            if (shift == null) return;

            String name = shift.getShiftName() != null ? shift.getShiftName() : "Ca làm việc";
            String timeRange = shift.getTimeRange();
            if (timeRange != null && !timeRange.trim().isEmpty()) {
                binding.tvShiftName.setText(name + " · " + timeRange);
            } else {
                binding.tvShiftName.setText(name);
            }

            String location = shift.getLocation();
            if (location != null && !location.trim().isEmpty()) {
                binding.tvShiftLocation.setText(location);
            } else {
                binding.tvShiftLocation.setText("Văn phòng chính");
            }

            // Chip hiển thị số nhân viên làm ca này
            binding.tagShiftEmployeeCount.setText(shift.getEmployeeCount() + " NV");

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onShiftClick(shift);
                }
            });
        }
    }
}
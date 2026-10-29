package com.example.attendanceapp.ui.employee.home.shift;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.ItemShiftTodayBinding;
import com.example.attendanceapp.domain.model.Shift;
import com.example.attendanceapp.domain.model.ShiftStatus;

import java.util.Objects;

public class EmployeeTodayShiftAdapter extends ListAdapter<Shift, EmployeeTodayShiftAdapter.ShiftViewHolder> {

    public interface OnShiftClickListener {
        void onShiftClick(Shift shift);
    }

    private final OnShiftClickListener onShiftClickListener;

    public EmployeeTodayShiftAdapter() {
        this(null);
    }

    public EmployeeTodayShiftAdapter(OnShiftClickListener onShiftClickListener) {
        super(DIFF_CALLBACK);
        this.onShiftClickListener = onShiftClickListener;
    }

    private static final DiffUtil.ItemCallback<Shift> DIFF_CALLBACK = new DiffUtil.ItemCallback<Shift>() {
        @Override
        public boolean areItemsTheSame(@NonNull Shift oldItem, @NonNull Shift newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Shift oldItem, @NonNull Shift newItem) {
            return Objects.equals(oldItem.getShiftName(), newItem.getShiftName())
                    && Objects.equals(oldItem.getStartTime(), newItem.getStartTime())
                    && Objects.equals(oldItem.getEndTime(), newItem.getEndTime())
                    && Objects.equals(oldItem.getStatus(), newItem.getStatus())
                    && Objects.equals(oldItem.getLocation(), newItem.getLocation());
        }
    };

    @NonNull
    @Override
    public ShiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemShiftTodayBinding binding = ItemShiftTodayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ShiftViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ShiftViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public class ShiftViewHolder extends RecyclerView.ViewHolder {
        private final ItemShiftTodayBinding binding;

        public ShiftViewHolder(@NonNull ItemShiftTodayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Shift shift) {
            if (shift == null) return;
            Context context = itemView.getContext();

            binding.tvShiftName.setText(shift.getShiftName());
            binding.tvShiftTime.setText(shift.getShiftTime());

            String location = shift.getLocation();
            if (location == null || location.trim().isEmpty()) {
                location = "Vị trí ca làm việc";
            }
            binding.tvShiftLocation.setText(location);

            ShiftStatus currentStatus = (shift.getStatus() != null) ? shift.getStatus() : ShiftStatus.PENDING;
            binding.tagShiftStatus.setText(currentStatus.getDisplayName());
            int bgColor = ColorUtil.getThemeColor(context, currentStatus.getBgAttrRes());
            int textColor = ColorUtil.getThemeColor(context, currentStatus.getTextColorAttrRes());
            binding.tagShiftStatus.setChipBackgroundColor(ColorStateList.valueOf(bgColor));
            binding.tagShiftStatus.setTextColor(textColor);

            binding.getRoot().setOnClickListener(v -> {
                if (onShiftClickListener != null) {
                    onShiftClickListener.onShiftClick(shift);
                }
            });
        }
    }
}

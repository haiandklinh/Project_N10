package com.example.attendanceapp.ui.employee.schedule.shift;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.ItemShiftUpcomingBinding;
import com.example.attendanceapp.domain.model.Shift;
import com.google.android.material.color.MaterialColors;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public class EmployeeUpcomingShiftAdapter
        extends ListAdapter<Shift, EmployeeUpcomingShiftAdapter.UpcomingShiftViewHolder> {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, dd/MM", new Locale("vi", "VN"));

    private OnItemClickListener onItemClickListener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public EmployeeUpcomingShiftAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Shift> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Shift>() {
                @Override
                public boolean areItemsTheSame(@NonNull Shift oldItem, @NonNull Shift newItem) {
                    return Objects.equals(oldItem.getId(), newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Shift oldItem, @NonNull Shift newItem) {
                    return Objects.equals(oldItem, newItem);
                }
            };

    @NonNull
    @Override
    public UpcomingShiftViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemShiftUpcomingBinding binding =
                ItemShiftUpcomingBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );

        return new UpcomingShiftViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UpcomingShiftViewHolder holder, int position) {
        Shift shift = getItem(position);
        boolean isSelected = (position == selectedPosition);

        ItemShiftUpcomingBinding binding = holder.binding;

        // 1. Cập nhật màu nền (Background Color) & Icon Color
        int backgroundColorAttr = isSelected ? R.attr.colorPrimaryContainer : R.attr.colorSurfaceVariant;
//        int bgColor = MaterialColors.getColor(binding.cardShiftIcon,
//                isSelected ? R.attr.colorSurfaceVariant : R.attr.colorPrimaryContainer);

        binding.getRoot().setCardBackgroundColor(
                MaterialColors.getColor(binding.getRoot(), backgroundColorAttr)
        );
//        binding.cardShiftIcon.setCardBackgroundColor(bgColor);

        // 2. Cập nhật độ rộng viền & màu viền (Stroke)
        binding.getRoot().setStrokeWidth(isSelected ? 4 : 0);
        binding.getRoot().setStrokeColor(
                MaterialColors.getColor(binding.getRoot(), R.attr.colorPrimary)
        );

        // 3. Bind dữ liệu Text
        binding.tvShiftName.setText(shift.getShiftName());

        LocalDate date = shift.getDate();
        if (date != null) {
            binding.tvShiftDate.setText(date.format(DATE_FORMATTER));
        } else {
            binding.tvShiftDate.setText("");
        }

        String loc = shift.getLocation();
        if (loc == null || loc.trim().isEmpty()) {
            loc = "Vị trí ca làm việc";
        }
        binding.tvShiftLocation.setText(loc);
        binding.tvShiftLocation.setVisibility(View.VISIBLE);

        binding.tvShiftTime.setText(shift.getShiftTime());
    }

    public void setSelectedPosition(int position) {
        if (position < 0 || position >= getItemCount()) return;

        int previous = selectedPosition;
        selectedPosition = position;

        if (previous != RecyclerView.NO_POSITION) {
            notifyItemChanged(previous);
        }
        notifyItemChanged(selectedPosition);
    }

    public void clearSelection() {
        int previous = selectedPosition;
        selectedPosition = RecyclerView.NO_POSITION;
        if (previous != RecyclerView.NO_POSITION) {
            notifyItemChanged(previous);
        }
    }

    public void setSelectedShiftId(@Nullable String shiftId) {
        if (shiftId == null) {
            clearSelection();
            return;
        }

        for (int i = 0; i < getItemCount(); i++) {
            Shift shift = getItem(i);
            if (shift != null && Objects.equals(shift.getId(), shiftId)) {
                setSelectedPosition(i);
                return;
            }
        }
    }

    @Nullable
    public Shift getSelectedShift() {
        if (selectedPosition != RecyclerView.NO_POSITION && selectedPosition < getItemCount()) {
            return getItem(selectedPosition);
        }
        return null;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public interface OnItemClickListener {
        void onItemClick(Shift shift, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    class UpcomingShiftViewHolder extends RecyclerView.ViewHolder {

        final ItemShiftUpcomingBinding binding;

        UpcomingShiftViewHolder(@NonNull ItemShiftUpcomingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Shift shift = getItem(position);
                    setSelectedPosition(position);

                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(shift, position);
                    }
                }
            });
        }
    }
}
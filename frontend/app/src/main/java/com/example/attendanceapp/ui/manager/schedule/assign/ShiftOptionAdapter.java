package com.example.attendanceapp.ui.manager.schedule.assign;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.ItemSelectShiftOptionBinding;
import com.example.attendanceapp.domain.model.ShiftOption;

import java.util.ArrayList;
import java.util.List;

public class ShiftOptionAdapter extends RecyclerView.Adapter<ShiftOptionAdapter.ViewHolder> {

    @FunctionalInterface
    public interface OnShiftSelectListener {
        void onShiftSelected(ShiftOption shift);
    }

    private final List<ShiftOption> list;
    private ShiftOption selectedShift;
    private final OnShiftSelectListener listener;

    public ShiftOptionAdapter(List<ShiftOption> list,
                              ShiftOption initialSelected,
                              OnShiftSelectListener listener) {
        this.list = list != null ? new ArrayList<>(list) : new ArrayList<>();
        this.selectedShift = initialSelected;
        this.listener = listener;
    }

    public void updateData(List<ShiftOption> newList, ShiftOption newSelected) {
        this.list.clear();
        if (newList != null) {
            this.list.addAll(newList);
        }
        this.selectedShift = newSelected;
        notifyDataSetChanged();
    }

    public void setSelectedShift(ShiftOption shift) {
        if (selectedShift != null && selectedShift.equals(shift)) return;

        int prevIndex = list.indexOf(selectedShift);
        this.selectedShift = shift;
        int newIndex = list.indexOf(selectedShift);

        if (prevIndex != -1) notifyItemChanged(prevIndex);
        if (newIndex != -1) notifyItemChanged(newIndex);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSelectShiftOptionBinding binding = ItemSelectShiftOptionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShiftOption item = list.get(position);
        Context context = holder.itemView.getContext();
        boolean isSelected = selectedShift != null && selectedShift.getId().equals(item.getId());

        holder.binding.tvShiftTitle.setText(item.getTitle());
        holder.binding.tvShiftTime.setText(item.getTimeRange());

        // Đặt Icon mặc định cho ca làm
        holder.binding.ivShiftIcon.setImageResource(R.drawable.ic_history);

        // Chống RadioButton nuốt event click
        holder.binding.radioSelected.setClickable(false);
        holder.binding.radioSelected.setFocusable(false);
        holder.binding.radioSelected.setChecked(isSelected);

        // Highlight stroke color, background & icon tint
        float density = context.getResources().getDisplayMetrics().density;
        int primaryColor = ColorUtil.getThemeColor(context, R.attr.colorPrimary);
        int bgSelected = ColorUtil.getThemeColor(context, R.attr.colorPrimaryContainer);
        int bgDefault = ColorUtil.getThemeColor(context, R.attr.colorSurfaceVariant);
        int defaultIconColor = ColorUtil.getThemeColor(context, R.attr.colorOnSurfaceVariant);

        if (isSelected) {
            // Dùng setCardBackgroundColor thay vì setBackgroundColor
            holder.binding.cardShiftOption.setCardBackgroundColor(bgSelected);
            holder.binding.cardShiftOption.setStrokeColor(primaryColor);
            holder.binding.cardShiftOption.setStrokeWidth((int) (2.0f * density));
            holder.binding.ivShiftIcon.setImageTintList(ColorStateList.valueOf(primaryColor));
        } else {
            holder.binding.cardShiftOption.setCardBackgroundColor(bgDefault);
            int strokeColor = ContextCompat.getColor(context, android.R.color.transparent);
            holder.binding.cardShiftOption.setStrokeColor(strokeColor);
            holder.binding.cardShiftOption.setStrokeWidth((int) (1.0f * density));
            holder.binding.ivShiftIcon.setImageTintList(ColorStateList.valueOf(defaultIconColor));
        }

        // Click chọn ca làm việc
        holder.binding.cardShiftOption.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            setSelectedShift(item);

            if (listener != null) {
                listener.onShiftSelected(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSelectShiftOptionBinding binding;

        public ViewHolder(ItemSelectShiftOptionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
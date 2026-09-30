package com.example.attendanceapp.ui.manager.schedule.assign;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.ItemSelectShiftOptionBinding;
import com.example.attendanceapp.domain.model.ShiftOption;

import java.util.ArrayList;
import java.util.List;

public class ShiftOptionAdapter extends RecyclerView.Adapter<ShiftOptionAdapter.ViewHolder> {

    public interface OnShiftActionListener {
        void onShiftSelected(ShiftOption shift);
        void onShiftEdit(ShiftOption shift, int position);
        void onShiftDelete(ShiftOption shift, int position);
    }

    private final List<ShiftOption> list;
    private ShiftOption selectedShift;
    private final OnShiftActionListener listener;

    public ShiftOptionAdapter(List<ShiftOption> list,
                              ShiftOption initialSelected,
                              OnShiftActionListener listener) {
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
        // Sử dụng getTimeRange() động bóc tách từ startTime và endTime
        holder.binding.tvShiftTime.setText(item.getTimeRange());

        // Đặt Icon mặc định cho ca làm
        holder.binding.ivShiftIcon.setImageResource(R.drawable.ic_history);

        // Radio State
        holder.binding.radioSelected.setChecked(isSelected);

        // Highlight stroke color & icon tint
        float density = context.getResources().getDisplayMetrics().density;
        if (isSelected) {
            int primaryColor = ColorUtil.getThemeColor(context, R.attr.colorPrimary);
            holder.binding.cardShiftOption.setStrokeColor(primaryColor);
            holder.binding.cardShiftOption.setStrokeWidth((int) (1.5f * density));
            holder.binding.ivShiftIcon.setImageTintList(ColorStateList.valueOf(primaryColor));
        } else {
            int strokeColor = ContextCompat.getColor(context, android.R.color.transparent);
            holder.binding.cardShiftOption.setStrokeColor(strokeColor);
            holder.binding.cardShiftOption.setStrokeWidth((int) (1.0f * density));
            holder.binding.ivShiftIcon.setImageTintList(null);
        }

        // 1. Click thường: Chọn ca
        holder.binding.cardShiftOption.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            int prevIndex = list.indexOf(selectedShift);
            selectedShift = item;

            if (prevIndex != -1) notifyItemChanged(prevIndex);
            notifyItemChanged(currentPos);

            if (listener != null) {
                listener.onShiftSelected(item);
            }
        });

        // 2. Nhấn giữ (Long Click): Hiển thị PopupMenu Chỉnh sửa / Xóa
        holder.binding.cardShiftOption.setOnLongClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                showPopupMenu(v, item, currentPos);
            }
            return true;
        });
    }

    private void showPopupMenu(View view, ShiftOption item, int position) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);

        popup.getMenu().add(0, 1, 0, "Chỉnh sửa ca");
        popup.getMenu().add(0, 2, 1, "Xóa ca");

        popup.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == 1) {
                if (listener != null) listener.onShiftEdit(item, position);
                return true;
            } else if (menuItem.getItemId() == 2) {
                if (listener != null) listener.onShiftDelete(item, position);
                return true;
            }
            return false;
        });

        popup.show();
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
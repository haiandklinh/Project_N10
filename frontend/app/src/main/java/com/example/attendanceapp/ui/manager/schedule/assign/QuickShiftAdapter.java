package com.example.attendanceapp.ui.manager.schedule.assign;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.LayoutShiftOptionRowBinding;
import com.example.attendanceapp.domain.model.ShiftOption;

import java.util.ArrayList;
import java.util.List;

public class QuickShiftAdapter extends RecyclerView.Adapter<QuickShiftAdapter.ViewHolder> {

    public interface OnShiftSelectedListener {
        /**
         * @param selectedShift Trả về null nếu chọn "Không xếp ca"
         */
        void onShiftSelected(@Nullable ShiftOption selectedShift);
    }

    private final List<ShiftOption> shifts;
    private final OnShiftSelectedListener listener;

    // ShiftOption đại diện cho mục "Không xếp ca" (id = null)
    private static final ShiftOption SHIFT_NONE = new ShiftOption(null, "Không xếp ca", "", "");

    private ShiftOption selectedItem = null; // Mặc định chưa chọn

    public QuickShiftAdapter(List<ShiftOption> availableShifts, OnShiftSelectedListener listener) {
        this.shifts = new ArrayList<>();
        if (availableShifts != null) {
            this.shifts.addAll(availableShifts);
        }
        // Luôn chèn thêm mục "Không xếp ca" vào cuối danh sách
        this.shifts.add(SHIFT_NONE);

        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutShiftOptionRowBinding binding = LayoutShiftOptionRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShiftOption item = shifts.get(position);
        Context context = holder.itemView.getContext();

        boolean isNoneOption = item.getId() == null;
        boolean isSelected = (selectedItem != null && item.getId() != null && item.getId().equals(selectedItem.getId()))
                || (selectedItem == SHIFT_NONE && isNoneOption);

        // 1. Hiển thị thông tin Tên ca & Giờ làm
        if (isNoneOption) {
            holder.binding.tvShiftName.setText(item.getTitle());
            holder.binding.tvShiftTime.setText("Nhân viên nghỉ ngày này");
            holder.binding.ivIcon.setImageResource(R.drawable.ic_close);
        } else {
            holder.binding.tvShiftName.setText(item.getTitle());
            holder.binding.tvShiftTime.setText(item.getTimeRange());
            holder.binding.ivIcon.setImageResource(R.drawable.ic_history);
        }

        // 2. Hiển thị trạng thái Tick chọn (Check icon)
        holder.binding.ivCheck.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);

        // 3. Highlight viền và màu sắc của Card khi được chọn
        int primaryColor = ColorUtil.getThemeColor(context, R.attr.colorPrimary);
        float density = context.getResources().getDisplayMetrics().density;

        if (isSelected) {
            holder.binding.cardIcon.setStrokeColor(primaryColor);
            holder.binding.cardIcon.setStrokeWidth((int) (1.5f * density));
            holder.binding.ivIcon.setImageTintList(ColorStateList.valueOf(primaryColor));
        } else {
            int transparentColor = ContextCompat.getColor(context, android.R.color.transparent);
            holder.binding.cardIcon.setStrokeColor(transparentColor);
            holder.binding.cardIcon.setStrokeWidth((int) (1.0f * density));
            holder.binding.ivIcon.setImageTintList(null);
        }

        // 4. Xử lý sự kiện Click chọn ca
        holder.binding.cardIcon.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            int prevPos = shifts.indexOf(selectedItem);
            selectedItem = item;

            if (prevPos != -1) notifyItemChanged(prevPos);
            notifyItemChanged(currentPos);

            if (listener != null) {
                // Nếu chọn SHIFT_NONE thì truyền null sang listener
                listener.onShiftSelected(isNoneOption ? null : item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return shifts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final LayoutShiftOptionRowBinding binding;

        public ViewHolder(LayoutShiftOptionRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
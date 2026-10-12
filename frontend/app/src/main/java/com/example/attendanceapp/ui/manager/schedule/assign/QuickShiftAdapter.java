package com.example.attendanceapp.ui.manager.schedule.assign;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.ItemSelectShiftOptionBinding;
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
    private static final ShiftOption SHIFT_NONE = new ShiftOption(null, "Không xếp ca", "", "", null, null);

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

    public void setSelectedShift(@Nullable ShiftOption shift) {
        ShiftOption newSelected = (shift == null) ? SHIFT_NONE : shift;
        if (selectedItem != null && isSameShift(selectedItem, newSelected)) return;

        int prevPos = getShiftPosition(selectedItem);
        this.selectedItem = newSelected;
        int newPos = getShiftPosition(selectedItem);

        if (prevPos != -1) notifyItemChanged(prevPos);
        if (newPos != -1) notifyItemChanged(newPos);
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
        ShiftOption item = shifts.get(position);
        Context context = holder.itemView.getContext();

        boolean isNoneOption = item.getId() == null;
        boolean isSelected = isSameShift(selectedItem, item);

        // 1. Hiển thị thông tin Tên ca & Giờ làm
        if (isNoneOption) {
            holder.binding.tvShiftTitle.setText(item.getTitle());
            holder.binding.tvShiftTime.setText("Nhân viên nghỉ ngày này");
            holder.binding.ivShiftIcon.setImageResource(R.drawable.ic_close);
        } else {
            holder.binding.tvShiftTitle.setText(item.getTitle());
            holder.binding.tvShiftTime.setText(item.getTimeRange());
            holder.binding.ivShiftIcon.setImageResource(R.drawable.ic_late);
        }

        // 2. Cập nhật trạng thái RadioButton
        holder.binding.radioSelected.setChecked(isSelected);

        // 3. Cập nhật màu sắc & viền theo trạng thái bằng ColorUtil
        int primaryColor = ColorUtil.getThemeColor(holder.itemView, R.attr.colorPrimary);
        int bgSelected = ColorUtil.getThemeColor(holder.itemView, R.attr.colorPrimaryContainer);
        int bgDefault = ColorUtil.getThemeColor(holder.itemView, R.attr.colorSurfaceVariant);
        int defaultIconColor = ColorUtil.getThemeColor(holder.itemView, R.attr.colorOnSurfaceVariant);
        float density = context.getResources().getDisplayMetrics().density;

        if (isSelected) {
            // Khi chọn: Đặt viền 1dp màu primaryColor
            holder.binding.cardShiftOption.setCardBackgroundColor(ColorStateList.valueOf(bgSelected));
            holder.binding.cardShiftOption.setStrokeColor(primaryColor);
            holder.binding.cardShiftOption.setStrokeWidth((int) (2.0f * density));
            holder.binding.ivShiftIcon.setImageTintList(ColorStateList.valueOf(primaryColor));
        } else {
            // Khi không chọn: Đặt màu nền colorSurface, viền 0dp (hoặc màu colorSurface đồng bộ)
            holder.binding.cardShiftOption.setCardBackgroundColor(ColorStateList.valueOf(bgDefault));
            holder.binding.cardShiftOption.setStrokeColor(bgDefault);
            holder.binding.cardShiftOption.setStrokeWidth(0);
            holder.binding.ivShiftIcon.setImageTintList(ColorStateList.valueOf(defaultIconColor));
        }

        // 4. Xử lý sự kiện Click chọn toàn bộ hàng
        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            setSelectedShift(isNoneOption ? null : item);

            if (listener != null) {
                listener.onShiftSelected(isNoneOption ? null : item);
            }
        });
    }

    private boolean isSameShift(ShiftOption s1, ShiftOption s2) {
        if (s1 == null || s2 == null) return false;
        if (s1.getId() == null && s2.getId() == null) return true;
        if (s1.getId() != null && s2.getId() != null) return s1.getId().equals(s2.getId());
        return false;
    }

    private int getShiftPosition(ShiftOption shift) {
        if (shift == null) return -1;
        for (int i = 0; i < shifts.size(); i++) {
            if (isSameShift(shifts.get(i), shift)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return shifts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSelectShiftOptionBinding binding;

        public ViewHolder(ItemSelectShiftOptionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
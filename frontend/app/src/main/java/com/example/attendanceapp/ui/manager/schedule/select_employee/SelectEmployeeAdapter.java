package com.example.attendanceapp.ui.manager.schedule.select_employee;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.ItemSelectEmployeeBinding;
import com.example.attendanceapp.domain.model.EmployeeStatus;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SelectEmployeeAdapter extends RecyclerView.Adapter<SelectEmployeeAdapter.ViewHolder> {

    public interface OnEmployeeCheckListener {
        void onCheckChanged(ManagerEmployeeItem employee, boolean isChecked);
    }

    private final List<ManagerEmployeeItem> list;
    private final Set<String> selectedEmployeeIds;
    private final Map<String, String> assignedShiftsMap = new HashMap<>();
    private final OnEmployeeCheckListener listener;

    public SelectEmployeeAdapter(
            List<ManagerEmployeeItem> list,
            Set<String> selectedEmployeeIds,
            OnEmployeeCheckListener listener
    ) {
        this.list = list != null
                ? new ArrayList<>(list)
                : new ArrayList<>();

        this.selectedEmployeeIds = selectedEmployeeIds;
        this.listener = listener;
    }

    public void setAssignedShiftsMap(Map<String, String> map) {
        this.assignedShiftsMap.clear();
        if (map != null) {
            this.assignedShiftsMap.putAll(map);
        }
        notifyDataSetChanged();
    }

    public void updateData(List<ManagerEmployeeItem> newList) {
        list.clear();
        if (newList != null) {
            list.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemSelectEmployeeBinding binding =
                ItemSelectEmployeeBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        ManagerEmployeeItem item = list.get(position);
        Context context = holder.itemView.getContext();

        // THÔNG TIN NHÂN VIÊN
        holder.binding.tvName.setText(item.getFullName());

        com.example.attendanceapp.core.utils.AvatarLoader.loadAvatar(context, item.getAvatarUrl(), holder.binding.ivAvatar);

        String employeeCode = item.getEmployeeCode() != null
                ? item.getEmployeeCode()
                : "";

        String positionName = item.getPosition() != null
                ? item.getPosition()
                : "";

        holder.binding.tvCodePosition.setText(
                employeeCode + " · " + positionName
        );

        boolean hasShift = assignedShiftsMap.containsKey(item.getUserId());
        String shiftName = hasShift ? assignedShiftsMap.get(item.getUserId()) : null;

        if (hasShift) {
            // 1. NHÂN VIÊN ĐÃ CÓ CA TRONG NGÀY: Làm mờ và đánh dấu "Đã có ca"
            holder.binding.cardRoot.setAlpha(0.45f);
            updateCardStyle(holder.binding.cardRoot, false);

            String label = (shiftName != null && !shiftName.isEmpty() && !shiftName.equals("Đã có ca"))
                    ? "Đã có ca: " + shiftName
                    : "Đã có ca";
            holder.binding.chipStatus.setText(label);

            int outlineColor = ColorUtil.getThemeColor(context, com.google.android.material.R.attr.colorOutline);
            int surfaceVariantColor = ColorUtil.getThemeColor(context, com.google.android.material.R.attr.colorSurfaceVariant);

            holder.binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(surfaceVariantColor));
            holder.binding.chipStatus.setTextColor(ColorStateList.valueOf(outlineColor));

            // Click vào nhân viên đã có ca: Hiển thị Toast thông báo lý do không cho chọn
            holder.binding.cardRoot.setOnClickListener(v -> {
                Toast.makeText(context,
                        "Nhân viên " + item.getFullName() + " đã có ca (" + label + ") trong ngày này. Mỗi nhân viên chỉ làm 1 ca/ngày.",
                        Toast.LENGTH_SHORT).show();
            });
        } else {
            // 2. NHÂN VIÊN CHƯA CÓ CA: Hiển thị bình thường, cho phép tick chọn
            holder.binding.cardRoot.setAlpha(1.0f);

            EmployeeStatus status = item.getStatus() != null
                    ? item.getStatus()
                    : EmployeeStatus.ACTIVE;

            holder.binding.chipStatus.setText(status.getLabel());
            holder.binding.chipStatus.setTextColor(
                    ContextCompat.getColor(context, status.getTextColorRes())
            );
            holder.binding.chipStatus.setChipBackgroundColor(
                    ColorStateList.valueOf(ContextCompat.getColor(context, status.getContainerColorRes()))
            );

            boolean isSelected = selectedEmployeeIds.contains(item.getUserId());
            updateCardStyle(holder.binding.cardRoot, isSelected);

            holder.binding.cardRoot.setOnClickListener(v -> {
                int currentPosition = holder.getBindingAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION) {
                    return;
                }

                boolean nextState = !selectedEmployeeIds.contains(item.getUserId());
                updateCardStyle(holder.binding.cardRoot, nextState);

                if (listener != null) {
                    listener.onCheckChanged(item, nextState);
                }
            });
        }
    }

    private void updateCardStyle(MaterialCardView card, boolean isSelected) {
        if (isSelected) {
            int colorPrimaryContainer = ColorUtil.getThemeColor(card, R.attr.colorPrimaryContainer);
            int colorPrimary = ColorUtil.getThemeColor(card, R.attr.colorPrimary);

            card.setCardBackgroundColor(colorPrimaryContainer);
            card.setStrokeColor(colorPrimary);
            card.setStrokeWidth(dpToPx(card.getContext(), 1));
        } else {
            int colorSurfaceVariant = ColorUtil.getThemeColor(card, R.attr.colorSurface);

            card.setCardBackgroundColor(colorSurfaceVariant);
            card.setStrokeWidth(0);
        }
    }

    private int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources()
                .getDisplayMetrics()
                .density
        );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSelectEmployeeBinding binding;

        public ViewHolder(@NonNull ItemSelectEmployeeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

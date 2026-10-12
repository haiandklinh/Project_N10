package com.example.attendanceapp.ui.manager.home;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.ItemManagerRecentCheckinBinding;
import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.RecentCheckInItem;
import java.util.List;

public class RecentCheckInAdapter extends RecyclerView.Adapter<RecentCheckInAdapter.ViewHolder> {

    private final List<RecentCheckInItem> items;

    public RecentCheckInAdapter(List<RecentCheckInItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemManagerRecentCheckinBinding binding = ItemManagerRecentCheckinBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemManagerRecentCheckinBinding binding;

        ViewHolder(ItemManagerRecentCheckinBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RecentCheckInItem item) {
            binding.tvName.setText(item.getEmployeeName());
            binding.tvCheckinInfo.setText(item.getShiftName() + " · " + item.getCheckInTime());
            
            AttendanceStatus status = item.getStatus();
            if (status != null) {
                binding.chipStatus.setText(status.getDisplayName());
                
                int color = ColorUtil.getThemeColor(binding.getRoot(), status.getColorAttr());
                int containerColor = ColorUtil.getThemeColor(binding.getRoot(), status.getContainerColorAttr());

                binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(containerColor));
                binding.chipStatus.setTextColor(color);
                binding.chipStatus.setChipStrokeColor(ColorStateList.valueOf(color));
            }

            Glide.with(binding.ivAvatar.getContext())
                    .load(item.getAvatarUrl())
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .circleCrop()
                    .into(binding.ivAvatar);
        }
    }
}
package com.example.attendanceapp.ui.manager.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.ItemManagerActionRequiredBinding;
import java.util.List;

public class ManagerActionAlertAdapter extends RecyclerView.Adapter<ManagerActionAlertAdapter.ViewHolder> {

    private final List<ManagerActionAlert> alerts;

    public ManagerActionAlertAdapter(List<ManagerActionAlert> alerts) {
        this.alerts = alerts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemManagerActionRequiredBinding binding = ItemManagerActionRequiredBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(alerts.get(position));
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemManagerActionRequiredBinding binding;

        ViewHolder(ItemManagerActionRequiredBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ManagerActionAlert alert) {
            binding.tvAlertTitle.setText(alert.getTitle());
            binding.tvAlertDescription.setText(alert.getDescription());
            binding.ivAlertIcon.setImageResource(alert.getIconRes());
            
            int color = ColorUtil.getThemeColor(binding.getRoot().getContext(), alert.getIconColorAttr());
            int bgIconColor = ColorUtil.getThemeColor(binding.getRoot().getContext(), alert.getBackgroundColorAttr());
            binding.ivAlertIcon.setColorFilter(color);
            binding.cardIcon.setCardBackgroundColor(bgIconColor);

            binding.getRoot().setOnClickListener(v -> {
                if (alert.getAction() != null) {
                    alert.getAction().run();
                }
            });
        }
    }
}
package com.example.attendanceapp.ui.main.auth.company;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.data.dto.response.staff.StaffCompanyResponse;
import com.example.attendanceapp.databinding.ItemStaffCompanyBinding;

import java.util.ArrayList;
import java.util.List;

public class StaffCompanyAdapter extends RecyclerView.Adapter<StaffCompanyAdapter.CompanyViewHolder> {

    public interface OnCompanyClickListener {
        void onCompanyClick(StaffCompanyResponse company);
    }

    private final List<StaffCompanyResponse> items = new ArrayList<>();
    private final OnCompanyClickListener listener;

    public StaffCompanyAdapter(OnCompanyClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<StaffCompanyResponse> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CompanyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStaffCompanyBinding binding = ItemStaffCompanyBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CompanyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CompanyViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class CompanyViewHolder extends RecyclerView.ViewHolder {
        private final ItemStaffCompanyBinding binding;

        CompanyViewHolder(ItemStaffCompanyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(StaffCompanyResponse item) {
            binding.tvCompanyName.setText(item.getCompanyName() != null && !item.getCompanyName().isEmpty() 
                    ? item.getCompanyName() 
                    : "Doanh nghiệp");
            
            String manager = item.getManagerName() != null && !item.getManagerName().isEmpty() 
                    ? item.getManagerName() 
                    : "Quản lý";
            binding.tvManagerName.setText("Quản lý: " + manager);

            String pos = item.getPositionStaff() != null && !item.getPositionStaff().isEmpty()
                    ? item.getPositionStaff()
                    : "Nhân viên";
            binding.tvPosition.setText(pos);

            binding.cardCompany.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCompanyClick(item);
                }
            });
        }
    }
}

package com.example.attendanceapp.ui.main.onboarding;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.databinding.ItemOnboardingBinding;
import com.example.attendanceapp.domain.model.OnboardingItem;


import java.util.List;

public class OnboardingPagerAdapter extends RecyclerView.Adapter<OnboardingPagerAdapter.OnboardingViewHolder> {

    private final List<OnboardingItem> onboardingItems;

    public OnboardingPagerAdapter(List<OnboardingItem> onboardingItems) {
        this.onboardingItems = onboardingItems;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOnboardingBinding binding = ItemOnboardingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OnboardingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        OnboardingItem item = onboardingItems.get(position);
        holder.binding.imageOnboarding.setImageResource(item.getImageRes());
        holder.binding.textTitle.setText(item.getTitle());
        holder.binding.textDescription.setText(item.getDescription());
    }

    @Override
    public int getItemCount() {
        return (onboardingItems == null) ? 0 : onboardingItems.size();
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        final ItemOnboardingBinding binding;

        OnboardingViewHolder(ItemOnboardingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
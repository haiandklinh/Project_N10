package com.example.attendanceapp.ui.employee.home.category;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.databinding.ItemHomeCategoryBinding;
import com.example.attendanceapp.domain.model.CategoryType;

import java.util.Objects;
import java.util.function.Consumer;


//ListAdapter + DiffUtil + View Binding
public class EmployeeCategoryTypeAdapter extends ListAdapter<CategoryType, EmployeeCategoryTypeAdapter.CategoryViewHolder> {

    // Sử dụng Consumer (Java 8) hoặc Functional Interface ngắn gọn thay cho OnCategoryClickListener rườm rà
    private final Consumer<CategoryType> onCategoryClick;

    public EmployeeCategoryTypeAdapter(@NonNull Consumer<CategoryType> onCategoryClick) {
        super(DIFF_CALLBACK);
        this.onCategoryClick = onCategoryClick;
    }

    // 1. Cấu hình DiffUtil tối ưu cập nhật UI
    private static final DiffUtil.ItemCallback<CategoryType> DIFF_CALLBACK = new DiffUtil.ItemCallback<CategoryType>() {
        @Override
        public boolean areItemsTheSame(@NonNull CategoryType oldItem, @NonNull CategoryType newItem) {
            // So sánh Enum hoặc ID duy nhất của item
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull CategoryType oldItem, @NonNull CategoryType newItem) {
            // So sánh nội dung bên trong
            return Objects.equals(oldItem, newItem);
        }
    };

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeCategoryBinding binding = ItemHomeCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CategoryViewHolder(binding, onCategoryClick);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        // getItem(position) là hàm có sẵn của ListAdapter
        holder.bind(getItem(position));
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemHomeCategoryBinding binding;
        private CategoryType currentCategory;

        public CategoryViewHolder(@NonNull ItemHomeCategoryBinding binding, Consumer<CategoryType> onCategoryClick) {
            super(binding.getRoot());
            this.binding = binding;

            // 2. Tối ưu: Gán Click Listener 1 lần duy nhất trong Constructor
            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && currentCategory != null && onCategoryClick != null) {
                    onCategoryClick.accept(currentCategory);
                }
            });
        }

        public void bind(CategoryType categoryType) {
            this.currentCategory = categoryType;
            binding.tvCategoryName.setText(categoryType.getTitle());
            binding.ivCategoryIcon.setImageResource(categoryType.getIconResId());
        }
    }
}
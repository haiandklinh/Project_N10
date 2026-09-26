package com.example.attendanceapp.core.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendanceapp.databinding.LayoutToolbarAppBinding;

public class ToolbarApp extends ConstraintLayout {

    private LayoutToolbarAppBinding binding;

    public ToolbarApp(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ToolbarApp(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ToolbarApp(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding = LayoutToolbarAppBinding.inflate(LayoutInflater.from(context), this, true);

        // Tránh chạy Insets khi đang render ở cửa sổ Design XML
        if (!isInEditMode()) {
            setupEdgeToEdge();
        }
    }
    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
    }
    public void setTitle(String title) {
        if (binding != null) {
            binding.tvTitle.setText(title);
        }
    }
    public void setOnBackClickListener(Runnable onBackAction) {
        if (binding != null) {
            binding.ivBack.setOnClickListener(v -> {
                if (onBackAction != null) {
                    onBackAction.run();
                }
            });
        }
    }
    public void setBackButtonVisible(boolean isVisible) {
        if (binding != null) {
            binding.ivBack.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }
}
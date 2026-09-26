package com.example.attendanceapp.ui.main.auth.login;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.DialogRoleSelectionBinding;
import com.example.attendanceapp.domain.model.UserRole;
import com.google.android.material.card.MaterialCardView;

public class RoleSelectionDialog extends DialogFragment {

    private DialogRoleSelectionBinding binding;
    private OnRoleSelectedListener listener;

    public interface OnRoleSelectedListener {
        void onRoleSelected(UserRole role);
        void onDialogCanceled();
    }

    public static RoleSelectionDialog newInstance(OnRoleSelectedListener listener) {
        RoleSelectionDialog dialog = new RoleSelectionDialog();
        dialog.setListener(listener);
        return dialog;
    }

    public void setListener(OnRoleSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogRoleSelectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog == null || dialog.getWindow() == null) {
            return;
        }
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        );
    }

    private void setupViews() {
        binding.cardManager.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRoleSelected(UserRole.MANAGER);
                dismiss();
            }
        });

        binding.cardEmployee.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRoleSelected(UserRole.EMPLOYEE);
                dismiss();
            }
        });

        binding.btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDialogCanceled();
            }
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

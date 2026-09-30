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

import com.example.attendanceapp.databinding.DialogAuthRoleSelectionBinding;
import com.example.attendanceapp.domain.model.UserRole;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RoleSelectionDialog extends DialogFragment {

    public static final String TAG = "RoleSelectionDialog";
    public static final String REQUEST_KEY = "role_selection_request_key";
    public static final String BUNDLE_KEY_SELECTED_ROLE = "bundle_key_selected_role";

    private DialogAuthRoleSelectionBinding binding;

    public static RoleSelectionDialog newInstance() {
        return new RoleSelectionDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAuthRoleSelectionBinding.inflate(inflater, container, false);
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
    }

    private void setupViews() {
        Dialog dialog = getDialog();
        if (dialog == null || dialog.getWindow() == null) return;
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // Tính toán chiều rộng màn hình minus 48dp (24dp mỗi bên)
        int marginPx = (int) (24 * getResources().getDisplayMetrics().density);
        int width = getResources().getDisplayMetrics().widthPixels - (marginPx * 2);
        window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);


        // Chọn Quản lý -> Báo về Fragment cha là MANAGER
        binding.cardManager.setOnClickListener(v -> sendResultAndDismiss(UserRole.MANAGER));

        // Chọn Nhân viên -> Báo về Fragment cha là EMPLOYEE
        binding.cardEmployee.setOnClickListener(v -> sendResultAndDismiss(UserRole.EMPLOYEE));

        // Bấm Hủy
        binding.btnCancel.setOnClickListener(v -> dismiss());
    }

    private void sendResultAndDismiss(UserRole role) {
        Bundle result = new Bundle();
        result.putString(BUNDLE_KEY_SELECTED_ROLE, role.name());
        getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
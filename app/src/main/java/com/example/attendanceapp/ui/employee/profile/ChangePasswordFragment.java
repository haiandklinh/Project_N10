package com.example.attendanceapp.ui.employee.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.databinding.FragmentChangePasswordBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChangePasswordFragment extends Fragment {

    private FragmentChangePasswordBinding binding;

    public ChangePasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupInputWatchers();
        setupListeners();
    }

    private void setupToolbar() {
        binding.layoutToolbar.setTitle("Đổi mật khẩu");
        binding.layoutToolbar.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );
    }

    private void setupInputWatchers() {
        // Xóa thông báo lỗi khi người dùng gõ lại
        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilCurrentPassword.setError(null);
                binding.tilNewPassword.setError(null);
                binding.tilConfirmPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.etCurrentPassword.addTextChangedListener(clearErrorWatcher);
        binding.etNewPassword.addTextChangedListener(clearErrorWatcher);
        binding.etConfirmPassword.addTextChangedListener(clearErrorWatcher);
    }

    private void setupListeners() {
        binding.btnChangePassword.setOnClickListener(v -> handleChangePassword(v));
    }

    private void handleChangePassword(View view) {
        String currentPassword = binding.etCurrentPassword.getText() != null
                ? binding.etCurrentPassword.getText().toString().trim() : "";
        String newPassword = binding.etNewPassword.getText() != null
                ? binding.etNewPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null
                ? binding.etConfirmPassword.getText().toString().trim() : "";

        // Validations
        if (currentPassword.isEmpty()) {
            binding.tilCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại");
            return;
        }

        if (newPassword.isEmpty()) {
            binding.tilNewPassword.setError("Vui lòng nhập mật khẩu mới");
            return;
        }

        if (newPassword.length() < 6) {
            binding.tilNewPassword.setError("Mật khẩu mới phải có ít nhất 6 ký tự");
            return;
        }

        if (newPassword.equals(currentPassword)) {
            binding.tilNewPassword.setError("Mật khẩu mới không được trùng với mật khẩu hiện tại");
            return;
        }

        if (!confirmPassword.equals(newPassword)) {
            binding.tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        // TODO: Gọi AuthViewModel / Repository gửi request đổi mật khẩu lên API Server
        Toast.makeText(requireContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(view).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.attendanceapp.databinding.FragmentChangePasswordBinding;
import com.example.attendanceapp.ui.common.password.ChangePasswordViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EmployeeChangePasswordFragment extends Fragment {

    private FragmentChangePasswordBinding binding;
    private ChangePasswordViewModel viewModel;

    public EmployeeChangePasswordFragment() {
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
        viewModel = new ViewModelProvider(this).get(ChangePasswordViewModel.class);

        setupToolbar();
        setupInputWatchers();
        setupListeners();
        observeViewModel();
    }

    private void setupToolbar() {
        binding.layoutToolbar.setTitle("Đổi mật khẩu");
        binding.layoutToolbar.setOnBackClickListener(() ->
                Navigation.findNavController(requireView()).navigateUp()
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
        binding.btnChangePassword.setOnClickListener(v -> handleChangePassword());
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnChangePassword.setEnabled(!isLoading);
            binding.btnChangePassword.setText(isLoading ? "Đang cập nhật..." : "Cập nhật mật khẩu");
        });

        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof ChangePasswordViewModel.ChangePasswordState.Success) {
                Toast.makeText(requireContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            } else if (state instanceof ChangePasswordViewModel.ChangePasswordState.Error) {
                ChangePasswordViewModel.ChangePasswordState.Error err =
                        (ChangePasswordViewModel.ChangePasswordState.Error) state;
                switch (err.field) {
                    case CURRENT_PASSWORD:
                        binding.tilCurrentPassword.setError(err.message);
                        binding.etCurrentPassword.requestFocus();
                        break;
                    case NEW_PASSWORD:
                        binding.tilNewPassword.setError(err.message);
                        binding.etNewPassword.requestFocus();
                        break;
                    case CONFIRM_PASSWORD:
                        binding.tilConfirmPassword.setError(err.message);
                        binding.etConfirmPassword.requestFocus();
                        break;
                    case GENERAL:
                    default:
                        Toast.makeText(requireContext(), err.message, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

    private void handleChangePassword() {
        String currentPassword = binding.etCurrentPassword.getText() != null
                ? binding.etCurrentPassword.getText().toString().trim() : "";
        String newPassword = binding.etNewPassword.getText() != null
                ? binding.etNewPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null
                ? binding.etConfirmPassword.getText().toString().trim() : "";

        viewModel.changePassword(currentPassword, newPassword, confirmPassword);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

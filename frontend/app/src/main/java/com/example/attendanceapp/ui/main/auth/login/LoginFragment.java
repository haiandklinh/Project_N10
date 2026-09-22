package com.example.attendanceapp.ui.main.auth.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentLoginBinding;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.ui.employee.EmployeeMainActivity;
import com.example.attendanceapp.ui.manager.ManagerMainActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputLayout;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        observeViewModel();
        setupBottomSheet();
        setupViews();
        setupTextChangeListeners();
    }

    private void observeViewModel() {
        viewModel.getLoginState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof LoginState.Success) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);

                UserAccount user = ((LoginState.Success) state).user;
                Toast.makeText(requireContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                navigateToRoleHome(user.getRole());

            } else if (state instanceof LoginState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);

                String errorMsg = ((LoginState.Error) state).message;
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();

            } else if (state instanceof LoginState.Idle) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
            }
        });
    }

    private void navigateToRoleHome(UserRole role) {
        Intent intent;

        if (role == UserRole.MANAGER) {
            intent = new Intent(requireContext(), ManagerMainActivity.class);
        } else {
            intent = new Intent(requireContext(), EmployeeMainActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        requireActivity().finish();
    }

    private void setupBottomSheet() {
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(binding.bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);
    }

    private void setupViews() {
        binding.btnLogin.setOnClickListener(v -> performLogin());
        binding.tvForgotPassword.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        );
        binding.tvSignUp.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registerFragment)
        );
    }

    private void setupTextChangeListeners() {
        binding.edtPhone.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPhone.setError(null);
            }
        });

        binding.edtPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPassword.setError(null);
            }
        });
    }

    private void performLogin() {
        clearAllErrors();
        String phone = binding.edtPhone.getText().toString().trim();
        String password = binding.edtPassword.getText().toString();

        boolean isValid = true;
        TextInputLayout firstErrorTil = null; // Lưu lại ô lỗi đầu tiên tính từ trên xuống


        // 1. Kiểm tra Số điện thoại
        if (TextUtils.isEmpty(phone)) {
            binding.tilPhone.setError("Vui lòng nhập số điện thoại");
            isValid = false;
            if (firstErrorTil == null) firstErrorTil = binding.tilPhone;
        } else if (!Patterns.PHONE.matcher(phone).matches() || phone.length() < 9) {
            binding.tilPhone.setError("Số điện thoại không hợp lệ");
            isValid = false;
            if (firstErrorTil == null) firstErrorTil = binding.tilPhone;
        }

        // 3. Kiểm tra Mật khẩu
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Vui lòng nhập mật khẩu");
            isValid = false;
            if (firstErrorTil == null) firstErrorTil = binding.tilPassword;
        }

        // Nếu có lỗi -> Con trỏ tự động di chuyển tới ô lỗi đầu tiên (theo thứ tự trên xuống)
        if (!isValid) {
            if (firstErrorTil != null && firstErrorTil.getEditText() != null) {
                firstErrorTil.getEditText().requestFocus();
            }
            return;
        }

        // Bật loading & gọi ViewModel khi tất cả trường dữ liệu đều hợp lệ
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);
        viewModel.login(phone, password);
    }

    private void clearAllErrors() {
        binding.tilPhone.setError(null);
        binding.tilPassword.setError(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
package com.example.attendanceapp.ui.main.auth.login;

import android.content.Intent;
import android.os.Bundle;
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
    }

    private void observeViewModel() {
        viewModel.getLoginState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof LoginState.Success) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);

                UserAccount user = ((LoginState.Success) state).user;
                Toast.makeText(requireContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                // Truyền thẳng enum UserRole vào hàm điều hướng
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
    /**
     * Điều hướng sang Activity tương ứng với vai trò và đóng toàn bộ luồng Auth cũ
     */
    private void navigateToRoleHome(UserRole role) {
        Intent intent;

        if (role == UserRole.MANAGER) {
            intent = new Intent(requireContext(), ManagerMainActivity.class);
        } else {
            intent = new Intent(requireContext(), EmployeeMainActivity.class);
        }

        // FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK giúp xóa sạch LoginFragment & AuthActivity khỏi BackStack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Đóng Activity chứa LoginFragment
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
    private void performLogin() {
        String companyCode = binding.edtCompanyCode.getText().toString().trim();
        String phone = binding.edtPhone.getText().toString().trim();
        String password = binding.edtPassword.getText().toString();

        if (TextUtils.isEmpty(companyCode)) {
            showError("Vui lòng nhập mã công ty", binding.edtCompanyCode);
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            showError("Vui lòng nhập số điện thoại", binding.edtPhone);
            return;
        }

        if (!Patterns.PHONE.matcher(phone).matches() || phone.length() < 9) {
            showError("Số điện thoại không hợp lệ", binding.edtPhone);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            showError("Vui lòng nhập mật khẩu", binding.edtPassword);
            return;
        }

        // Nếu dữ liệu hợp lệ -> Gọi ViewModel
        viewModel.login(companyCode, phone, password);
    }

    // Hàm Helper phụ trợ cho gọn code
    private void showError(String message, View viewToFocus) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        if (viewToFocus != null) {
            viewToFocus.requestFocus();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
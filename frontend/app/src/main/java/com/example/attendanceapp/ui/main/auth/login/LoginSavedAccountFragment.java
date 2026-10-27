package com.example.attendanceapp.ui.main.auth.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.attendanceapp.databinding.FragmentAuthLoginSavedBinding;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.ui.employee.EmployeeMainActivity;
import com.example.attendanceapp.ui.manager.ManagerMainActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginSavedAccountFragment extends Fragment {

    private FragmentAuthLoginSavedBinding binding;
    private LoginSavedAccountViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAuthLoginSavedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginSavedAccountViewModel.class);

        setupViews();
        observeViewModel();

        viewModel.loadSavedAccount();
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!isLoading);
            binding.btnBiometric.setEnabled(!isLoading);
            binding.btnSwitchAccount.setEnabled(!isLoading);
        });

        viewModel.getLoginState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof LoginState.Success) {
                UserAccount user = ((LoginState.Success) state).user;
                Toast.makeText(requireContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                // Điều hướng sang Activity tương ứng
                navigateToRoleHome(user.getRole());

            } else if (state instanceof LoginState.Error) {
                String error = ((LoginState.Error) state).message;
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSavedAccountData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                binding.tvUserName.setText(data.fullName);
                binding.tvCompany.setText(data.companyName);
                binding.tvMaskedPhone.setText(maskPhone(data.phone));

                if (data.avatarUrl != null && !data.avatarUrl.isEmpty()) {
                    com.bumptech.glide.Glide.with(this)
                            .load(data.avatarUrl)
                            .placeholder(R.drawable.ic_avatar)
                            .error(R.drawable.ic_avatar)
                            .into(binding.ivUserAvatar);
                } else {
                    binding.ivUserAvatar.setImageResource(R.drawable.ic_avatar);
                }
            } else {
                // Không có tài khoản đã lưu -> chuyển về màn hình Login thường
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_loginSavedAccountFragment_to_loginFragment);
            }
        });
    }

    private void setupViews() {
        binding.btnLogin.setOnClickListener(v -> performPasswordLogin());

        binding.tvForgotPassword.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_loginSavedAccountFragment_to_forgotPasswordFragment)
        );

        binding.btnSwitchAccount.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_loginSavedAccountFragment_to_loginFragment)
        );

        binding.btnBiometric.setOnClickListener(v -> performBiometricLogin());

        binding.btnContactSupport.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Tính năng CSKH đang phát triển", Toast.LENGTH_SHORT).show()
        );
    }

    private void performPasswordLogin() {
        String password = binding.edtPassword.getText().toString();

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }
        binding.tilPassword.setError(null);

        viewModel.loginWithPassword(password);
    }

    private void performBiometricLogin() {
        // TODO: Xử lý xác thực sinh trắc học (BiometricPrompt)
        Toast.makeText(requireContext(), "Tính năng sinh trắc học đang phát triển", Toast.LENGTH_SHORT).show();
    }

    /**
     * Điều hướng sang Activity tương ứng với vai trò và xóa sạch luồng Auth khỏi BackStack
     */
    private void navigateToRoleHome(UserRole role) {
        if (role == UserRole.MANAGER) {
            Intent intent = new Intent(requireContext(), ManagerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        } else {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_loginSavedAccountFragment_to_staffCompanySelectionFragment);
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 4) + "***" + phone.substring(phone.length() - 3);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.example.attendanceapp.ui.main.auth.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.example.attendanceapp.domain.usecase.auth.AuthCallback;
import com.example.attendanceapp.ui.employee.EmployeeMainActivity;
import com.example.attendanceapp.ui.manager.ManagerMainActivity;
import com.google.android.material.textfield.TextInputLayout;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        setupDialogListeners();
        observeViewModel();
        setupViews();
        setupTextChangeListeners();
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!isLoading);
        });

        viewModel.getLoginState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof LoginState.Success) {
                UserAccount user = ((LoginState.Success) state).user;
                clearAllErrors();
                Toast.makeText(requireContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                // Kiểm tra xem User đã có Vai trò hay chưa
                if (user.getRole() != null) {
                    navigateToRoleHome(user.getRole());
                } else {
                    // Nếu chưa có vai trò -> Hiện Dialog chọn vai trò
                    showRoleSelectionDialog();
                }

            } else if (state instanceof LoginState.Error) {
                String errorMsg = ((LoginState.Error) state).message;
                showErrorOnInput(errorMsg);
            }
        });
    }

    /** Lắng nghe kết quả trả về từ RoleSelectionDialog qua FragmentResultListener */
    private void setupDialogListeners() {
        getChildFragmentManager().setFragmentResultListener(
                RoleSelectionDialog.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String action = result.getString(RoleSelectionDialog.RESULT_ACTION, "");

                    if (RoleSelectionDialog.ACTION_SUBMITTED.equals(action)) {
                        String roleStr = result.getString(RoleSelectionDialog.BUNDLE_KEY_ROLE, "");
                        UserRole role = UserRole.fromString(roleStr);

                        if (role == UserRole.MANAGER) {
                            String companyName = result.getString(RoleSelectionDialog.BUNDLE_KEY_COMPANY_NAME, "");
                            // Gọi ViewModel để xử lý chọn role và tạo công ty
                            viewModel.handleManagerRoleSelection(companyName, new AuthCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    navigateToRoleHome(UserRole.MANAGER);
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else if (role == UserRole.EMPLOYEE) {
                            String invitationCode = result.getString(RoleSelectionDialog.BUNDLE_KEY_INVITATION_CODE, "");
                            // Gọi ViewModel để xử lý chọn role và tham gia công ty
                            viewModel.handleEmployeeRoleSelection(invitationCode, new AuthCallback<String>() {
                                @Override
                                public void onSuccess(String companyName) {
                                    navigateToPendingApproval(invitationCode, companyName);
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
        );
    }

    private void showRoleSelectionDialog() {
        RoleSelectionDialog.newInstance()
                .show(getChildFragmentManager(), RoleSelectionDialog.TAG);
    }

    private void navigateToPendingApproval(String inviteCode, String companyName) {
        Bundle bundle = new Bundle();
        bundle.putString("invite_code", inviteCode);
        bundle.putString("company_name", companyName);

        Navigation.findNavController(requireView())
                .navigate(R.id.action_loginFragment_to_pendingApprovalFragment, bundle);
    }

    private void navigateToRoleHome(UserRole role) {
        Intent intent;

        if (role == UserRole.MANAGER) {
            intent = new Intent(requireContext(), ManagerMainActivity.class);
        } else {
            intent = new Intent(requireContext(), EmployeeMainActivity.class);
        }

        // Mở Activity mới và xóa toàn bộ luồng Auth khỏi BackStack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        requireActivity().finish();
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
        String password = binding.edtPassword.getText().toString().trim();

        boolean isValid = true;
        TextInputLayout firstErrorTil = null;

        // 1. Kiểm tra Số điện thoại
        if (TextUtils.isEmpty(phone)) {
            binding.tilPhone.setError("Vui lòng nhập số điện thoại");
            isValid = false;
            if (firstErrorTil == null) firstErrorTil = binding.tilPhone;
        } else if (!Patterns.PHONE.matcher(phone).matches() || !phone.matches("^(0|\\+84)[0-9]{9,10}$")) {
            binding.tilPhone.setError("Số điện thoại không đúng định dạng");
            isValid = false;
            if (firstErrorTil == null) firstErrorTil = binding.tilPhone;
        }

        // 2. Kiểm tra Mật khẩu
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Vui lòng nhập mật khẩu");
            isValid = false;
            if (firstErrorTil == null) firstErrorTil = binding.tilPassword;
        } else if (password.length() < 6) {
            binding.tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            isValid = false;
            if (firstErrorTil == null) firstErrorTil = binding.tilPassword;
        }

        if (!isValid) {
            if (firstErrorTil != null && firstErrorTil.getEditText() != null) {
                firstErrorTil.getEditText().requestFocus();
            }
            return;
        }

        // Gọi ViewModel xử lý đăng nhập
        viewModel.login(phone, password);
    }

    private void clearAllErrors() {
        binding.tilPhone.setError(null);
        binding.tilPassword.setError(null);
    }

    private void showErrorOnInput(String errorMsg) {
        clearAllErrors();
        
        if (errorMsg.contains("Số điện thoại") || errorMsg.contains("phone")) {
            binding.tilPhone.setError(errorMsg);
            if (binding.tilPhone.getEditText() != null) {
                binding.tilPhone.getEditText().requestFocus();
            }
        } else {
            binding.tilPassword.setError(errorMsg);
            if (binding.tilPassword.getEditText() != null) {
                binding.tilPassword.getEditText().requestFocus();
            }
        }
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
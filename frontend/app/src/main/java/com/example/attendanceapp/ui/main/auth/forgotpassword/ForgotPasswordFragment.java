package com.example.attendanceapp.ui.main.auth.forgotpassword;

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
import com.example.attendanceapp.databinding.FragmentAuthForgotPasswordBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ForgotPasswordFragment extends Fragment {

    private static final String OTP_REQUEST_KEY = "forgot_password_otp_verify_request";

    private FragmentAuthForgotPasswordBinding binding;
    private ForgotPasswordViewModel viewModel;

    private boolean isPhoneVerified = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAuthForgotPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);

        observeViewModel();
        setupOtpResultListener();
        setupSteps();
        showStep(1);
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            if (binding.layoutStep2 != null && binding.layoutStep2.btnResetPasswordFinal != null) {
                binding.layoutStep2.btnResetPasswordFinal.setEnabled(!isLoading);
            }
            if (binding.layoutStep1 != null && binding.layoutStep1.btnNextStep1 != null) {
                binding.layoutStep1.btnNextStep1.setEnabled(!isLoading);
            }
        });

        viewModel.getForgotPasswordState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof ForgotPasswordViewModel.ForgotPasswordState.OtpSent) {
                String phone = ((ForgotPasswordViewModel.ForgotPasswordState.OtpSent) state).account;
                openOtpBottomSheet(phone);
            } else if (state instanceof ForgotPasswordViewModel.ForgotPasswordState.Success) {
                clearAllErrors();
                Toast.makeText(requireContext(), R.string.msg_password_reset_success, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).popBackStack();
            } else if (state instanceof ForgotPasswordViewModel.ForgotPasswordState.Error) {
                String error = ((ForgotPasswordViewModel.ForgotPasswordState.Error) state).message;
                showErrorOnInput(error);
            }
        });
    }

    private void setupOtpResultListener() {
        getChildFragmentManager().setFragmentResultListener(
                OTP_REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String verifiedPhone = result.getString(ForgotPasswordOtpBottomSheetFragment.RESULT_VERIFIED_PHONE, "");
                    if (!TextUtils.isEmpty(verifiedPhone)) {
                        onPhoneVerified(verifiedPhone);
                    }
                }
        );
    }

    private void setupSteps() {
        // Step 1: Số điện thoại & OTP
        binding.layoutStep1.btnNextStep1.setOnClickListener(v -> handleContinueStep1());

        binding.layoutStep1.edtPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isPhoneVerified) {
                    resetPhoneVerification();
                }
            }
        });

        // Step 2: Mật khẩu mới
        binding.layoutStep2.btnResetPasswordFinal.setOnClickListener(v -> {
            if (validateStep2()) {
                performResetPassword();
            }
        });
        binding.layoutStep2.tvBackStep1.setOnClickListener(v -> showStep(1));
    }

    private void handleContinueStep1() {
        if (!isPhoneVerified) {
            if (!validateStep1()) return;
            String phone = binding.layoutStep1.edtPhone.getText().toString().trim();
            viewModel.sendOtp(phone);
        } else {
            showStep(2);
        }
    }

    private void openOtpBottomSheet(String phone) {
        ForgotPasswordOtpBottomSheetFragment bottomSheet = ForgotPasswordOtpBottomSheetFragment.newInstance(phone, OTP_REQUEST_KEY);
        bottomSheet.show(getChildFragmentManager(), ForgotPasswordOtpBottomSheetFragment.TAG);
    }

    private void onPhoneVerified(String verifiedPhone) {
        isPhoneVerified = true;
        binding.layoutStep1.layoutVerifiedStatus.setVisibility(View.VISIBLE);
        binding.layoutStep1.tvVerifiedPhone.setText(
                getString(R.string.auth_phone_verified_format, maskPhone(verifiedPhone))
        );
        binding.layoutStep1.edtPhone.setEnabled(false);

        showStep(2);
    }

    private void resetPhoneVerification() {
        isPhoneVerified = false;
        binding.layoutStep1.layoutVerifiedStatus.setVisibility(View.GONE);
        binding.layoutStep1.edtPhone.setEnabled(true);
    }

    private void showStep(int step) {
        binding.layoutStep1.getRoot().setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        binding.layoutStep2.getRoot().setVisibility(step == 2 ? View.VISIBLE : View.GONE);
    }

    private boolean validateStep1() {
        String phone = binding.layoutStep1.edtPhone.getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            binding.layoutStep1.tilPhone.setError(getString(R.string.auth_error_phone_required));
            return false;
        }
        if (!Patterns.PHONE.matcher(phone).matches() || phone.length() < 9) {
            binding.layoutStep1.tilPhone.setError(getString(R.string.auth_error_phone_invalid));
            return false;
        }
        binding.layoutStep1.tilPhone.setError(null);
        return true;
    }

    private boolean validateStep2() {
        String pass = binding.layoutStep2.edtPassword.getText().toString();
        String confirm = binding.layoutStep2.edtConfirmPassword.getText().toString();

        if (pass.length() < 6) {
            binding.layoutStep2.tilPassword.setError(getString(R.string.auth_error_password_min));
            return false;
        }
        binding.layoutStep2.tilPassword.setError(null);

        if (!pass.equals(confirm)) {
            binding.layoutStep2.tilConfirmPassword.setError(getString(R.string.auth_error_password_mismatch));
            return false;
        }
        binding.layoutStep2.tilConfirmPassword.setError(null);

        return true;
    }

    private void performResetPassword() {
        String password = binding.layoutStep2.edtPassword.getText().toString();
        String confirmPassword = binding.layoutStep2.edtConfirmPassword.getText().toString();

        viewModel.resetPassword(password, confirmPassword);
    }

    private String maskPhone(String phone) {
        if (phone.length() < 7) return phone;
        return phone.substring(0, 4) + "***" + phone.substring(phone.length() - 3);
    }

    private void clearAllErrors() {
        binding.layoutStep1.tilPhone.setError(null);
        binding.layoutStep2.tilPassword.setError(null);
        binding.layoutStep2.tilConfirmPassword.setError(null);
    }

    private void showErrorOnInput(String errorMsg) {
        clearAllErrors();

        if (errorMsg.contains("Số điện thoại") || errorMsg.contains("phone")) {
            showStep(1);
            binding.layoutStep1.tilPhone.setError(errorMsg);
        } else if (errorMsg.contains("mật khẩu") || errorMsg.contains("Mật khẩu") || errorMsg.contains("password")) {
            // Chỉ khi đang ở Step 2 (đã verify xong) thì mới hiện lỗi mật khẩu
            if (isPhoneVerified) {
                showStep(2);
                binding.layoutStep2.tilPassword.setError(errorMsg);
            }
        } else {
            // Các lỗi khác (như sai OTP): Giữ nguyên step hiện tại, không tự nhảy layout
            if (!isPhoneVerified) {
                showStep(1);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.example.attendanceapp.ui.main.auth.register;

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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentAuthRegisterBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    private static final String OTP_REQUEST_KEY = "otp_verify_request";

    private FragmentAuthRegisterBinding binding;
    private RegisterViewModel registerViewModel;

    private boolean isPhoneVerified = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAuthRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        observeViewModel();
        setupOtpResultListener();
        setupSteps();
        handleArguments();
        showStep(1);
    }

    private void handleArguments() {
        if (getArguments() != null) {
            String phone = getArguments().getString("phone_number");
            if (TextUtils.isEmpty(phone)) {
                phone = getArguments().getString("phone");
            }
            if (!TextUtils.isEmpty(phone)) {
                binding.layoutStep1.edtPhone.setText(phone);
            }
        }
    }

    private void observeViewModel() {
        registerViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

            // Nút chỉ enable khi không load VÀ đã tích checkbox
            binding.layoutStep2.btnRegisterFinal.setEnabled(!isLoading && binding.layoutStep2.cbAcceptTerms.isChecked());
            binding.layoutStep1.btnNextStep1.setEnabled(!isLoading);
        });

        registerViewModel.getRegisterState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof RegisterState.Success) {
                clearAllErrors();
                Toast.makeText(requireContext(), "Đăng ký thành công! Đang thiết lập phiên đăng nhập...", Toast.LENGTH_SHORT).show();

                String phone = binding.layoutStep1.edtPhone.getText().toString().trim();
                String password = binding.layoutStep2.edtPassword.getText().toString().trim();
                NavController navController = Navigation.findNavController(requireView());

                // Truyền thông tin sang LoginFragment để tự động đăng nhập ngầm (Auto-login)
                if (navController.getPreviousBackStackEntry() != null) {
                    navController.getPreviousBackStackEntry()
                            .getSavedStateHandle()
                            .set("auto_login_phone", phone);
                    navController.getPreviousBackStackEntry()
                            .getSavedStateHandle()
                            .set("auto_login_password", password);
                    navController.getPreviousBackStackEntry()
                            .getSavedStateHandle()
                            .set("registered_phone", phone);
                }

                navController.popBackStack();
            } else if (state instanceof RegisterState.Error) {
                String error = ((RegisterState.Error) state).message;
                showErrorOnInput(error);
            }
        });

        registerViewModel.getOtpState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof OtpState.Sent) {
                // SĐT hợp lệ và không trùng -> Tiến hành mở BottomSheet OTP
                String phone = ((OtpState.Sent) state).phone;
                openOtpBottomSheet(phone);
            } else if (state instanceof OtpState.Error) {
                // Trùng SĐT hoặc lỗi gửi OTP -> Hiển thị lỗi ngay Step 1
                String error = ((OtpState.Error) state).message;
                showErrorOnStep2(error);
            }
        });
    }

    private void setupOtpResultListener() {
        getChildFragmentManager().setFragmentResultListener(
                OTP_REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String verifiedPhone = result.getString(OtpBottomSheetFragment.RESULT_VERIFIED_PHONE, "");
                    if (!TextUtils.isEmpty(verifiedPhone)) {
                        onPhoneVerified(verifiedPhone);
                    }
                }
        );
    }

    private void setupSteps() {
        setupTermsCheckbox();

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

        // Step 2: Mật khẩu
        binding.layoutStep2.btnRegisterFinal.setOnClickListener(v -> {
            if (validateStep2()) {
                performRegister();
            }
        });
        binding.layoutStep2.tvBackStep1.setOnClickListener(v -> showStep(1));
    }

    private void handleContinueStep1() {
        if (!isPhoneVerified) {
            if (!validateStep1()) return;
            String phone = binding.layoutStep1.edtPhone.getText().toString().trim();
            registerViewModel.checkPhoneAndSendOtp(phone);
        } else {
            showStep(2);
        }
    }

    private void openOtpBottomSheet(String phone) {
        OtpBottomSheetFragment bottomSheet = OtpBottomSheetFragment.newInstance(phone, OTP_REQUEST_KEY);
        bottomSheet.show(getChildFragmentManager(), OtpBottomSheetFragment.TAG);
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

    private void setupTermsCheckbox() {
        binding.layoutStep2.cbAcceptTerms.setOnCheckedChangeListener((buttonView, isChecked) ->
                binding.layoutStep2.btnRegisterFinal.setEnabled(isChecked)
        );
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
        boolean acceptTerms = binding.layoutStep2.cbAcceptTerms.isChecked();

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

        if (!acceptTerms) {
            Toast.makeText(requireContext(), R.string.auth_error_accept_terms, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void performRegister() {
        String phone = binding.layoutStep1.edtPhone.getText().toString().trim();
        String password = binding.layoutStep2.edtPassword.getText().toString();

        registerViewModel.register(phone, password);
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

        if (errorMsg.contains("Số điện thoại") || errorMsg.contains("phone") || errorMsg.contains("đăng ký")) {
            showStep(1);
            binding.layoutStep1.tilPhone.setError(errorMsg);
        } else if (errorMsg.contains("mật khẩu") || errorMsg.contains("Mật khẩu") || errorMsg.contains("password")) {
            if (isPhoneVerified) {
                showStep(2);
                binding.layoutStep2.tilPassword.setError(errorMsg);
            }
        } else {
            if (!isPhoneVerified) {
                showStep(1);
            }
        }
    }

    private void showErrorOnStep2(String errorMsg) {
        showStep(1);
        binding.layoutStep1.tilPhone.setError(errorMsg);
        if (binding.layoutStep1.tilPhone.getEditText() != null) {
            binding.layoutStep1.tilPhone.getEditText().requestFocus();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
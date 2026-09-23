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
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentRegisterBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    private static final String OTP_REQUEST_KEY = "otp_verify_request";

    private FragmentRegisterBinding binding;
    private RegisterViewModel viewModel;

    private boolean isPhoneVerified = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        observeViewModel();
        setupOtpResultListener();
        setupSteps();
        showStep(1);
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.layoutStep3.btnRegisterFinal.setEnabled(!isLoading);
            binding.layoutStep2.btnContinueStep2.setEnabled(!isLoading);
        });

        viewModel.getRegisterState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof RegisterViewModel.RegisterState.Success) {
                clearAllErrors();
                Toast.makeText(requireContext(), R.string.auth_register_success, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).popBackStack();
            } else if (state instanceof RegisterViewModel.RegisterState.Error) {
                String error = ((RegisterViewModel.RegisterState.Error) state).message;
                showErrorOnInput(error);
            }
        });

        // BỔ SUNG: Observe OTP state (Lắng nghe kết quả gửi OTP và lỗi kiểm tra SĐT)
        viewModel.getOtpState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof RegisterViewModel.OtpState.Sent) {
                // SĐT hợp lệ và không trùng -> Tiến hành mở BottomSheet OTP
                String phone = ((RegisterViewModel.OtpState.Sent) state).phone;
                openOtpBottomSheet(phone);
            } else if (state instanceof RegisterViewModel.OtpState.Error) {
                // Trùng SĐT hoặc lỗi gửi OTP -> Hiển thị lỗi ngay Step 2
                String error = ((RegisterViewModel.OtpState.Error) state).message;
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

        // Step 1: Họ tên
        binding.layoutStep1.btnNextStep1.setOnClickListener(v -> {
            if (validateStep1()) {
                showStep(2);
            }
        });

        // Step 2: Số điện thoại
        binding.layoutStep2.tvBackStep1.setOnClickListener(v -> showStep(1));
        binding.layoutStep2.btnContinueStep2.setOnClickListener(v -> handleContinueStep2());

        binding.layoutStep2.edtPhone.addTextChangedListener(new TextWatcher() {
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

        // Step 3: Mật khẩu
        binding.layoutStep3.btnRegisterFinal.setOnClickListener(v -> {
            if (validateStep3()) {
                performRegister();
            }
        });
        binding.layoutStep3.tvBackStep2.setOnClickListener(v -> showStep(2));
    }

    private void handleContinueStep2() {
        if (!isPhoneVerified) {
            if (!validateStep2()) return;
            String phone = binding.layoutStep2.edtPhone.getText().toString().trim();

            // ĐỔI: Gọi hàm kiểm tra trùng SĐT trước khi gửi OTP
            viewModel.checkPhoneAndSendOtp(phone);
        } else {
            showStep(3);
        }
    }

    private void openOtpBottomSheet(String phone) {
        OtpBottomSheetFragment bottomSheet = OtpBottomSheetFragment.newInstance(phone, OTP_REQUEST_KEY);
        bottomSheet.show(getChildFragmentManager(), OtpBottomSheetFragment.TAG);
    }

    private void onPhoneVerified(String verifiedPhone) {
        isPhoneVerified = true;
        binding.layoutStep2.layoutVerifiedStatus.setVisibility(View.VISIBLE);
        binding.layoutStep2.tvVerifiedPhone.setText(
                getString(R.string.auth_phone_verified_format, maskPhone(verifiedPhone))
        );
        binding.layoutStep2.edtPhone.setEnabled(false);

        showStep(3);
    }

    private void resetPhoneVerification() {
        isPhoneVerified = false;
        binding.layoutStep2.layoutVerifiedStatus.setVisibility(View.GONE);
        binding.layoutStep2.edtPhone.setEnabled(true);
    }

    private void setupTermsCheckbox() {
        binding.layoutStep3.cbAcceptTerms.setOnCheckedChangeListener((buttonView, isChecked) ->
                binding.layoutStep3.btnRegisterFinal.setEnabled(isChecked)
        );
    }

    private void showStep(int step) {
        binding.layoutStep1.getRoot().setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        binding.layoutStep2.getRoot().setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        binding.layoutStep3.getRoot().setVisibility(step == 3 ? View.VISIBLE : View.GONE);
    }

    private boolean validateStep1() {
        String name = binding.layoutStep1.edtFullName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.layoutStep1.tilFullName.setError(getString(R.string.auth_error_full_name_required));
            return false;
        }
        binding.layoutStep1.tilFullName.setError(null);
        return true;
    }

    private boolean validateStep2() {
        String phone = binding.layoutStep2.edtPhone.getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            binding.layoutStep2.tilPhone.setError(getString(R.string.auth_error_phone_required));
            return false;
        }
        if (!Patterns.PHONE.matcher(phone).matches() || phone.length() < 9) {
            binding.layoutStep2.tilPhone.setError(getString(R.string.auth_error_phone_invalid));
            return false;
        }
        binding.layoutStep2.tilPhone.setError(null);
        return true;
    }

    private boolean validateStep3() {
        String pass = binding.layoutStep3.edtPassword.getText().toString();
        String confirm = binding.layoutStep3.edtConfirmPassword.getText().toString();
        boolean acceptTerms = binding.layoutStep3.cbAcceptTerms.isChecked();

        if (pass.length() < 6) {
            binding.layoutStep3.tilPassword.setError(getString(R.string.auth_error_password_min));
            return false;
        }
        binding.layoutStep3.tilPassword.setError(null);

        if (!pass.equals(confirm)) {
            binding.layoutStep3.tilConfirmPassword.setError(getString(R.string.auth_error_password_mismatch));
            return false;
        }
        binding.layoutStep3.tilConfirmPassword.setError(null);

        if (!acceptTerms) {
            Toast.makeText(requireContext(), R.string.auth_error_accept_terms, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void performRegister() {
        String fullName = binding.layoutStep1.edtFullName.getText().toString().trim();
        String phone = binding.layoutStep2.edtPhone.getText().toString().trim();
        String password = binding.layoutStep3.edtPassword.getText().toString();

        viewModel.register(fullName, phone, password);
    }

    private String maskPhone(String phone) {
        if (phone.length() < 7) return phone;
        return phone.substring(0, 4) + "***" + phone.substring(phone.length() - 3);
    }

    private void clearAllErrors() {
        binding.layoutStep1.tilFullName.setError(null);
        binding.layoutStep2.tilPhone.setError(null);
        binding.layoutStep3.tilPassword.setError(null);
        binding.layoutStep3.tilConfirmPassword.setError(null);
    }

    private void showErrorOnInput(String errorMsg) {
        clearAllErrors();

        if (errorMsg.contains("Họ tên") || errorMsg.contains("tên")) {
            showStep(1);
            binding.layoutStep1.tilFullName.setError(errorMsg);
            if (binding.layoutStep1.tilFullName.getEditText() != null) {
                binding.layoutStep1.tilFullName.getEditText().requestFocus();
            }
        } else if (errorMsg.contains("Số điện thoại") || errorMsg.contains("phone") || errorMsg.contains("đăng ký")) {
            showStep(2);
            binding.layoutStep2.tilPhone.setError(errorMsg);
            if (binding.layoutStep2.tilPhone.getEditText() != null) {
                binding.layoutStep2.tilPhone.getEditText().requestFocus();
            }
        } else {
            showStep(3);
            binding.layoutStep3.tilPassword.setError(errorMsg);
            if (binding.layoutStep3.tilPassword.getEditText() != null) {
                binding.layoutStep3.tilPassword.getEditText().requestFocus();
            }
        }
    }

    private void showErrorOnStep2(String errorMsg) {
        showStep(2);
        binding.layoutStep2.tilPhone.setError(errorMsg);
        if (binding.layoutStep2.tilPhone.getEditText() != null) {
            binding.layoutStep2.tilPhone.getEditText().requestFocus();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
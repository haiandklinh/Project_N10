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
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentRegisterBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    private static final String OTP_REQUEST_KEY = "otp_verify_request";
    private static final String OTP_RESULT_VERIFIED_PHONE = "verified_phone";

    private FragmentRegisterBinding binding;
    private RegisterViewModel viewModel;

    private int currentStep = 1;
    private boolean isPhoneVerified = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        });

        viewModel.getRegisterState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof RegisterViewModel.RegisterState.Success) {
                Toast.makeText(requireContext(), R.string.auth_register_success, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).popBackStack();
            } else if (state instanceof RegisterViewModel.RegisterState.Error) {
                String error = ((RegisterViewModel.RegisterState.Error) state).message;
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Lang nghe ket qua xac thuc OTP tra ve tu OtpBottomSheetFragment */
    private void setupOtpResultListener() {
        getParentFragmentManager().setFragmentResultListener(
                OTP_REQUEST_KEY,
                getViewLifecycleOwner(),
                (FragmentResultListener) (requestKey, result) -> {
                    String verifiedPhone = result.getString(OTP_RESULT_VERIFIED_PHONE, "");
                    if (!TextUtils.isEmpty(verifiedPhone)) {
                        onPhoneVerified(verifiedPhone);
                    }
                }
        );
    }

    private void setupSteps() {
        setupTermsCheckbox();
        setupJoinCompanyLink();

        // Step 1: Ten & Cong ty
        binding.layoutStep1.btnNextStep1.setOnClickListener(v -> {
            if (validateStep1()) {
                showStep(2);
            }
        });

        // Step 2: So dien thoai (gui OTP gop trong nut Tiep tuc)
        binding.layoutStep2.tvBackStep1.setOnClickListener(v -> showStep(1));
        binding.layoutStep2.btnContinueStep2.setOnClickListener(v -> handleContinueStep2());

        // Neu nguoi dung sua lai so dien thoai sau khi da xac thuc -> reset trang thai
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

        // Step 3: Mat khau
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
            sendOtpAndOpenBottomSheet(phone);
        } else {
            showStep(3);
        }
    }

    private void sendOtpAndOpenBottomSheet(String phone) {
        viewModel.sendOtp(phone);
        OtpBottomSheetFragment bottomSheet = OtpBottomSheetFragment.newInstance(phone, OTP_REQUEST_KEY);
        bottomSheet.show(getParentFragmentManager(), OtpBottomSheetFragment.TAG);
    }

    private void onPhoneVerified(String verifiedPhone) {
        isPhoneVerified = true;
        binding.layoutStep2.layoutVerifiedStatus.setVisibility(View.VISIBLE);
        binding.layoutStep2.tvVerifiedPhone.setText(
                getString(R.string.auth_phone_verified_format, maskPhone(verifiedPhone))
        );
        binding.layoutStep2.edtPhone.setEnabled(false);
    }

    private void resetPhoneVerification() {
        isPhoneVerified = false;
        binding.layoutStep2.layoutVerifiedStatus.setVisibility(View.GONE);
    }

    private void setupTermsCheckbox() {
        binding.layoutStep3.cbAcceptTerms.setOnCheckedChangeListener((buttonView, isChecked) ->
                binding.layoutStep3.btnRegisterFinal.setEnabled(isChecked)
        );
    }

    private void setupJoinCompanyLink() {
        binding.layoutStep1.tvGoToJoinCompany.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Tính năng gia nhập công ty bằng mã đang phát triển",
                        Toast.LENGTH_SHORT).show()
        );
    }

    private void showStep(int step) {
        currentStep = step;
        binding.layoutStep1.getRoot().setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        binding.layoutStep2.getRoot().setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        binding.layoutStep3.getRoot().setVisibility(step == 3 ? View.VISIBLE : View.GONE);
    }

    private boolean validateStep1() {
        String name = binding.layoutStep1.edtFullName.getText().toString().trim();
        String company = binding.layoutStep1.edtCompanyName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.layoutStep1.tilFullName.setError(getString(R.string.auth_error_full_name_required));
            return false;
        }
        binding.layoutStep1.tilFullName.setError(null);

        if (TextUtils.isEmpty(company)) {
            binding.layoutStep1.tilCompanyName.setError(getString(R.string.auth_error_company_required));
            return false;
        }
        binding.layoutStep1.tilCompanyName.setError(null);
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
        String companyName = binding.layoutStep1.edtCompanyName.getText().toString().trim();
        String phone = binding.layoutStep2.edtPhone.getText().toString().trim();
        String password = binding.layoutStep3.edtPassword.getText().toString();

        viewModel.register(fullName, companyName, phone, password);
    }

    private String maskPhone(String phone) {
        if (phone.length() < 7) return phone;
        return phone.substring(0, 4) + "***" + phone.substring(phone.length() - 3);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
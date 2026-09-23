package com.example.attendanceapp.ui.main.auth.forgotpassword;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentForgotPasswordBinding;

import dagger.hilt.android.AndroidEntryPoint;

import java.util.Arrays;
import java.util.List;

@AndroidEntryPoint
public class ForgotPasswordFragment extends Fragment {

    private FragmentForgotPasswordBinding binding;
    private List<EditText> otpBoxes;
    private ForgotPasswordViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);

        setupViews();
        setupOtpInputs();
        setupListeners();
        observeViewModel();
    }

    private void setupViews() {
        // Danh sách 6 ô nhập OTP từ layout đã include
        otpBoxes = Arrays.asList(
                binding.includeStep2.edtOtp1,
                binding.includeStep2.edtOtp2,
                binding.includeStep2.edtOtp3,
                binding.includeStep2.edtOtp4,
                binding.includeStep2.edtOtp5,
                binding.includeStep2.edtOtp6
        );

        // Mặc định hiển thị Bước 1
        showStep(1);
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.includeStep1.btnSendOtp.setEnabled(!isLoading);
            binding.includeStep2.btnVerifyOtp.setEnabled(!isLoading);
            binding.includeStep2.tvResendOtp.setEnabled(!isLoading);
            binding.includeStep3.btnResetPassword.setEnabled(!isLoading);
        });

        viewModel.getForgotPasswordState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof ForgotPasswordViewModel.ForgotPasswordState.OtpSent) {
                Toast.makeText(requireContext(), getString(R.string.msg_otp_sent_success), Toast.LENGTH_SHORT).show();
                showStep(2);
            } else if (state instanceof ForgotPasswordViewModel.ForgotPasswordState.OtpVerified) {
                Toast.makeText(requireContext(), getString(R.string.msg_otp_verified_success), Toast.LENGTH_SHORT).show();
                showStep(3);
            } else if (state instanceof ForgotPasswordViewModel.ForgotPasswordState.OtpResent) {
                Toast.makeText(requireContext(), getString(R.string.msg_otp_resend_success), Toast.LENGTH_SHORT).show();
                clearOtpBoxes();
            } else if (state instanceof ForgotPasswordViewModel.ForgotPasswordState.Success) {
                Toast.makeText(requireContext(), getString(R.string.msg_password_reset_success), Toast.LENGTH_LONG).show();
                Navigation.findNavController(requireView()).navigateUp();
            } else if (state instanceof ForgotPasswordViewModel.ForgotPasswordState.Error) {
                String error = ((ForgotPasswordViewModel.ForgotPasswordState.Error) state).message;
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        // Nút Back trên cùng
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // BƯỚC 1: Nút Gửi mã xác thực OTP
        binding.includeStep1.btnSendOtp.setOnClickListener(v -> {
            String inputAccount = binding.includeStep1.edtAccountInput.getText().toString().trim();

            if (inputAccount.isEmpty()) {
                binding.includeStep1.tilAccountInput.setError(getString(R.string.error_enter_email_or_phone));
                return;
            }
            binding.includeStep1.tilAccountInput.setError(null);

            viewModel.sendOtp(inputAccount);
        });

        // BƯỚC 2: Nút Xác nhận OTP
        binding.includeStep2.btnVerifyOtp.setOnClickListener(v -> {
            String otpCode = getOtpCode();

            if (otpCode.length() < 6) {
                Toast.makeText(requireContext(), getString(R.string.error_enter_6_digits), Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.verifyOtp(otpCode);
        });

        // BƯỚC 2: Nút Gửi lại mã OTP
        binding.includeStep2.tvResendOtp.setOnClickListener(v -> {
            viewModel.resendOtp();
        });

        // BƯỚC 3: Nút Hoàn tất đặt lại mật khẩu mới
        binding.includeStep3.btnResetPassword.setOnClickListener(v -> {
            String newPassword = binding.includeStep3.edtNewPassword.getText().toString().trim();
            String confirmPassword = binding.includeStep3.edtConfirmPassword.getText().toString().trim();

            if (newPassword.isEmpty()) {
                binding.includeStep3.tilNewPassword.setError(getString(R.string.error_enter_new_password));
                return;
            }

            if (newPassword.length() < 6) {
                binding.includeStep3.tilNewPassword.setError(getString(R.string.error_password_min_length));
                return;
            }
            binding.includeStep3.tilNewPassword.setError(null);

            if (!newPassword.equals(confirmPassword)) {
                binding.includeStep3.tilConfirmPassword.setError(getString(R.string.error_password_not_match));
                return;
            }
            binding.includeStep3.tilConfirmPassword.setError(null);

            viewModel.resetPassword(newPassword, confirmPassword);
        });
    }

    /**
     * Xử lý tự động di chuyển con trỏ (Focus) và xóa lùi (Backspace) cho 6 ô OTP
     */
    private void setupOtpInputs() {
        for (int i = 0; i < otpBoxes.size(); i++) {
            final int currentIndex = i;

            // Bắt sự kiện khi gõ ký tự -> Nhảy sang ô tiếp theo
            otpBoxes.get(i).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s != null && s.length() == 1 && currentIndex < otpBoxes.size() - 1) {
                        otpBoxes.get(currentIndex + 1).requestFocus();
                    }
                }
            });

            // Bắt sự kiện bấm nút Xóa (Backspace) -> Quay lại ô trước
            otpBoxes.get(i).setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpBoxes.get(currentIndex).getText().toString().isEmpty() && currentIndex > 0) {
                        otpBoxes.get(currentIndex - 1).requestFocus();
                        otpBoxes.get(currentIndex - 1).setText("");
                        return true;
                    }
                }
                return false;
            });
        }
    }

    /**
     * Hiển thị CardView tương ứng với từng bước (1, 2 hoặc 3)
     */
    private void showStep(int step) {
        binding.includeStep1.getRoot().setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        binding.includeStep2.getRoot().setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        binding.includeStep3.getRoot().setVisibility(step == 3 ? View.VISIBLE : View.GONE);

        if (step == 2) {
            otpBoxes.get(0).requestFocus();
        }
    }

    /**
     * Lấy chuỗi OTP 6 số từ các ô nhập
     */
    private String getOtpCode() {
        StringBuilder sb = new StringBuilder();
        for (EditText editText : otpBoxes) {
            sb.append(editText.getText().toString().trim());
        }
        return sb.toString();
    }

    /**
     * Xóa nội dung các ô OTP
     */
    private void clearOtpBoxes() {
        for (EditText editText : otpBoxes) {
            editText.setText("");
        }
        if (!otpBoxes.isEmpty()) {
            otpBoxes.get(0).requestFocus();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
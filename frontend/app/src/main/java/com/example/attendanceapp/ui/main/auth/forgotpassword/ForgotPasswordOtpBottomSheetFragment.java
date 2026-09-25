package com.example.attendanceapp.ui.main.auth.forgotpassword;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.BottomSheetAuthOtpBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ForgotPasswordOtpBottomSheetFragment extends BottomSheetDialogFragment {

    public static final String TAG = "ForgotPasswordOtpBottomSheetFragment";

    private static final String ARG_PHONE = "arg_phone";
    private static final String ARG_REQUEST_KEY = "arg_request_key";
    public static final String RESULT_VERIFIED_PHONE = "verified_phone";
    private static final long RESEND_COUNTDOWN_MS = 60_000;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_COOLDOWN_MS = 60_000;

    private BottomSheetAuthOtpBinding binding;
    private ForgotPasswordViewModel viewModel;

    private String phone;
    private String requestKey;
    private TextInputLayout[] otpTilBoxes;
    private EditText[] otpBoxes;
    private CountDownTimer countDownTimer;
    private CountDownTimer lockTimer;
    private int failedAttempts = 0;
    private boolean isLocked = false;

    public static ForgotPasswordOtpBottomSheetFragment newInstance(String phone, String requestKey) {
        ForgotPasswordOtpBottomSheetFragment fragment = new ForgotPasswordOtpBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHONE, phone);
        args.putString(ARG_REQUEST_KEY, requestKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            phone = getArguments().getString(ARG_PHONE, "");
            requestKey = getArguments().getString(ARG_REQUEST_KEY, "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAuthOtpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Scope ViewModel vào ParentFragment để dùng chung ViewModel với ForgotPasswordFragment
        viewModel = new ViewModelProvider(requireParentFragment()).get(ForgotPasswordViewModel.class);

        binding.tvOtpSubtitle.setText(getString(R.string.auth_otp_subtitle_format, maskPhone(phone)));

        // Mảng TextInputLayout dùng để quản lý viền Material
        otpTilBoxes = new TextInputLayout[]{
                binding.tilOtp1, binding.tilOtp2, binding.tilOtp3,
                binding.tilOtp4, binding.tilOtp5, binding.tilOtp6
        };

        // Mảng EditText nhận phím
        otpBoxes = new EditText[]{
                binding.edtOtp1, binding.edtOtp2, binding.edtOtp3,
                binding.edtOtp4, binding.edtOtp5, binding.edtOtp6
        };

        setupOtpBoxAutoAdvance();
        setupConfirmButton();
        setupResendButton();
        observeViewModel();
        startResendCountdown();
    }

    private void observeViewModel() {
        viewModel.getForgotPasswordState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof ForgotPasswordViewModel.ForgotPasswordState.OtpVerified) {
                sendResultAndDismiss();
            } else if (state instanceof ForgotPasswordViewModel.ForgotPasswordState.OtpResent) {
                Toast.makeText(requireContext(), "Đã gửi lại mã OTP", Toast.LENGTH_SHORT).show();
                clearOtpBoxes();
                startResendCountdown();
            } else if (state instanceof ForgotPasswordViewModel.ForgotPasswordState.Error) {
                String errorMsg = ((ForgotPasswordViewModel.ForgotPasswordState.Error) state).message;
                handleOtpError(errorMsg);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.btnConfirmOtp.setEnabled(!isLoading && getOtpCode().length() == otpBoxes.length && !isLocked);
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    /** Tự động nhảy ô khi nhập số, lùi ô khi bấm xóa */
    private void setupOtpBoxAutoAdvance() {
        for (int i = 0; i < otpBoxes.length; i++) {
            final int index = i;
            EditText current = otpBoxes[i];

            current.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    // Chỉ reset style lỗi khi người dùng bắt đầu nhập kí tự mới (s.length() > 0)
                    if (s.length() > 0) {
                        resetOtpBoxStyles();
                        binding.tvOtpError.setVisibility(View.GONE);
                    }

                    if (s.length() == 1 && index < otpBoxes.length - 1) {
                        otpBoxes[index + 1].requestFocus();
                    }
                    updateConfirmButtonState();
                }
            });

            current.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && current.getText().toString().isEmpty()
                        && index > 0) {
                    otpBoxes[index - 1].requestFocus();
                    otpBoxes[index - 1].setText("");
                    return true;
                }
                return false;
            });
        }
    }

    private void updateConfirmButtonState() {
        boolean isFull = getOtpCode().length() == otpBoxes.length;
        Boolean isLoading = viewModel.getIsLoading().getValue();
        binding.btnConfirmOtp.setEnabled(isFull && (isLoading == null || !isLoading) && !isLocked);
    }

    private String getOtpCode() {
        StringBuilder sb = new StringBuilder();
        for (EditText box : otpBoxes) {
            sb.append(box.getText().toString().trim());
        }
        return sb.toString();
    }

    private void setupConfirmButton() {
        binding.btnConfirmOtp.setOnClickListener(v -> verifyOtp());
    }

    private void verifyOtp() {
        String otp = getOtpCode();
        viewModel.verifyOtp(otp);
    }

    private void handleOtpError(String message) {
        failedAttempts++;

        // 1. Rung máy (Haptic)
        triggerHapticFeedback();

        // 2. Lắc cụm ô nhập (Shake animation)
        Animation shake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake);
        binding.layoutOtpBoxes.startAnimation(shake);

        // 3. Đổi viền ô sang màu Đỏ Material
        for (TextInputLayout til : otpTilBoxes) {
            til.setErrorEnabled(true);
            til.setError(" "); // Hoặc til.setError(message) nếu cần
        }

        // 4. Báo lỗi phía dưới
        binding.tvOtpError.setText(message);
        binding.tvOtpError.setVisibility(View.VISIBLE);

        // 5. Kiểm tra giới hạn số lần thử
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            lockOtpInputs();
        } else {
            clearOtpBoxes();
        }
    }

    private void triggerHapticFeedback() {
        Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(200);
            }
        }
    }

    private void resetOtpBoxStyles() {
        for (TextInputLayout til : otpTilBoxes) {
            til.setError(null);
            til.setErrorEnabled(false);
        }
    }
    private void lockOtpInputs() {
        isLocked = true;
        setInputsEnabled(false);

        // Ẩn cụm đếm ngược gửi lại mã, hiện TextView thông báo khóa thay thế
        binding.layoutResendArea.setVisibility(View.GONE);
        binding.tvLockCountdown.setVisibility(View.VISIBLE);
        binding.tvOtpError.setVisibility(View.GONE);

        if (countDownTimer != null) countDownTimer.cancel();

        lockTimer = new CountDownTimer(LOCK_COOLDOWN_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (binding == null) return;
                long seconds = millisUntilFinished / 1000;
                binding.tvLockCountdown.setText(getString(R.string.auth_otp_locked_format, seconds));
            }

            @Override
            public void onFinish() {
                if (binding == null) return;
                unlockOtpInputs();
            }
        }.start();
    }

    private void unlockOtpInputs() {
        isLocked = false;
        failedAttempts = 0;
        setInputsEnabled(true);
        binding.tvOtpError.setVisibility(View.GONE);
        clearOtpBoxes();

        // Ẩn TextView thông báo khóa, hiện lại cụm gửi lại mã
        binding.tvLockCountdown.setVisibility(View.GONE);
        binding.layoutResendArea.setVisibility(View.VISIBLE);

        binding.tvOtpCountdown.setVisibility(View.GONE);
        binding.tvResendOtp.setVisibility(View.VISIBLE);
    }

    private void setInputsEnabled(boolean enabled) {
        for (EditText box : otpBoxes) {
            box.setEnabled(enabled);
        }
        binding.btnConfirmOtp.setEnabled(enabled && getOtpCode().length() == otpBoxes.length);
    }

    private void clearOtpBoxes() {
        for (EditText box : otpBoxes) {
            box.setText("");
        }
        otpBoxes[0].requestFocus();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(otpBoxes[0], InputMethodManager.SHOW_IMPLICIT);
        }
        binding.btnConfirmOtp.setEnabled(false);
    }

    private void sendResultAndDismiss() {
        Bundle result = new Bundle();
        result.putString(RESULT_VERIFIED_PHONE, phone);
        getParentFragmentManager().setFragmentResult(requestKey, result);
        dismiss();
    }

    private void setupResendButton() {
        binding.tvResendOtp.setOnClickListener(v -> viewModel.resendOtp());
    }

    private void startResendCountdown() {
        binding.tvOtpCountdown.setVisibility(View.VISIBLE);
        binding.tvResendOtp.setVisibility(View.GONE);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(RESEND_COUNTDOWN_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (binding == null) return;
                long seconds = millisUntilFinished / 1000;
                binding.tvOtpCountdown.setText(
                        getString(R.string.auth_resend_otp_countdown_format, seconds)
                );
            }

            @Override
            public void onFinish() {
                if (binding == null) return;
                binding.tvOtpCountdown.setVisibility(View.GONE);
                binding.tvResendOtp.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 4) + "***" + phone.substring(phone.length() - 3);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (lockTimer != null) {
            lockTimer.cancel();
        }
        binding = null;
    }
}
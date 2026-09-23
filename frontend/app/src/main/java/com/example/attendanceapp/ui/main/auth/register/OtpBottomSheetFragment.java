package com.example.attendanceapp.ui.main.auth.register;

import android.os.Bundle;
import android.os.CountDownTimer;
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
import androidx.lifecycle.ViewModelProvider;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.BottomSheetOtpBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OtpBottomSheetFragment extends BottomSheetDialogFragment {

    public static final String TAG = "OtpBottomSheetFragment";

    private static final String ARG_PHONE = "arg_phone";
    private static final String ARG_REQUEST_KEY = "arg_request_key";
    public static final String RESULT_VERIFIED_PHONE = "verified_phone";
    private static final long RESEND_COUNTDOWN_MS = 60_000;

    private BottomSheetOtpBinding binding;
    private RegisterViewModel viewModel;

    private String phone;
    private String requestKey;
    private EditText[] otpBoxes;
    private CountDownTimer countDownTimer;

    public static OtpBottomSheetFragment newInstance(String phone, String requestKey) {
        OtpBottomSheetFragment fragment = new OtpBottomSheetFragment();
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
        binding = BottomSheetOtpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Scope ViewModel vào ParentFragment để dùng chung ViewModel với RegisterFragment
        viewModel = new ViewModelProvider(requireParentFragment()).get(RegisterViewModel.class);

        binding.tvOtpSubtitle.setText(getString(R.string.auth_otp_subtitle_format, maskPhone(phone)));

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
        // Observe trạng thái OTP từ ViewModel
        viewModel.getOtpState().observe(getViewLifecycleOwner(), otpState -> {
            if (otpState instanceof RegisterViewModel.OtpState.Verified) {
                // Xác thực thành công -> Trả kết quả về RegisterFragment và đóng BottomSheet
                sendResultAndDismiss();
            } else if (otpState instanceof RegisterViewModel.OtpState.Error) {
                // Thất bại -> Hiển thị thông báo lỗi
                String errorMsg = ((RegisterViewModel.OtpState.Error) otpState).message;
                binding.tvOtpError.setText(errorMsg);
                binding.tvOtpError.setVisibility(View.VISIBLE);
                clearOtpBoxes();
            }
        });

        // Observe trạng thái Loading
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.btnConfirmOtp.setEnabled(!isLoading && getOtpCode().length() == otpBoxes.length);
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    /** Tự nhảy sang ô kế tiếp khi gõ xong 1 số, lùi lại khi backspace ở ô rỗng */
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
                    binding.tvOtpError.setVisibility(View.GONE);
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
        binding.btnConfirmOtp.setEnabled(isFull && (isLoading == null || !isLoading));
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
        // Gọi hàm Asynchronous trên ViewModel thay vì verifyOtpSync
        viewModel.verifyOtp(phone, otp);
    }

    private void clearOtpBoxes() {
        for (EditText box : otpBoxes) {
            box.setText("");
        }
        otpBoxes[0].requestFocus();
        binding.btnConfirmOtp.setEnabled(false);
    }

    private void sendResultAndDismiss() {
        Bundle result = new Bundle();
        result.putString(RESULT_VERIFIED_PHONE, phone);
        getParentFragmentManager().setFragmentResult(requestKey, result);
        dismiss();
    }

    private void setupResendButton() {
        binding.tvResendOtp.setOnClickListener(v -> {
            viewModel.checkPhoneAndSendOtp(phone);
            clearOtpBoxes();
            startResendCountdown();
            Toast.makeText(requireContext(), "Đã gửi lại mã OTP", Toast.LENGTH_SHORT).show();
        });
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
        binding = null;
    }
}
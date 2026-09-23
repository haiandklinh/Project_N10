package com.example.attendanceapp.ui.main.auth.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.attendanceapp.databinding.BottomSheetInvitationCodeBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EnterInvitationCodeBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "EnterInvitationCodeBottomSheet";
    public static final String REQUEST_KEY = "invitation_code_request_key";
    public static final String RESULT_ACTION = "result_action"; // SUBMITTED / CANCELED
    public static final String ACTION_SUBMITTED = "action_submitted";
    public static final String ACTION_CANCELED = "action_canceled";
    public static final String BUNDLE_KEY_CODE = "bundle_key_code";

    private BottomSheetInvitationCodeBinding binding;

    public static EnterInvitationCodeBottomSheet newInstance() {
        return new EnterInvitationCodeBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetInvitationCodeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
    }

    private void setupViews() {
        binding.btnSubmit.setOnClickListener(v -> {
            String code = binding.edtInvitationCode.getText().toString().trim();

            if (TextUtils.isEmpty(code)) {
                binding.tilInvitationCode.setError("Vui lòng nhập mã mời");
                return;
            }

            if (code.length() < 6) {
                binding.tilInvitationCode.setError("Mã mời phải có ít nhất 6 ký tự");
                return;
            }

            binding.tilInvitationCode.setError(null);

            // Gửi kết quả về Fragment cha qua FragmentResultListener
            Bundle result = new Bundle();
            result.putString(RESULT_ACTION, ACTION_SUBMITTED);
            result.putString(BUNDLE_KEY_CODE, code);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);

            dismiss();
        });

        binding.btnCancel.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putString(RESULT_ACTION, ACTION_CANCELED);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);

            dismiss();
        });

        binding.edtInvitationCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilInvitationCode.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
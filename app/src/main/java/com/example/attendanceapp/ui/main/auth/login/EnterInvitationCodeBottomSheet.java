package com.example.attendanceapp.ui.main.auth.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.attendanceapp.databinding.BottomSheetInvitationCodeBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class EnterInvitationCodeBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetInvitationCodeBinding binding;
    private OnInvitationCodeListener listener;

    public interface OnInvitationCodeListener {
        void onInvitationCodeSubmitted(String code);
        void onCanceled();
    }

    public static EnterInvitationCodeBottomSheet newInstance(OnInvitationCodeListener listener) {
        EnterInvitationCodeBottomSheet bottomSheet = new EnterInvitationCodeBottomSheet();
        bottomSheet.setListener(listener);
        return bottomSheet;
    }

    public void setListener(OnInvitationCodeListener listener) {
        this.listener = listener;
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
            
            if (listener != null) {
                listener.onInvitationCodeSubmitted(code);
                dismiss();
            }
        });

        binding.btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCanceled();
            }
            dismiss();
        });

        binding.edtInvitationCode.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilInvitationCode.setError(null);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

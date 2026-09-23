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

import com.example.attendanceapp.databinding.BottomSheetCompanyNameBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CompanyNameBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "CompanyNameBottomSheet";
    public static final String REQUEST_KEY = "company_name_request_key";
    public static final String RESULT_ACTION = "result_action"; // SUBMITTED / CANCELED
    public static final String ACTION_SUBMITTED = "action_submitted";
    public static final String ACTION_CANCELED = "action_canceled";
    public static final String BUNDLE_KEY_COMPANY_NAME = "bundle_key_company_name";

    private BottomSheetCompanyNameBinding binding;

    public static CompanyNameBottomSheet newInstance() {
        return new CompanyNameBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetCompanyNameBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
    }

    private void setupViews() {
        binding.btnSubmit.setOnClickListener(v -> {
            String companyName = binding.edtCompanyName.getText().toString().trim();

            if (TextUtils.isEmpty(companyName)) {
                binding.tilCompanyName.setError("Vui lòng nhập tên doanh nghiệp");
                return;
            }

            if (companyName.length() < 3) {
                binding.tilCompanyName.setError("Tên doanh nghiệp phải có ít nhất 3 ký tự");
                return;
            }

            binding.tilCompanyName.setError(null);

            // Gửi kết quả về Fragment cha
            Bundle result = new Bundle();
            result.putString(RESULT_ACTION, ACTION_SUBMITTED);
            result.putString(BUNDLE_KEY_COMPANY_NAME, companyName);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);

            dismiss();
        });

        binding.btnCancel.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putString(RESULT_ACTION, ACTION_CANCELED);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);

            dismiss();
        });

        binding.edtCompanyName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilCompanyName.setError(null);
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
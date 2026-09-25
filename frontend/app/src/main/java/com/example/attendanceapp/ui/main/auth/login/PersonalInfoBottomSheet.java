package com.example.attendanceapp.ui.main.auth.login;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.BottomSheetAuthPersonalInfoBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PersonalInfoBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "PersonalInfoBottomSheet";
    public static final String REQUEST_KEY = "personal_info_request_key";
    public static final String RESULT_ACTION = "result_action"; // SUBMITTED / CANCELED
    public static final String ACTION_SUBMITTED = "action_submitted";
    public static final String ACTION_CANCELED = "action_canceled";
    
    public static final String BUNDLE_KEY_NAME = "bundle_key_name";
    public static final String BUNDLE_KEY_DOB = "bundle_key_dob";
    public static final String BUNDLE_KEY_GENDER = "bundle_key_gender";

    private BottomSheetAuthPersonalInfoBinding binding;
    private final Calendar calendar = Calendar.getInstance();

    public static PersonalInfoBottomSheet newInstance() {
        return new PersonalInfoBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAuthPersonalInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
    }

    private void setupViews() {
        binding.edtDob.setOnClickListener(v -> showDatePicker());

        binding.btnSubmit.setOnClickListener(v -> {
            String name = binding.edtFullName.getText().toString().trim();
            String dob = binding.edtDob.getText().toString().trim();
            
            int checkedId = binding.rgGender.getCheckedRadioButtonId();
            String gender = "";
            if (checkedId == R.id.rbMale) {
                gender = "Male";
            } else if (checkedId == R.id.rbFemale) {
                gender = "Female";
            } else if (checkedId == R.id.rbOther) {
                gender = "Other";
            }

            boolean isValid = true;

            if (TextUtils.isEmpty(name)) {
                binding.tilFullName.setError("Vui lòng nhập họ và tên");
                isValid = false;
            } else if (name.length() < 2) {
                binding.tilFullName.setError("Họ và tên quá ngắn");
                isValid = false;
            }

            if (TextUtils.isEmpty(dob)) {
                binding.tilDob.setError("Vui lòng chọn ngày sinh");
                isValid = false;
            }

            if (TextUtils.isEmpty(gender)) {
                isValid = false;
            }

            if (!isValid) return;

            Bundle result = new Bundle();
            result.putString(RESULT_ACTION, ACTION_SUBMITTED);
            result.putString(BUNDLE_KEY_NAME, name);
            result.putString(BUNDLE_KEY_DOB, dob);
            result.putString(BUNDLE_KEY_GENDER, gender);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);

            dismiss();
        });

        binding.btnCancel.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putString(RESULT_ACTION, ACTION_CANCELED);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);

            dismiss();
        });

        binding.edtFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilFullName.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            
            String format = String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, month + 1, year);
            binding.edtDob.setText(format);
            binding.tilDob.setError(null);
        };

        new DatePickerDialog(requireContext(), dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

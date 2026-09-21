package com.example.attendanceapp.ui.employee.schedule.leave;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.databinding.FragmentLeaveRequestBinding;
import com.example.attendanceapp.domain.model.LeaveRequest;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LeaveRequestFragment extends Fragment {

    private FragmentLeaveRequestBinding binding;
    private final String[] requestTypes = {"Xin nghỉ phép", "Đổi ca / Đổi lịch", "Làm ngoài giờ (OT)", "Đi muộn", "Về sớm"};
    private final String[] availableShifts = {"Ca sáng (08:00 - 12:00)", "Ca chiều (13:30 - 17:30)", "Ca hành chính (08:00 - 17:30)", "Ca tối (18:00 - 22:00)"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLeaveRequestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupDropdowns();
        setupDatePicker();
        setupInputWatchers();
        readArgumentsBundle(); // Nhận dữ liệu truyền từ ScheduleFragment
        setupSubmitButton();
    }

    private void setupToolbar() {
        binding.toolbarApp.setTitle("Tạo đơn đề nghị");
        binding.toolbarApp.setBackButtonVisible(true);
        binding.toolbarApp.setOnBackClickListener(() ->
                Navigation.findNavController(requireView()).navigateUp()
        );
    }

    private void setupDropdowns() {
        // 1. Dropdown Loại đơn
        ArrayAdapter<String> adapterType = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                requestTypes
        );
        binding.actRequestType.setAdapter(adapterType);

        binding.actRequestType.setOnItemClickListener((parent, view, position, id) -> {
            String selectedType = requestTypes[position];
            if ("Đổi ca / Đổi lịch".equals(selectedType)) {
                binding.tilShiftInfo.setVisibility(View.VISIBLE);
            } else {
                binding.tilShiftInfo.setVisibility(View.GONE);
                binding.actShiftInfo.setText("", false);
            }
        });

        // 2. Dropdown Ca làm việc (Khớp với actShiftInfo trong XML mới)
        ArrayAdapter<String> adapterShift = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                availableShifts
        );
        binding.actShiftInfo.setAdapter(adapterShift);
    }

    private void readArgumentsBundle() {
        if (getArguments() != null) {
            String selectedDate = getArguments().getString("selectedDate");
            String defaultType = getArguments().getString("defaultType");

            if (!TextUtils.isEmpty(selectedDate)) {
                binding.etDate.setText(selectedDate);
            }

            if (!TextUtils.isEmpty(defaultType)) {
                binding.actRequestType.setText(defaultType, false);
                if ("Đổi ca / Đổi lịch".equals(defaultType)) {
                    binding.tilShiftInfo.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void setupDatePicker() {
        binding.etDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày áp dụng")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                binding.etDate.setText(sdf.format(new Date(selection)));
                binding.tilDate.setError(null);
            });

            datePicker.show(getChildFragmentManager(), "DATE_PICKER");
        });
    }

    private void setupInputWatchers() {
        // Xóa thông báo lỗi khi người dùng bắt đầu gõ
        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilRequestType.setError(null);
                binding.tilDate.setError(null);
                binding.tilShiftInfo.setError(null);
                binding.tilReason.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.actRequestType.addTextChangedListener(clearErrorWatcher);
        binding.etDate.addTextChangedListener(clearErrorWatcher);
        binding.actShiftInfo.addTextChangedListener(clearErrorWatcher);
        binding.etReason.addTextChangedListener(clearErrorWatcher);
    }

    private void setupSubmitButton() {
        binding.btnSubmit.setOnClickListener(v -> {
            String type = binding.actRequestType.getText().toString().trim();
            String date = binding.etDate.getText().toString().trim();
            String shiftInfo = binding.actShiftInfo.getText().toString().trim();
            String reason = binding.etReason.getText() != null ? binding.etReason.getText().toString().trim() : "";
            boolean isEmergency = binding.cbIsEmergency.isChecked();

            // Validate dữ liệu
            if (TextUtils.isEmpty(type)) {
                binding.tilRequestType.setError("Vui lòng chọn loại đơn từ");
                return;
            }

            if (TextUtils.isEmpty(date)) {
                binding.tilDate.setError("Vui lòng chọn ngày áp dụng");
                return;
            }

            if (binding.tilShiftInfo.getVisibility() == View.VISIBLE && TextUtils.isEmpty(shiftInfo)) {
                binding.tilShiftInfo.setError("Vui lòng chọn ca cần đổi");
                return;
            }

            if (TextUtils.isEmpty(reason)) {
                binding.tilReason.setError("Vui lòng nhập lý do cụ thể");
                return;
            }

            sendRequest(type, date, shiftInfo, reason, isEmergency);
        });
    }

    private void sendRequest(String type, String date, String shiftInfo, String reason, boolean isEmergency) {
        LeaveRequest request = new LeaveRequest();
        request.setId(String.valueOf(System.currentTimeMillis()));
        request.setType(type);
        request.setTargetDate(date);
        request.setShiftId(shiftInfo);
        request.setReason(reason);
        request.setEmergency(isEmergency);
        request.setStatus("PENDING");
        request.setCreatedAt(System.currentTimeMillis());

        // TODO: Gọi ViewModel gửi dữ liệu lên Room DB / API Server ở đây

        Toast.makeText(requireContext(), "Gửi đơn " + type + " thành công!", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
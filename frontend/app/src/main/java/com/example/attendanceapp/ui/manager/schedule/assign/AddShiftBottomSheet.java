package com.example.attendanceapp.ui.manager.schedule.assign;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.attendanceapp.core.utils.DateTimeFormatUtils;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.databinding.BottomSheetAddShiftBinding;
import com.example.attendanceapp.domain.model.ShiftOption;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddShiftBottomSheet extends BottomSheetDialogFragment {

    public static final String REQUEST_KEY = "AddShiftBottomSheet_RequestKey";
    public static final String EXTRA_RESULT_SHIFT = "extra_result_shift";
    private static final String ARG_SHIFT_ITEM = "arg_shift_item";

    @Inject
    SharedPreferenceApp sharedPreferenceApp;

    private BottomSheetAddShiftBinding binding;

    private LocalTime startTime = LocalTime.of(8, 0);
    private LocalTime endTime = LocalTime.of(12, 0);
    private Double latitude = null;
    private Double longitude = null;
    private ShiftOption editingShift = null;

    public static AddShiftBottomSheet newInstance() {
        return new AddShiftBottomSheet();
    }

    public static AddShiftBottomSheet newInstanceForEdit(ShiftOption shift) {
        AddShiftBottomSheet fragment = new AddShiftBottomSheet();
        Bundle args = new Bundle();
        args.putParcelable(ARG_SHIFT_ITEM, shift);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAddShiftBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Kiểm tra dữ liệu truyền vào
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_SHIFT_ITEM)) {
            editingShift = args.getParcelable(ARG_SHIFT_ITEM);
        }

        if (editingShift != null) {
            binding.tvTitle.setText("Chỉnh sửa ca làm việc");
            binding.edtShiftName.setText(editingShift.getTitle());
            latitude = editingShift.getLatitude();
            longitude = editingShift.getLongitude();

            parseTimes(editingShift.getStartTime(), editingShift.getEndTime());
            binding.btnSaveShift.setText("Cập nhật ca làm");
        } else {
            binding.tvTitle.setText("Thêm ca làm việc mới");
            // Tự động lấy vị trí công ty đã lưu từ SharedPreferences
            latitude = sharedPreferenceApp.getCompanyLat();
            longitude = sharedPreferenceApp.getCompanyLon();
        }

        updateTimeText(binding.edtStartTime, startTime);
        updateTimeText(binding.edtEndTime, endTime);

        // Xóa thông báo lỗi Tên ca khi người dùng bắt đầu nhập
        binding.edtShiftName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilShiftName.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 2. Lắng nghe chọn giờ bắt đầu
        View.OnClickListener startPicker = v -> showTimePicker(startTime, selectedTime -> {
            startTime = selectedTime;
            updateTimeText(binding.edtStartTime, startTime);
            binding.tilEndTime.setError(null);
        });
        binding.edtStartTime.setOnClickListener(startPicker);
        binding.tilStartTime.setEndIconOnClickListener(startPicker);

        // 3. Lắng nghe chọn giờ kết thúc
        View.OnClickListener endPicker = v -> showTimePicker(endTime, selectedTime -> {
            endTime = selectedTime;
            updateTimeText(binding.edtEndTime, endTime);
            binding.tilEndTime.setError(null);
        });
        binding.edtEndTime.setOnClickListener(endPicker);
        binding.tilEndTime.setEndIconOnClickListener(endPicker);

        // 4. Nút Lưu / Cập nhật
        binding.btnSaveShift.setOnClickListener(v -> handleSave());
    }

    private void handleSave() {
        String name = binding.edtShiftName.getText() != null ? binding.edtShiftName.getText().toString().trim() : "";
        if (TextUtils.isEmpty(name)) {
            binding.tilShiftName.setError("Vui lòng nhập tên ca!");
            return;
        }

        if (!endTime.isAfter(startTime)) {
            binding.tilEndTime.setError("Giờ kết thúc phải lớn hơn giờ bắt đầu!");
            return;
        }

        // Kiểm tra vị trí đã được lấy từ SharedPreferences chưa
        if (latitude == null || longitude == null || (latitude == 0.0 && longitude == 0.0)) {
            Toast.makeText(requireContext(), "Lỗi: Không tìm thấy vị trí công ty. Vui lòng thiết lập trong cài đặt.", Toast.LENGTH_LONG).show();
            return;
        }

        String startTimeStr = DateTimeFormatUtils.formatTime(startTime);
        String endTimeStr = DateTimeFormatUtils.formatTime(endTime);

        // QUAN TRỌNG: Để id = null khi tạo mới để các Fragment phân biệt là lệnh THÊM MỚI (addShift)
        String id = editingShift != null ? editingShift.getId() : null;
        ShiftOption resultShift = new ShiftOption(id, name, startTimeStr, endTimeStr, latitude, longitude);

        Bundle result = new Bundle();
        result.putParcelable(EXTRA_RESULT_SHIFT, resultShift);

        // Trả kết quả về cho Fragment/Dialog cha
        getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);

        dismiss();
    }

    private void parseTimes(String startTimeStr, String endTimeStr) {
        if (!TextUtils.isEmpty(startTimeStr)) {
            try {
                startTime = LocalTime.parse(startTimeStr);
            } catch (DateTimeParseException ignored) {}
        }
        if (!TextUtils.isEmpty(endTimeStr)) {
            try {
                endTime = LocalTime.parse(endTimeStr);
            } catch (DateTimeParseException ignored) {}
        }
    }

    private interface TimePickerCallback {
        void onTimeSelected(LocalTime time);
    }

    private void showTimePicker(LocalTime currentTime, TimePickerCallback callback) {
        new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> callback.onTimeSelected(LocalTime.of(hourOfDay, minute)),
                currentTime.getHour(),
                currentTime.getMinute(),
                true
        ).show();
    }

    private void updateTimeText(android.widget.EditText editText, LocalTime time) {
        editText.setText(DateTimeFormatUtils.formatTime(time));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

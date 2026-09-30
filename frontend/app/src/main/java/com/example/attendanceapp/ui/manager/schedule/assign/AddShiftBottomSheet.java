package com.example.attendanceapp.ui.manager.schedule.assign;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.attendanceapp.databinding.BottomSheetAddShiftBinding;
import com.example.attendanceapp.domain.model.ShiftOption;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Locale;

public class AddShiftBottomSheet extends BottomSheetDialogFragment {

    public interface OnShiftSubmitListener {
        void onSubmit(String shiftName, String startTime, String endTime, String description);
    }

    private BottomSheetAddShiftBinding binding;
    private OnShiftSubmitListener listener;
    private ShiftOption editingShift;

    private int startHour = 8, startMinute = 0;
    private int endHour = 12, endMinute = 0;

    /**
     * Khởi tạo cho trường hợp THÊM MỚI ca làm
     */
    public static AddShiftBottomSheet newInstance(OnShiftSubmitListener listener) {
        AddShiftBottomSheet fragment = new AddShiftBottomSheet();
        fragment.listener = listener;
        return fragment;
    }

    /**
     * Khởi tạo cho trường hợp CHỈNH SỬA ca làm hiện có
     */
    public static AddShiftBottomSheet newInstanceForEdit(ShiftOption shift, OnShiftSubmitListener listener) {
        AddShiftBottomSheet fragment = new AddShiftBottomSheet();
        fragment.editingShift = shift;
        fragment.listener = listener;
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

        // 1. Populate dữ liệu nếu ở chế độ Chỉnh sửa (Edit Mode)
        if (editingShift != null) {
            binding.edtShiftName.setText(editingShift.getTitle());
            parseAndSetTimes(editingShift.getStartTime(), editingShift.getEndTime());
            binding.btnSaveShift.setText("Cập nhật ca làm");
        } else {
            updateTimeText(binding.edtStartTime, startHour, startMinute);
            updateTimeText(binding.edtEndTime, endHour, endMinute);
        }

        // 2. Lắng nghe sự kiện chọn giờ bắt đầu
        binding.edtStartTime.setOnClickListener(v -> showTimePicker(startHour, startMinute, (h, m) -> {
            startHour = h;
            startMinute = m;
            updateTimeText(binding.edtStartTime, startHour, startMinute);
        }));

        // 3. Lắng nghe sự kiện chọn giờ kết thúc
        binding.edtEndTime.setOnClickListener(v -> showTimePicker(endHour, endMinute, (h, m) -> {
            endHour = h;
            endMinute = m;
            updateTimeText(binding.edtEndTime, endHour, endMinute);
        }));

        // 4. Nút Lưu / Cập nhật
        binding.btnSaveShift.setOnClickListener(v -> {
            String name = binding.edtShiftName.getText() != null ? binding.edtShiftName.getText().toString().trim() : "";
            if (TextUtils.isEmpty(name)) {
                binding.tilShiftName.setError("Vui lòng nhập tên ca!");
                return;
            }
            binding.tilShiftName.setError(null);

            String startTime = String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute);
            String endTime = String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute);

            String description = "";
            if (binding.edtDescription != null && binding.edtDescription.getText() != null) {
                description = binding.edtDescription.getText().toString().trim();
            }

            if (listener != null) {
                listener.onSubmit(name, startTime, endTime, description);
            }
            dismiss();
        });
    }

    private void parseAndSetTimes(String startTimeStr, String endTimeStr) {
        if (!TextUtils.isEmpty(startTimeStr) && startTimeStr.contains(":")) {
            String[] startParts = startTimeStr.split(":");
            try {
                startHour = Integer.parseInt(startParts[0]);
                startMinute = Integer.parseInt(startParts[1]);
            } catch (NumberFormatException ignored) {}
        }

        if (!TextUtils.isEmpty(endTimeStr) && endTimeStr.contains(":")) {
            String[] endParts = endTimeStr.split(":");
            try {
                endHour = Integer.parseInt(endParts[0]);
                endMinute = Integer.parseInt(endParts[1]);
            } catch (NumberFormatException ignored) {}
        }

        updateTimeText(binding.edtStartTime, startHour, startMinute);
        updateTimeText(binding.edtEndTime, endHour, endMinute);
    }

    private interface TimePickerCallback {
        void onTimeSelected(int hour, int minute);
    }

    private void showTimePicker(int currentHour, int currentMinute, TimePickerCallback callback) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> callback.onTimeSelected(hourOfDay, minute),
                currentHour,
                currentMinute,
                true
        );
        timePickerDialog.show();
    }

    private void updateTimeText(android.widget.EditText editText, int hour, int minute) {
        editText.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
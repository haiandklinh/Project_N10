package com.example.attendanceapp.ui.manager.schedule.assign;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.databinding.BottomSheetAssignShiftBinding;
import com.example.attendanceapp.domain.model.ShiftOption;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * BottomSheet xếp ca nhanh cho 1 nhân viên vào 1 ngày cụ thể.
 * Dữ liệu ca làm việc (List<ShiftOption>) được truyền động từ ViewModel/Repository.
 */
public class AssignShiftBottomSheet extends BottomSheetDialogFragment {

    public interface OnShiftAssignedListener {
        /**
         * shift == null nghĩa là chọn "Không xếp ca" (Cho nghỉ)
         */
        void onShiftAssigned(@Nullable ShiftOption shift);
    }

    private static final String ARG_EMPLOYEE_NAME = "employee_name";
    private static final String ARG_DATE_EPOCH_DAY = "date_epoch_day";

    private BottomSheetAssignShiftBinding binding;
    private OnShiftAssignedListener listener;

    private final List<ShiftOption> availableShifts = new ArrayList<>();
    private ShiftOption selectedShift;

    public static AssignShiftBottomSheet newInstance(
            String employeeName,
            LocalDate date,
            List<ShiftOption> shifts,
            OnShiftAssignedListener listener
    ) {
        AssignShiftBottomSheet sheet = new AssignShiftBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_EMPLOYEE_NAME, employeeName);
        args.putLong(ARG_DATE_EPOCH_DAY, date.toEpochDay());
        sheet.setArguments(args);

        if (shifts != null) {
            sheet.availableShifts.addAll(shifts);
        }
        sheet.listener = listener;
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAssignShiftBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String employeeName = args != null ? args.getString(ARG_EMPLOYEE_NAME, "") : "";
        LocalDate date = args != null ? LocalDate.ofEpochDay(args.getLong(ARG_DATE_EPOCH_DAY)) : LocalDate.now();

        binding.tvTitle.setText("Xếp ca cho " + employeeName);
        binding.tvSubtitle.setText(formatVietnameseDate(date));

        // Kiểm tra xem hệ thống đã có danh sách ca làm việc chưa
        if (availableShifts.isEmpty()) {
            showEmptyState();
        } else {
            setupShiftRecyclerView();
        }

        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnConfirm.setOnClickListener(v -> {
            if (listener != null) {
                listener.onShiftAssigned(selectedShift);
            }
            dismiss();
        });
    }

    private void showEmptyState() {
        binding.rvShifts.setVisibility(View.GONE);
        binding.layoutEmptyState.getRoot().setVisibility(View.VISIBLE);
        binding.btnConfirm.setEnabled(false);
    }

    private void setupShiftRecyclerView() {
        binding.layoutEmptyState.getRoot().setVisibility(View.GONE);
        binding.rvShifts.setVisibility(View.VISIBLE);

        // Tạo danh sách bao gồm các Ca khả dụng + 1 tùy chọn "Không xếp ca"
        QuickShiftAdapter adapter = new QuickShiftAdapter(availableShifts, selected -> {
            this.selectedShift = selected;
            binding.btnConfirm.setEnabled(true);
        });

        binding.rvShifts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvShifts.setAdapter(adapter);
    }

    private String formatVietnameseDate(LocalDate date) {
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("vi"));
        String capitalized = dayOfWeek.substring(0, 1).toUpperCase(Locale.ROOT) + dayOfWeek.substring(1);
        return capitalized + ", " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.example.attendanceapp.ui.manager.schedule;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.attendanceapp.databinding.BottomSheetAssignShiftBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;

import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Bottom sheet cho phép Quản lý chọn một ca làm cố định (Sáng/Chiều/Tối) hoặc
 * bỏ xếp ca cho nhân viên vào ngày đã chọn. Xem use case "Xếp ca".
 */
public class AssignShiftBottomSheet extends BottomSheetDialogFragment {

    public interface OnShiftAssignedListener {
        /** shiftName == null nghĩa là "Không xếp ca". */
        void onShiftAssigned(@Nullable String shiftName, @Nullable String shiftTime);
    }

    private static final String ARG_EMPLOYEE_NAME = "employee_name";
    private static final String ARG_DATE_EPOCH_DAY = "date_epoch_day";

    private BottomSheetAssignShiftBinding binding;
    private OnShiftAssignedListener listener;

    private MaterialCardView selectedCard;
    private String selectedShiftName;
    private String selectedShiftTime;

    public static AssignShiftBottomSheet newInstance(String employeeName, LocalDate date, OnShiftAssignedListener listener) {
        AssignShiftBottomSheet sheet = new AssignShiftBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_EMPLOYEE_NAME, employeeName);
        args.putLong(ARG_DATE_EPOCH_DAY, date.toEpochDay());
        sheet.setArguments(args);
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

        setupShiftRow(binding.rowMorning, "Ca Sáng", "08:00 - 12:00");
        setupShiftRow(binding.rowAfternoon, "Ca Chiều", "13:00 - 17:00");
        setupShiftRow(binding.rowNight, "Ca Tối", "18:00 - 22:00");
        setupShiftRow(binding.rowNone, "Không xếp ca", "Nhân viên nghỉ ngày này");
        binding.rowNone.ivIcon.setImageResource(com.example.attendanceapp.R.drawable.ic_close);

        binding.cardShiftMorning.setOnClickListener(v -> select(binding.cardShiftMorning, "Ca Sáng", "08:00 - 12:00"));
        binding.cardShiftAfternoon.setOnClickListener(v -> select(binding.cardShiftAfternoon, "Ca Chiều", "13:00 - 17:00"));
        binding.cardShiftNight.setOnClickListener(v -> select(binding.cardShiftNight, "Ca Tối", "18:00 - 22:00"));
        binding.cardShiftNone.setOnClickListener(v -> select(binding.cardShiftNone, null, null));

        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnConfirm.setOnClickListener(v -> {
            if (listener != null) listener.onShiftAssigned(selectedShiftName, selectedShiftTime);
            dismiss();
        });
    }

    private void setupShiftRow(com.example.attendanceapp.databinding.LayoutShiftOptionRowBinding row, String name, String time) {
        row.tvShiftName.setText(name);
        row.tvShiftTime.setText(time);
    }

    private void select(MaterialCardView card, @Nullable String shiftName, @Nullable String shiftTime) {
        // Reset trạng thái tick của lựa chọn trước đó
        binding.rowMorning.ivCheck.setVisibility(View.INVISIBLE);
        binding.rowAfternoon.ivCheck.setVisibility(View.INVISIBLE);
        binding.rowNight.ivCheck.setVisibility(View.INVISIBLE);
        binding.rowNone.ivCheck.setVisibility(View.INVISIBLE);
        binding.cardShiftMorning.setStrokeColor(0x00000000);
        binding.cardShiftAfternoon.setStrokeColor(0x00000000);
        binding.cardShiftNight.setStrokeColor(0x00000000);
        binding.cardShiftNone.setStrokeColor(0x00000000);

        selectedCard = card;
        selectedShiftName = shiftName;
        selectedShiftTime = shiftTime;

        int primary = com.example.attendanceapp.core.utils.ColorUtil.getThemeColor(requireContext(), com.example.attendanceapp.R.attr.colorPrimary);
        card.setStrokeColor(primary);

        if (card == binding.cardShiftMorning) binding.rowMorning.ivCheck.setVisibility(View.VISIBLE);
        else if (card == binding.cardShiftAfternoon) binding.rowAfternoon.ivCheck.setVisibility(View.VISIBLE);
        else if (card == binding.cardShiftNight) binding.rowNight.ivCheck.setVisibility(View.VISIBLE);
        else if (card == binding.cardShiftNone) binding.rowNone.ivCheck.setVisibility(View.VISIBLE);
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

package com.example.attendanceapp.ui.manager.schedule.assign;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
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

import javax.inject.Inject;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * BottomSheet xếp ca nhanh cho 1 nhân viên vào 1 ngày cụ thể.
 * Hỗ trợ lưu trữ state an toàn qua Bundle & FragmentResult API.
 */
@AndroidEntryPoint
public class AssignShiftBottomSheet extends BottomSheetDialogFragment {

    // Keys dùng cho FragmentResult API
    public static final String REQUEST_KEY = "AssignShiftBottomSheet_RequestKey";
    public static final String EXTRA_SELECTED_SHIFT = "extra_selected_shift";

    public interface OnShiftAssignedListener {
        /**
         * shift == null nghĩa là chọn "Không xếp ca" (Cho nghỉ)
         */
        void onShiftAssigned(@Nullable ShiftOption shift);
    }

    private static final String ARG_EMPLOYEE_NAME = "employee_name";
    private static final String ARG_DATE_STR = "date_str";
    private static final String ARG_SHIFTS_LIST = "shifts_list";

    @Inject
    SharedPreferenceApp sharedPreferenceApp;

    private BottomSheetAssignShiftBinding binding;
    private OnShiftAssignedListener listener;
    private AssignShiftViewModel assignViewModel;

    private final ArrayList<ShiftOption> availableShifts = new ArrayList<>();
    private ShiftOption selectedShift;

    public static AssignShiftBottomSheet newInstance(
            String employeeName,
            LocalDate date,
            List<ShiftOption> shifts
    ) {
        AssignShiftBottomSheet sheet = new AssignShiftBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_EMPLOYEE_NAME, employeeName);
        args.putString(ARG_DATE_STR, date != null ? date.toString() : LocalDate.now().toString());
        if (shifts != null) {
            args.putParcelableArrayList(ARG_SHIFTS_LIST, new ArrayList<>(shifts));
        }
        sheet.setArguments(args);
        return sheet;
    }

    public static AssignShiftBottomSheet newInstance(
            String employeeName,
            LocalDate date,
            List<ShiftOption> shifts,
            OnShiftAssignedListener listener
    ) {
        AssignShiftBottomSheet sheet = newInstance(employeeName, date, shifts);
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

        // Lấy AssignShiftViewModel ở Activity Scope
        assignViewModel = new ViewModelProvider(requireActivity()).get(AssignShiftViewModel.class);

        Bundle args = getArguments();
        String employeeName = "";
        LocalDate date = LocalDate.now();

        if (args != null) {
            employeeName = args.getString(ARG_EMPLOYEE_NAME, "");

            String dateStr = args.getString(ARG_DATE_STR);
            if (!TextUtils.isEmpty(dateStr)) {
                try {
                    date = LocalDate.parse(dateStr);
                } catch (Exception ignored) {}
            }

            ArrayList<ShiftOption> parcelableShifts = args.getParcelableArrayList(ARG_SHIFTS_LIST);
            if (parcelableShifts != null) {
                availableShifts.clear();
                availableShifts.addAll(parcelableShifts);
            }
        }

        binding.tvTitle.setText("Xếp ca cho " + employeeName);
        binding.tvSubtitle.setText(formatVietnameseDate(date));

        // 1. Đăng ký nhận kết quả ca mới tạo từ AddShiftBottomSheet và gọi ViewModel lưu vào CSDL
        getChildFragmentManager().setFragmentResultListener(
                AddShiftBottomSheet.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    ShiftOption newShift = result.getParcelable(AddShiftBottomSheet.EXTRA_RESULT_SHIFT);
                    if (newShift != null && newShift.getId() == null) {
                        if (sharedPreferenceApp != null && newShift.getLatitude() != null && newShift.getLongitude() != null) {
                            sharedPreferenceApp.saveShiftLocation(
                                    newShift.getTitle(),
                                    newShift.getLatitude(),
                                    newShift.getLongitude(),
                                    newShift.getCheckInRadius() != null ? newShift.getCheckInRadius() : 100.0
                            );
                        }
                        // Thêm ca mới
                        if (sharedPreferenceApp != null && newShift.getLatitude() != null && newShift.getLongitude() != null) {
                            sharedPreferenceApp.saveLatestShiftLocation(
                                    newShift.getLatitude(),
                                    newShift.getLongitude(),
                                    newShift.getCheckInRadius() != null ? newShift.getCheckInRadius() : 100.0,
                                    ""
                            );
                        }
                        assignViewModel.addShift(
                                newShift.getTitle(),
                                newShift.getStartTime(),
                                newShift.getEndTime(),
                                newShift.getAllowedCheckInTime(),
                                newShift.getCheckInRadius() != null ? newShift.getCheckInRadius() : 100.0,
                                newShift.getLatitude(),
                                newShift.getLongitude()
                        );
                    }
                }
        );

        // 2. Sự kiện nhấn nút "Thêm ca làm khác" bên dưới RecyclerView
        binding.btnAddOtherShift.setOnClickListener(v -> {
            AddShiftBottomSheet addShiftSheet = AddShiftBottomSheet.newInstance();
            addShiftSheet.show(getChildFragmentManager(), "AddShiftBottomSheet");
        });

        // 3. Quan sát stream danh sách ca từ Repository qua AssignShiftViewModel
        assignViewModel.getShiftsLiveData().observe(getViewLifecycleOwner(), shifts -> {
            if (shifts != null && !shifts.isEmpty()) {
                availableShifts.clear();
                availableShifts.addAll(shifts);
                setupShiftRecyclerView();

                // Tự động chọn ca vừa được tạo mới gần nhất
                ShiftOption latestSelected = assignViewModel.getSelectedShiftLiveData().getValue();
                if (latestSelected != null) {
                    this.selectedShift = latestSelected;
                    if (binding.rvShifts.getAdapter() instanceof QuickShiftAdapter) {
                        ((QuickShiftAdapter) binding.rvShifts.getAdapter()).setSelectedShift(latestSelected);
                    }
                    binding.btnConfirm.setEnabled(true);
                }
            } else {
                showEmptyState();
            }
        });

        // 4. Hiển thị ban đầu
        if (availableShifts.isEmpty()) {
            showEmptyState();
        } else {
            setupShiftRecyclerView();
        }

        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnConfirm.setOnClickListener(v -> {
            if (availableShifts.isEmpty()) {
                // Nếu chưa có ca -> Mở AddShiftBottomSheet tạo ca mới dưới dạng Dialog con
                AddShiftBottomSheet addShiftSheet = AddShiftBottomSheet.newInstance();
                addShiftSheet.show(getChildFragmentManager(), "AddShiftBottomSheet");
            } else {
                if (selectedShift != null && sharedPreferenceApp != null) {
                    if (selectedShift.getLatitude() != null && selectedShift.getLongitude() != null
                            && (selectedShift.getLatitude() != 0.0 || selectedShift.getLongitude() != 0.0)) {
                        double radius = selectedShift.getCheckInRadius() != null && selectedShift.getCheckInRadius() > 0 ? selectedShift.getCheckInRadius() : sharedPreferenceApp.getShiftRadius(selectedShift.getTitle());
                        if (radius <= 0) radius = sharedPreferenceApp.getLatestShiftRadius();
                        if (radius <= 0) radius = 100.0;
                        sharedPreferenceApp.saveShiftLocation(
                                selectedShift.getTitle(),
                                selectedShift.getLatitude(),
                                selectedShift.getLongitude(),
                                radius
                        );
                        sharedPreferenceApp.saveLatestShiftLocation(
                                selectedShift.getLatitude(),
                                selectedShift.getLongitude(),
                                radius,
                                ""
                        );
                    }
                }
                // 1. Trả kết quả qua FragmentResult API (Chống mất callback khi xoay màn hình)
                Bundle result = new Bundle();
                result.putParcelable(EXTRA_SELECTED_SHIFT, selectedShift);
                getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);

                // 2. Trả kết quả qua listener truyền thống (nếu có)
                if (listener != null) {
                    listener.onShiftAssigned(selectedShift);
                }
                dismiss();
            }
        });
    }

    private void showEmptyState() {
        binding.rvShifts.setVisibility(View.GONE);
        binding.btnAddOtherShift.setVisibility(View.GONE); // Ẩn nút "Thêm ca làm khác" khi trống ca
        binding.layoutEmptyState.getRoot().setVisibility(View.VISIBLE);

        binding.btnConfirm.setText("Tạo ca làm việc");
        binding.btnConfirm.setEnabled(true);
    }

    private void setupShiftRecyclerView() {
        binding.layoutEmptyState.getRoot().setVisibility(View.GONE);
        binding.rvShifts.setVisibility(View.VISIBLE);
        binding.btnAddOtherShift.setVisibility(View.VISIBLE); // Hiển thị nút "Thêm ca làm khác" khi có ít nhất 1 ca
        binding.btnConfirm.setText("Xác nhận xếp ca");

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

package com.example.attendanceapp.ui.employee.schedule.leave;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.data.dto.request.staff.CreateLeaveRequest;
import com.example.attendanceapp.data.dto.response.staff.StaffWorkScheduleResponse;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.databinding.FragmentLeaveRequestBinding;
import com.example.attendanceapp.domain.model.ApprovalRequestType;
import com.example.attendanceapp.domain.model.ApprovalStatus;
import com.example.attendanceapp.domain.model.LeaveRequest;
import com.example.attendanceapp.domain.model.ShiftOption;
import com.example.attendanceapp.domain.repository.ShiftRepository;
import com.example.attendanceapp.domain.repository.StaffRepository;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class EmployeeLeaveRequestFragment extends Fragment {

    private FragmentLeaveRequestBinding binding;

    @Inject
    StaffRepository staffRepository;

    @Inject
    ShiftRepository shiftRepository;

    @Inject
    SharedPreferenceApp sharedPreferenceApp;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public static class ShiftItem {
        public final String id;
        public final String name;
        public final String label;

        public ShiftItem(String id, String name, String label) {
            this.id = id;
            this.name = name;
            this.label = label;
        }

        @NonNull
        @Override
        public String toString() {
            return label;
        }
    }

    private final List<ShiftItem> shiftItemList = new ArrayList<>();
    private ArrayAdapter<ShiftItem> shiftAdapter;

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
        setupDatePicker();
        setupInputWatchers();
        setupShiftDropdown();
        readArgumentsBundle();
        setupSubmitButton();

        // Mặc định nạp ca làm cho ngày hiện tại nếu chưa chọn ngày
        if (TextUtils.isEmpty(binding.etDate.getText())) {
            String todayStr = LocalDate.now().toString();
            binding.etDate.setText(todayStr);
            loadShiftsForDate(todayStr);
        } else {
            loadShiftsForDate(binding.etDate.getText().toString().trim());
        }
    }

    private void setupToolbar() {
        if (getParentFragment() instanceof EmployeeCreateRequestContainerFragment) {
            binding.toolbarApp.setVisibility(View.GONE);
        } else {
            binding.toolbarApp.setVisibility(View.VISIBLE);
            binding.toolbarApp.setTitle("Tạo đơn xin nghỉ phép");
            binding.toolbarApp.setBackButtonVisible(true);
            binding.toolbarApp.setOnBackClickListener(() -> Navigation.findNavController(requireView()).navigateUp());
        }
    }

    private void setupShiftDropdown() {
        shiftAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, shiftItemList);
        binding.actShiftInfo.setAdapter(shiftAdapter);
    }

    private void readArgumentsBundle() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            String defaultDate = bundle.getString("selected_date");
            if (!TextUtils.isEmpty(defaultDate)) {
                binding.etDate.setText(defaultDate);
                loadShiftsForDate(defaultDate);
            }
        }
    }

    private void setupDatePicker() {
        binding.etDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày xin nghỉ")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String selectedDate = sdf.format(new Date(selection));
                binding.etDate.setText(selectedDate);
                binding.tilDate.setError(null);
                loadShiftsForDate(selectedDate);
            });

            datePicker.show(getChildFragmentManager(), "DATE_PICKER");
        });
    }

    /**
     * Tải danh sách ca làm việc thực tế từ backend tương ứng với ngày được chọn.
     */
    private void loadShiftsForDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return;

        disposables.add(
                staffRepository.getWorkSchedules()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    shiftItemList.clear();
                                    boolean hasShiftsOnDay = false;

                                    if (response != null && response.getData() != null) {
                                        for (StaffWorkScheduleResponse ws : response.getData()) {
                                            if (ws != null && dateStr.equals(ws.getWorkDay()) && ws.getShifts() != null) {
                                                for (StaffWorkScheduleResponse.ShiftInfo si : ws.getShifts()) {
                                                    if (si != null) {
                                                        String shiftId = (si.getShiftId() != null && !si.getShiftId().trim().isEmpty())
                                                                ? si.getShiftId().trim()
                                                                : (si.getWorkScheduleId() != null ? si.getWorkScheduleId().trim() : "shift_01");
                                                        String shiftName = si.getShiftName() != null ? si.getShiftName().trim() : "Ca làm việc";
                                                        String timeRange = "";
                                                        if (si.getStartTime() != null && si.getEndTime() != null) {
                                                            timeRange = " (" + si.getStartTime() + " - " + si.getEndTime() + ")";
                                                        }
                                                        shiftItemList.add(new ShiftItem(shiftId, shiftName, shiftName + timeRange));
                                                        hasShiftsOnDay = true;
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (hasShiftsOnDay) {
                                        shiftItemList.add(new ShiftItem("shift_all", "Cả ngày", "Cả ngày (Tất cả ca làm việc)"));
                                        updateShiftDropdown();
                                    } else {
                                        // Nếu ngày này chưa được phân ca, nạp danh sách cấu hình ca chung của công ty
                                        loadFallbackCompanyShifts();
                                    }
                                },
                                throwable -> loadFallbackCompanyShifts()
                        )
        );
    }

    private void loadFallbackCompanyShifts() {
        disposables.add(
                shiftRepository.getShifts()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                options -> {
                                    shiftItemList.clear();
                                    if (options != null && !options.isEmpty()) {
                                        for (ShiftOption opt : options) {
                                            String id = opt.getId() != null ? opt.getId().trim() : "shift_01";
                                            String title = opt.getTitle() != null ? opt.getTitle().trim() : "Ca làm";
                                            String timeRange = "";
                                            if (opt.getStartTime() != null && opt.getEndTime() != null) {
                                                timeRange = " (" + opt.getStartTime() + " - " + opt.getEndTime() + ")";
                                            }
                                            shiftItemList.add(new ShiftItem(id, title, title + timeRange));
                                        }
                                        shiftItemList.add(new ShiftItem("shift_all", "Cả ngày", "Cả ngày (Tất cả ca làm việc)"));
                                    } else {
                                        populateDefaultShiftItems();
                                    }
                                    updateShiftDropdown();
                                },
                                throwable -> {
                                    shiftItemList.clear();
                                    populateDefaultShiftItems();
                                    updateShiftDropdown();
                                }
                        )
        );
    }

    private void populateDefaultShiftItems() {
        shiftItemList.add(new ShiftItem("shift_01", "Ca sáng", "Ca sáng (08:00 - 12:00)"));
        shiftItemList.add(new ShiftItem("shift_02", "Ca chiều", "Ca chiều (13:30 - 17:30)"));
        shiftItemList.add(new ShiftItem("shift_03", "Ca hành chính", "Ca hành chính (08:00 - 17:30)"));
        shiftItemList.add(new ShiftItem("shift_all", "Cả ngày", "Cả ngày (Tất cả ca làm việc)"));
    }

    private void updateShiftDropdown() {
        if (binding == null || getContext() == null) return;
        shiftAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, shiftItemList);
        binding.actShiftInfo.setAdapter(shiftAdapter);

        if (!shiftItemList.isEmpty()) {
            binding.actShiftInfo.setText(shiftItemList.get(0).label, false);
        }
    }

    private void setupInputWatchers() {
        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilDate.setError(null);
                binding.tilShiftInfo.setError(null);
                binding.tilReason.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.etDate.addTextChangedListener(clearErrorWatcher);
        binding.actShiftInfo.addTextChangedListener(clearErrorWatcher);
        binding.etReason.addTextChangedListener(clearErrorWatcher);
    }

    private void setupSubmitButton() {
        binding.btnSubmit.setOnClickListener(v -> {
            String date = binding.etDate.getText().toString().trim();
            String shiftLabel = binding.actShiftInfo.getText().toString().trim();
            String reason = binding.etReason.getText() != null ? binding.etReason.getText().toString().trim() : "";
            boolean isEmergency = binding.cbIsEmergency.isChecked();

            if (TextUtils.isEmpty(date)) {
                binding.tilDate.setError("Vui lòng chọn ngày xin nghỉ");
                return;
            }

            if (TextUtils.isEmpty(shiftLabel)) {
                binding.tilShiftInfo.setError("Vui lòng chọn ca xin nghỉ");
                return;
            }

            if (TextUtils.isEmpty(reason)) {
                binding.tilReason.setError("Vui lòng nhập lý do cụ thể");
                return;
            }

            String shiftId = "shift_01";
            String shiftName = shiftLabel;
            for (ShiftItem item : shiftItemList) {
                if (item.label.equalsIgnoreCase(shiftLabel)) {
                    shiftId = item.id;
                    shiftName = item.name;
                    break;
                }
            }

            sendLeaveRequest(date, shiftId, shiftName, reason, isEmergency);
        });
    }

    private void sendLeaveRequest(String date, String shiftId, String shiftName, String reason, boolean isEmergency) {
        binding.btnSubmit.setEnabled(false);

        String finalReason = isEmergency ? "[ĐỘT XUẤT] " + reason : reason;
        CreateLeaveRequest request = new CreateLeaveRequest(date, shiftId, finalReason);

        disposables.add(staffRepository.requestLeave(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            if (isAdded()) {
                                if (sharedPreferenceApp != null) {
                                    sharedPreferenceApp.saveLeaveDate(date);
                                    String uid = sharedPreferenceApp.getUserId() != null ? sharedPreferenceApp.getUserId() : "staff";
                                    LeaveRequest submitted = new LeaveRequest(
                                            UUID.randomUUID().toString(),
                                            uid,
                                            ApprovalRequestType.LEAVE,
                                            date,
                                            shiftName,
                                            finalReason,
                                            isEmergency,
                                            ApprovalStatus.PENDING,
                                            System.currentTimeMillis()
                                        );
                                    sharedPreferenceApp.saveSubmittedLeaveRequest(submitted);
                                }
                                Toast.makeText(requireContext(), "Gửi đơn xin nghỉ phép thành công!", Toast.LENGTH_SHORT).show();
                                Navigation.findNavController(requireView()).navigateUp();
                            }
                        },
                        throwable -> {
                            if (isAdded()) {
                                binding.btnSubmit.setEnabled(true);
                                Toast.makeText(requireContext(), "Lỗi: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                )
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }
}

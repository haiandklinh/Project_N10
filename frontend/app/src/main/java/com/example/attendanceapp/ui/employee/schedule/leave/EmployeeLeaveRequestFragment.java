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
import com.example.attendanceapp.data.dto.response.BaseListResponse;
import com.example.attendanceapp.data.local.dao.ShiftDao;
import com.example.attendanceapp.data.local.dao.WorkScheduleDao;
import com.example.attendanceapp.data.local.entity.ShiftEntity;
import com.example.attendanceapp.data.local.entity.WorkScheduleEntity;
import io.reactivex.rxjava3.core.Single;
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
    ShiftDao shiftDao;

    @Inject
    WorkScheduleDao workScheduleDao;

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
                Single.zip(
                        staffRepository.getWorkSchedules()
                                .subscribeOn(Schedulers.io())
                                .onErrorReturnItem(new BaseListResponse<>(new ArrayList<>())),
                        shiftDao.observeAllShifts()
                                .firstOrError()
                                .subscribeOn(Schedulers.io())
                                .onErrorReturnItem(new ArrayList<>()),
                        workScheduleDao.observeAllSchedules()
                                .firstOrError()
                                .subscribeOn(Schedulers.io())
                                .onErrorReturnItem(new ArrayList<>()),
                        (scheduleResponse, roomShifts, roomWorkSchedules) -> {
                            List<ShiftItem> items = new ArrayList<>();
                            String firstRealShiftId = null;

                            if (scheduleResponse != null && scheduleResponse.getData() != null) {
                                for (StaffWorkScheduleResponse ws : scheduleResponse.getData()) {
                                    String wsDate = ws != null && ws.getWorkDay() != null ? ws.getWorkDay().trim() : "";
                                    if (wsDate.length() >= 10) {
                                        wsDate = wsDate.substring(0, 10);
                                    }

                                    if (dateStr.equals(wsDate) && ws.getShifts() != null) {
                                        for (StaffWorkScheduleResponse.ShiftInfo si : ws.getShifts()) {
                                            if (si == null) continue;

                                            String shiftName = si.getShiftName() != null ? si.getShiftName().trim() : "Ca làm việc";
                                            String timeRange = "";
                                            if (si.getStartTime() != null && si.getEndTime() != null) {
                                                timeRange = " (" + formatTime(si.getStartTime()) + " - " + formatTime(si.getEndTime()) + ")";
                                            }

                                            // 1. Ưu tiên shiftId trực tiếp nếu server trả
                                            String realShiftId = (si.getShiftId() != null && !si.getShiftId().trim().isEmpty())
                                                    ? si.getShiftId().trim()
                                                    : null;

                                            // 2. Tra cứu trong bảng shifts (Room DB) theo tên ca
                                            if (realShiftId == null || realShiftId.isEmpty()) {
                                                if (roomShifts != null) {
                                                    for (ShiftEntity entity : roomShifts) {
                                                        if (entity != null && entity.getTitle() != null &&
                                                                entity.getTitle().trim().equalsIgnoreCase(shiftName)) {
                                                            realShiftId = entity.getId();
                                                            break;
                                                        }
                                                    }
                                                }
                                            }

                                            // 3. Tra cứu trong bảng shifts (Room DB) theo giờ bắt đầu/kết thúc
                                            if (realShiftId == null || realShiftId.isEmpty()) {
                                                if (roomShifts != null && si.getStartTime() != null) {
                                                    for (ShiftEntity entity : roomShifts) {
                                                        if (entity != null && entity.getStartTime() != null &&
                                                                entity.getStartTime().trim().startsWith(si.getStartTime().trim())) {
                                                            realShiftId = entity.getId();
                                                            break;
                                                        }
                                                    }
                                                }
                                            }

                                            // 4. Tra cứu trong bảng work_schedules (Room DB)
                                            if (realShiftId == null || realShiftId.isEmpty()) {
                                                if (roomWorkSchedules != null) {
                                                    for (WorkScheduleEntity wse : roomWorkSchedules) {
                                                        if (wse != null && wse.getShiftName() != null &&
                                                                wse.getShiftName().trim().equalsIgnoreCase(shiftName) &&
                                                                wse.getShiftId() != null && !wse.getShiftId().trim().isEmpty()) {
                                                            realShiftId = wse.getShiftId().trim();
                                                            break;
                                                        }
                                                    }
                                                }
                                            }

                                            // 5. Fallback cuối cùng là workScheduleId nếu không tìm thấy ID ca
                                            if (realShiftId == null || realShiftId.isEmpty()) {
                                                realShiftId = (si.getWorkScheduleId() != null && !si.getWorkScheduleId().trim().isEmpty())
                                                        ? si.getWorkScheduleId().trim()
                                                        : "";
                                            }

                                            if (!realShiftId.isEmpty()) {
                                                if (firstRealShiftId == null) {
                                                    firstRealShiftId = realShiftId;
                                                }
                                                items.add(new ShiftItem(realShiftId, shiftName, shiftName + timeRange));
                                            }
                                        }
                                    }
                                }
                            }

                            if (!items.isEmpty()) {
                                String allDayId = firstRealShiftId != null ? firstRealShiftId : "all";
                                items.add(new ShiftItem(allDayId, "Cả ngày", "Cả ngày (Tất cả ca làm việc)"));
                            }
                            return items;
                        }
                )
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                items -> {
                                    shiftItemList.clear();
                                    if (items != null && !items.isEmpty()) {
                                        shiftItemList.addAll(items);
                                        binding.tilShiftInfo.setHelperText(null);
                                        binding.tilShiftInfo.setError(null);
                                        binding.btnSubmit.setEnabled(true);
                                        updateShiftDropdown();
                                    } else {
                                        loadCompanyShiftsOrEmpty();
                                    }
                                },
                                throwable -> loadCompanyShiftsOrEmpty()
                        )
        );
    }

    private void loadCompanyShiftsOrEmpty() {
        disposables.add(
                shiftRepository.getShifts()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                options -> {
                                    shiftItemList.clear();
                                    String firstShiftId = null;
                                    if (options != null && !options.isEmpty()) {
                                        for (ShiftOption opt : options) {
                                            String id = opt.getId() != null ? opt.getId().trim() : "";
                                            if (id.isEmpty()) continue;

                                            if (firstShiftId == null) {
                                                firstShiftId = id;
                                            }
                                            String title = opt.getTitle() != null ? opt.getTitle().trim() : "Ca làm";
                                            String timeRange = "";
                                            if (opt.getStartTime() != null && opt.getEndTime() != null) {
                                                timeRange = " (" + formatTime(opt.getStartTime()) + " - " + formatTime(opt.getEndTime()) + ")";
                                            }
                                            shiftItemList.add(new ShiftItem(id, title, title + timeRange));
                                        }
                                        String allDayId = firstShiftId != null ? firstShiftId : "all";
                                        shiftItemList.add(new ShiftItem(allDayId, "Cả ngày", "Cả ngày (Tất cả ca làm việc)"));
                                        binding.tilShiftInfo.setHelperText("Ngày này chưa có phân ca cá nhân, hiển thị ca theo cấu hình công ty");
                                        binding.tilShiftInfo.setError(null);
                                        binding.btnSubmit.setEnabled(true);
                                    } else {
                                        binding.tilShiftInfo.setError("Ngày này chưa có ca làm việc nào");
                                        binding.tilShiftInfo.setHelperText(null);
                                        binding.btnSubmit.setEnabled(false);
                                    }
                                    updateShiftDropdown();
                                },
                                throwable -> {
                                    binding.tilShiftInfo.setError("Ngày này chưa có ca làm việc nào");
                                    binding.tilShiftInfo.setHelperText(null);
                                    binding.btnSubmit.setEnabled(false);
                                    updateShiftDropdown();
                                }
                        )
        );
    }

    private void updateShiftDropdown() {
        if (binding == null || getContext() == null) return;
        shiftAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, shiftItemList);
        binding.actShiftInfo.setAdapter(shiftAdapter);

        if (!shiftItemList.isEmpty()) {
            binding.actShiftInfo.setText(shiftItemList.get(0).label, false);
        } else {
            binding.actShiftInfo.setText("", false);
        }
    }

    private String formatTime(String time) {
        if (time != null && time.length() >= 5) {
            return time.substring(0, 5);
        }
        return time;
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

            if (TextUtils.isEmpty(shiftLabel) || shiftItemList.isEmpty()) {
                binding.tilShiftInfo.setError("Vui lòng chọn ca xin nghỉ hợp lệ");
                return;
            }

            if (TextUtils.isEmpty(reason)) {
                binding.tilReason.setError("Vui lòng nhập lý do cụ thể");
                return;
            }

            String shiftId = "";
            String shiftName = shiftLabel;
            for (ShiftItem item : shiftItemList) {
                if (item.label.equalsIgnoreCase(shiftLabel) || item.name.equalsIgnoreCase(shiftLabel)) {
                    shiftId = item.id;
                    shiftName = item.name;
                    break;
                }
            }

            if (shiftId.isEmpty() && !shiftItemList.isEmpty()) {
                shiftId = shiftItemList.get(0).id;
                shiftName = shiftItemList.get(0).name;
            }

            boolean isAllDay = "Cả ngày".equalsIgnoreCase(shiftName) || "all".equalsIgnoreCase(shiftId);
            String finalReason = isAllDay ? "[Nghỉ cả ngày] " + reason : reason;

            sendLeaveRequest(date, shiftId, shiftName, finalReason, isEmergency);
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

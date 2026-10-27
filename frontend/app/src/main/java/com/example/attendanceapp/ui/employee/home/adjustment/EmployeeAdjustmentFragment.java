package com.example.attendanceapp.ui.employee.home.adjustment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.data.dto.request.staff.AttendanceAdjustmentRequest;
import com.example.attendanceapp.databinding.FragmentEmployeeAdjustmentBinding;
import com.example.attendanceapp.databinding.LayoutCreateAdjustmentBinding;
import com.example.attendanceapp.domain.model.AdjustmentReasonType;
import com.example.attendanceapp.domain.model.ApprovalStatus;
import com.example.attendanceapp.domain.model.AttendanceAdjustment;
import com.example.attendanceapp.domain.repository.StaffRepository;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class EmployeeAdjustmentFragment extends Fragment {

    private FragmentEmployeeAdjustmentBinding binding;
    private EmployeeAdjustmentAdapter adapter;

    @Inject
    StaffRepository staffRepository;

    private final CompositeDisposable disposables = new CompositeDisposable();

    // Danh sách gốc chứa tất cả dữ liệu
    private final List<AttendanceAdjustment> adjustmentList = new ArrayList<>();

    public EmployeeAdjustmentFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEmployeeAdjustmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupRecyclerView();
        setupListeners();
        setupCreateForm();
        loadSampleData();
    }

    private void setupRecyclerView() {
        adapter = new EmployeeAdjustmentAdapter(new ArrayList<>());
        binding.rvAdjustments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAdjustments.setAdapter(adapter);
    }

    private void setupToolbar() {
        binding.toolbarApp.setTitle(getString(R.string.adjustment_title));
        binding.toolbarApp.setOnBackClickListener(() -> {
            if (binding != null && binding.layoutCreateView.getVisibility() == View.VISIBLE) {
                showListView();
            } else {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private void setupListeners() {
        // 1. Lắng nghe sự kiện Bấm nút FAB để mở Form tạo yêu cầu mới
        binding.fabCreateAdjustment.setOnClickListener(v -> showCreateView());

        // 2. Tự động thu gọn FAB khi cuộn danh sách
        binding.rvAdjustments.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && binding.fabCreateAdjustment.isExtended()) {
                    binding.fabCreateAdjustment.shrink();
                } else if (dy < 0 && !binding.fabCreateAdjustment.isExtended()) {
                    binding.fabCreateAdjustment.extend();
                }
            }
        });

        // 3. Lọc danh sách theo Tab
        binding.tabLayoutStats.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterListByCurrentTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 4. Lắng nghe sự kiện Bấm nút Hủy trên Form tạo
        binding.includeCreateAdjustment.btnCancel.setOnClickListener(v -> showListView());
    }

    private void filterListByCurrentTab(int tabPosition) {
        List<AttendanceAdjustment> filteredList;

        switch (tabPosition) {
            case 0: // Chờ duyệt (PENDING)
                filteredList = adjustmentList.stream()
                        .filter(a -> a.getStatus() == ApprovalStatus.PENDING)
                        .collect(Collectors.toList());
                break;
            case 1: // Đã duyệt (APPROVED)
                filteredList = adjustmentList.stream()
                        .filter(a -> a.getStatus() == ApprovalStatus.APPROVED)
                        .collect(Collectors.toList());
                break;
            case 2: // Từ chối (REJECTED)
                filteredList = adjustmentList.stream()
                        .filter(a -> a.getStatus() == ApprovalStatus.REJECTED)
                        .collect(Collectors.toList());
                break;
            default:
                filteredList = new ArrayList<>(adjustmentList);
                break;
        }

        adapter.updateData(filteredList);

        if (filteredList.isEmpty()) {
            binding.rvAdjustments.setVisibility(View.GONE);
            binding.includeEmptyState.getRoot().setVisibility(View.VISIBLE);
        } else {
            binding.rvAdjustments.setVisibility(View.VISIBLE);
            binding.includeEmptyState.getRoot().setVisibility(View.GONE);
        }
    }

    private void showListView() {
        binding.layoutListView.setVisibility(View.VISIBLE);
        binding.layoutCreateView.setVisibility(View.GONE);

        binding.tabLayoutStats.setVisibility(View.VISIBLE);
        binding.fabCreateAdjustment.show();
    }

    private void showCreateView() {
        binding.layoutListView.setVisibility(View.GONE);
        binding.layoutCreateView.setVisibility(View.VISIBLE);

        binding.tabLayoutStats.setVisibility(View.GONE);
        binding.fabCreateAdjustment.hide();
    }

    private void setupCreateForm() {
        LayoutCreateAdjustmentBinding createBinding = binding.includeCreateAdjustment;

        // Dropdown lấy nhãn hiển thị trực tiếp từ Enum AdjustmentReasonType
        List<String> adjustmentTypes = new ArrayList<>();
        for (AdjustmentReasonType type : AdjustmentReasonType.values()) {
            adjustmentTypes.add(type.getLabel());
        }

        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                adjustmentTypes
        );
        createBinding.actvAdjustmentType.setAdapter(dropdownAdapter);

        // DatePicker & TimePicker
        createBinding.etRequestDate.setOnClickListener(v -> showDatePickerDialog());
        createBinding.tilRequestDate.setEndIconOnClickListener(v -> showDatePickerDialog());

        createBinding.etCheckInTime.setOnClickListener(v -> showTimePickerDialog(true));
        createBinding.tilCheckInTime.setEndIconOnClickListener(v -> showTimePickerDialog(true));

        createBinding.etCheckOutTime.setOnClickListener(v -> showTimePickerDialog(false));
        createBinding.tilCheckOutTime.setEndIconOnClickListener(v -> showTimePickerDialog(false));

        // Nút Gửi yêu cầu
        createBinding.btnSubmitRequest.setOnClickListener(v -> {
            String date = createBinding.etRequestDate.getText().toString().trim();
            String type = createBinding.actvAdjustmentType.getText().toString().trim();
            String checkIn = createBinding.etCheckInTime.getText().toString().trim();
            String checkOut = createBinding.etCheckOutTime.getText().toString().trim();
            String reason = createBinding.etReason.getText().toString().trim();

            if (date.isEmpty() || type.isEmpty() || reason.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.error_fill_required_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            createBinding.btnSubmitRequest.setEnabled(false);

            AttendanceAdjustmentRequest req = new AttendanceAdjustmentRequest(
                    date,
                    "shift_01",
                    checkIn,
                    checkOut,
                    reason
            );

            disposables.add(staffRepository.requestAttendanceAdjustment("manager_01", req)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            () -> {
                                if (isAdded()) {
                                    Toast.makeText(requireContext(), getString(R.string.msg_adjustment_sent_success), Toast.LENGTH_SHORT).show();

                                    AdjustmentReasonType reasonType = AdjustmentReasonType.SUPPLEMENT_ATTENDANCE;
                                    for (AdjustmentReasonType t : AdjustmentReasonType.values()) {
                                        if (t.getLabel().equalsIgnoreCase(type)) {
                                            reasonType = t;
                                            break;
                                        }
                                    }

                                    adjustmentList.add(0, new AttendanceAdjustment(
                                            String.valueOf(System.currentTimeMillis()),
                                            reasonType,
                                            LocalDate.now(),
                                            LocalTime.of(8, 0),
                                            LocalTime.of(17, 30),
                                            reason,
                                            LocalDate.now(),
                                            ApprovalStatus.PENDING
                                    ));

                                    updateTabTitlesAndStats();
                                    filterListByCurrentTab(binding.tabLayoutStats.getSelectedTabPosition());
                                    clearFormFields();
                                    showListView();
                                }
                            },
                            throwable -> {
                                if (isAdded()) {
                                    createBinding.btnSubmitRequest.setEnabled(true);
                                    Toast.makeText(requireContext(), "Lỗi: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                    )
            );
        });
    }

    private void clearFormFields() {
        LayoutCreateAdjustmentBinding createBinding = binding.includeCreateAdjustment;
        createBinding.etRequestDate.setText("");
        createBinding.actvAdjustmentType.setText("", false);
        createBinding.etCheckInTime.setText("");
        createBinding.etCheckOutTime.setText("");
        createBinding.etReason.setText("");
        createBinding.btnSubmitRequest.setEnabled(true);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    binding.includeCreateAdjustment.etRequestDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePickerDialog(boolean isCheckIn) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    if (isCheckIn) {
                        binding.includeCreateAdjustment.etCheckInTime.setText(selectedTime);
                    } else {
                        binding.includeCreateAdjustment.etCheckOutTime.setText(selectedTime);
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void loadSampleData() {
        adjustmentList.clear();

        // Dữ liệu mẫu dùng Enum AdjustmentReasonType & ApprovalStatus
        adjustmentList.add(new AttendanceAdjustment(
                "1",
                AdjustmentReasonType.MISSING_CHECK_IN,
                LocalDate.now().minusDays(2),
                LocalTime.of(8, 5),
                null,
                "Quên chấm công vào do họp khách hàng ngay đầu giờ sáng.",
                LocalDate.now().minusDays(1),
                ApprovalStatus.PENDING
        ));

        adjustmentList.add(new AttendanceAdjustment(
                "2",
                AdjustmentReasonType.MISSING_CHECK_OUT,
                LocalDate.now().minusDays(5),
                null,
                LocalTime.of(17, 30),
                "Mất điện công ty cuối giờ không checkout được.",
                LocalDate.now().minusDays(4),
                ApprovalStatus.APPROVED
        ));

        adjustmentList.add(new AttendanceAdjustment(
                "3",
                AdjustmentReasonType.SUPPLEMENT_ATTENDANCE,
                LocalDate.now().minusDays(8),
                LocalTime.of(8, 30),
                LocalTime.of(17, 30),
                "Xe hỏng giữa đường.",
                LocalDate.now().minusDays(7),
                ApprovalStatus.REJECTED
        ));

        updateTabTitlesAndStats();
        filterListByCurrentTab(binding.tabLayoutStats.getSelectedTabPosition());
    }

    /**
     * Cập nhật số lượng đếm lên từng Tab từ Enum ApprovalStatus
     */
    private void updateTabTitlesAndStats() {
        long pending = adjustmentList.stream().filter(a -> a.getStatus() == ApprovalStatus.PENDING).count();
        long approved = adjustmentList.stream().filter(a -> a.getStatus() == ApprovalStatus.APPROVED).count();
        long rejected = adjustmentList.stream().filter(a -> a.getStatus() == ApprovalStatus.REJECTED).count();

        TabLayout.Tab tabPending = binding.tabLayoutStats.getTabAt(0);
        TabLayout.Tab tabApproved = binding.tabLayoutStats.getTabAt(1);
        TabLayout.Tab tabRejected = binding.tabLayoutStats.getTabAt(2);

        if (tabPending != null) tabPending.setText(getString(R.string.adjustment_tab_pending, pending));
        if (tabApproved != null) tabApproved.setText(getString(R.string.adjustment_tab_approved, approved));
        if (tabRejected != null) tabRejected.setText(getString(R.string.adjustment_tab_rejected, rejected));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }
}

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
import com.example.attendanceapp.databinding.FragmentAdjustmentBinding;
import com.example.attendanceapp.databinding.LayoutCreateAdjustmentBinding;
import com.example.attendanceapp.domain.model.AdjustmentReasonType;
import com.example.attendanceapp.domain.model.ApprovalStatus;
import com.example.attendanceapp.domain.model.AttendanceAdjustment;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EmployeeAdjustmentFragment extends Fragment {

    private FragmentAdjustmentBinding binding;
    private EmployeeAdjustmentAdapter adapter;

    // Danh sách gốc chứa tất cả dữ liệu
    private final List<AttendanceAdjustment> adjustmentList = new ArrayList<>();

    public EmployeeAdjustmentFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdjustmentBinding.inflate(inflater, container, false);
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
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
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
    }

    /**
     * Lọc danh sách RecyclerView theo vị trí Tab bằng Enum ApprovalStatus
     */
    private void filterListByCurrentTab(int tabPosition) {
        ApprovalStatus targetStatus;
        switch (tabPosition) {
            case 1:
                targetStatus = ApprovalStatus.APPROVED;
                break;
            case 2:
                targetStatus = ApprovalStatus.REJECTED;
                break;
            case 0:
            default:
                targetStatus = ApprovalStatus.PENDING;
                break;
        }

        List<AttendanceAdjustment> filteredList = adjustmentList.stream()
                .filter(item -> item.getStatus() == targetStatus)
                .collect(Collectors.toList());

        adapter.updateData(filteredList);

        boolean isEmpty = filteredList.isEmpty();
        binding.rvAdjustments.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.includeEmptyState.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void showListView() {
        binding.layoutListView.setVisibility(View.VISIBLE);
        binding.layoutCreateView.setVisibility(View.GONE);

        binding.tabLayoutStats.setVisibility(View.VISIBLE);
        binding.fabCreateAdjustment.show();

        filterListByCurrentTab(binding.tabLayoutStats.getSelectedTabPosition());
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
            String reason = createBinding.etReason.getText().toString().trim();

            if (date.isEmpty() || type.isEmpty() || reason.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.error_fill_required_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(requireContext(), getString(R.string.msg_adjustment_sent_success), Toast.LENGTH_SHORT).show();

            clearFormFields();
            showListView();
        });
    }

    private void clearFormFields() {
        LayoutCreateAdjustmentBinding createBinding = binding.includeCreateAdjustment;
        createBinding.etRequestDate.setText("");
        createBinding.actvAdjustmentType.setText("", false);
        createBinding.etCheckInTime.setText("");
        createBinding.etCheckOutTime.setText("");
        createBinding.etReason.setText("");
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
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
                LocalDate.of(2026, 9, 10),
                LocalTime.of(8, 5),
                null,
                "Quên chấm công vào do họp khách hàng ngay đầu giờ sáng.",
                LocalDate.of(2026, 9, 11),
                ApprovalStatus.PENDING
        ));

        adjustmentList.add(new AttendanceAdjustment(
                "2",
                AdjustmentReasonType.MISSING_CHECK_OUT,
                LocalDate.of(2026, 9, 8),
                null,
                LocalTime.of(17, 30),
                "Mất điện công ty cuối giờ không checkout được.",
                LocalDate.of(2026, 9, 9),
                ApprovalStatus.APPROVED
        ));

        adjustmentList.add(new AttendanceAdjustment(
                "3",
                AdjustmentReasonType.SUPPLEMENT_ATTENDANCE,
                LocalDate.of(2026, 9, 5),
                LocalTime.of(8, 30),
                LocalTime.of(17, 30),
                "Xe hỏng giữa đường.",
                LocalDate.of(2026, 9, 6),
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
        binding = null;
    }
}
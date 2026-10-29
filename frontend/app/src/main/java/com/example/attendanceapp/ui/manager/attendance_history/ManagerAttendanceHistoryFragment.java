package com.example.attendanceapp.ui.manager.attendance_history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentManagerAttendanceHistoryBinding;
import com.example.attendanceapp.domain.model.AttendanceStatus;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerAttendanceHistoryFragment extends Fragment {

    private FragmentManagerAttendanceHistoryBinding binding;
    private ManagerAttendanceHistoryViewModel viewModel;
    private ManagerAttendanceHistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerAttendanceHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ManagerAttendanceHistoryViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupListeners();
        setupObservers();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());
    }

    private void setupRecyclerView() {
        adapter = new ManagerAttendanceHistoryAdapter();
        binding.rvAttendanceHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAttendanceHistory.setAdapter(adapter);

        binding.includeEmptyState.tvEmptyTitle.setText("Chưa có dữ liệu chấm công");
        binding.includeEmptyState.tvEmptySubtitle.setText("Không tìm thấy bản ghi chấm công nào trong khoảng thời gian này.");
    }

    private void setupListeners() {
        // Toggle chế độ xem Tuần / Tháng
        binding.togglePeriod.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_week) {
                    viewModel.setPeriodMode(ManagerAttendanceHistoryViewModel.PeriodMode.WEEK);
                } else if (checkedId == R.id.btn_month) {
                    viewModel.setPeriodMode(ManagerAttendanceHistoryViewModel.PeriodMode.MONTH);
                }
            }
        });

        // Điều hướng mốc thời gian Trước / Sau
        binding.btnPrevPeriod.setOnClickListener(v -> viewModel.previousPeriod());
        binding.btnNextPeriod.setOnClickListener(v -> viewModel.nextPeriod());

        // Lọc theo trạng thái
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                group.check(R.id.chip_filter_all);
                viewModel.setFilter(null);
                return;
            }

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_filter_present) {
                viewModel.setFilter(AttendanceStatus.PRESENT);
            } else if (checkedId == R.id.chip_filter_late) {
                viewModel.setFilter(AttendanceStatus.LATE);
            } else if (checkedId == R.id.chip_filter_absent) {
                viewModel.setFilter(AttendanceStatus.ABSENT);
            } else if (checkedId == R.id.chip_filter_early) {
                viewModel.setFilter(AttendanceStatus.EARLY_LEAVE);
            } else {
                viewModel.setFilter(null);
            }
        });
    }

    private void setupObservers() {
        viewModel.getPeriodTitle().observe(getViewLifecycleOwner(), title -> {
            if (binding != null && title != null) {
                binding.tvPeriodTitle.setText(title);
            }
        });

        viewModel.getSummaryStats().observe(getViewLifecycleOwner(), stats -> {
            if (binding != null && stats != null) {
                binding.tvPresentCount.setText(String.valueOf(stats.presentCount));
                binding.tvLateCount.setText(String.valueOf(stats.lateCount));
                binding.tvAbsentCount.setText(String.valueOf(stats.absentCount));
                binding.tvEarlyLeaveCount.setText(String.valueOf(stats.earlyLeaveCount));
            }
        });

        viewModel.getAttendanceList().observe(getViewLifecycleOwner(), list -> {
            if (binding == null) return;
            boolean isEmpty = list == null || list.isEmpty();
            binding.rvAttendanceHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            binding.includeEmptyState.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            if (adapter != null) {
                adapter.updateData(list);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding != null) {
                binding.progressLoading.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getPeriodMode().observe(getViewLifecycleOwner(), mode -> {
            if (binding == null) return;
            if (mode == ManagerAttendanceHistoryViewModel.PeriodMode.WEEK) {
                binding.togglePeriod.check(R.id.btn_week);
            } else {
                binding.togglePeriod.check(R.id.btn_month);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        adapter = null;
    }
}

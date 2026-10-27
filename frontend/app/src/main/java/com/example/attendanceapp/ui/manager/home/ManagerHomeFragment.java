package com.example.attendanceapp.ui.manager.home;

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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentManagerHomeBinding;
import com.example.attendanceapp.domain.model.RecentCheckInItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerHomeFragment extends Fragment {

    private FragmentManagerHomeBinding binding;
    private ManagerHomeViewModel viewModel;
    private TodayShiftAdapter todayShiftAdapter;
    private RecentCheckInAdapter recentCheckInAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ManagerHomeViewModel.class);

        setupObservers();
        setupQuickActions();
        setupTodayShifts();
        setupRecentCheckIns();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;

            updateHeader(state);
            updateOverview(state);
            updateActionRequired(state);
            updateTodayShifts(state);
            updateRecentCheckIns(state);
        });
    }

    private void updateHeader(ManagerHomeViewModel.ManagerHomeUiState state) {
        binding.includeHeader.tvGreeting.setText(state.getUserName());
        binding.includeHeader.tvCompanyName.setText(state.getCompanyName());

        binding.includeHeader.notificationBadge.setVisibility(
                state.hasUnreadNotifications() ? View.VISIBLE : View.GONE
        );

        Glide.with(this)
                .load(state.getAvatarUrl())
                .placeholder(R.drawable.ic_avatar)
                .error(R.drawable.ic_avatar)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.includeHeader.ivAvatar);
    }

    private void updateOverview(ManagerHomeViewModel.ManagerHomeUiState state) {
        binding.includeOverview.tvOverviewDate.setText(state.getTodayDate());
        binding.includeOverview.tvTotalEmployees.setText(String.valueOf(state.getTotalEmployees()));
        binding.includeOverview.tvPresentCount.setText(String.valueOf(state.getPresentCount()));
        binding.includeOverview.tvLateCount.setText(String.valueOf(state.getLateCount()));
        binding.includeOverview.tvAbsentCount.setText(String.valueOf(state.getAbsentCount()));
        binding.includeOverview.tvLeaveCount.setText(String.valueOf(state.getLeaveCount()));
    }

    private void updateActionRequired(ManagerHomeViewModel.ManagerHomeUiState state) {
        if (state.getPendingApprovalCount() > 0) {
            binding.includeActionRequired.cardEmptyActionRequired.setVisibility(View.GONE);
            binding.includeActionRequired.rvActionRequired.setVisibility(View.VISIBLE);

            List<ManagerActionAlert> alerts = new ArrayList<>();
            alerts.add(new ManagerActionAlert(
                    state.getPendingApprovalCount() + " yêu cầu duyệt đơn",
                    "Đơn xin nghỉ phép và đổi ca đang chờ",
                    R.drawable.ic_approval,
                    R.attr.colorPrimary,
                    R.attr.colorAttendanceAbsentContainer,
                    () -> navigateTo(R.id.action_managerHomeFragment_to_managerApprovalFragment)
            ));

            binding.includeActionRequired.rvActionRequired.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.includeActionRequired.rvActionRequired.setAdapter(new ManagerActionAlertAdapter(alerts));
        } else {
            // Mặc định hiện card "Chưa có yêu cầu xử lý"
            binding.includeActionRequired.cardEmptyActionRequired.setVisibility(View.VISIBLE);
            binding.includeActionRequired.rvActionRequired.setVisibility(View.GONE);
            binding.includeActionRequired.cardEmptyActionRequired.setOnClickListener(v ->
                    navigateTo(R.id.action_managerHomeFragment_to_managerApprovalFragment));
        }
    }

    private void setupQuickActions() {
        List<ManagerQuickAction> actions = Arrays.asList(
                new ManagerQuickAction("Xếp ca", R.drawable.ic_calendar,
                        () -> navigateTo(R.id.action_managerHomeFragment_to_managerScheduleFragment)),
                new ManagerQuickAction("Duyệt đơn", R.drawable.ic_approval,
                        () -> navigateTo(R.id.action_managerHomeFragment_to_managerApprovalFragment)),
                new ManagerQuickAction("Mã mời & Duyệt", R.drawable.ic_profile,
                        () -> navigateTo(R.id.action_managerHomeFragment_to_managerJoinRequestFragment)),
                new ManagerQuickAction("Bảng lương", R.drawable.ic_payroll,
                        () -> navigateTo(R.id.action_managerHomeFragment_to_managerPayrollFragment)),
                new ManagerQuickAction("Thưởng & phạt", R.drawable.ic_gift,
                        () -> navigateTo(R.id.action_managerHomeFragment_to_managerRewardPunishFragment)),
                new ManagerQuickAction("Thống kê", R.drawable.ic_chart,
                        () -> navigateTo(R.id.action_managerHomeFragment_to_managerStatisticFragment))
        );

        RecyclerView rv = binding.includeQuickActions.rvQuickActions;
        rv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(new ManagerQuickActionAdapter(actions));

        // Scroll Indicator setup
        rv.post(this::updateScrollThumb);
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                updateScrollThumb();
            }
        });
    }

    private void updateScrollThumb() {
        if (binding == null) return;
        RecyclerView rv = binding.includeQuickActions.rvQuickActions;
        int totalRange = rv.computeHorizontalScrollRange();
        int extent = rv.computeHorizontalScrollExtent();
        int offset = rv.computeHorizontalScrollOffset();

        binding.includeQuickActions.scrollIndicatorTrack.setVisibility(View.VISIBLE);
        if (totalRange <= extent) {
            binding.includeQuickActions.scrollIndicatorThumb.setTranslationX(0f);
            return;
        }

        int trackWidth = binding.includeQuickActions.scrollIndicatorTrack.getWidth();
        int thumbWidth = binding.includeQuickActions.scrollIndicatorThumb.getWidth();
        float scrollPercent = (float) offset / (float) (totalRange - extent);
        binding.includeQuickActions.scrollIndicatorThumb.setTranslationX((trackWidth - thumbWidth) * scrollPercent);
    }

    private void setupListeners() {
        binding.includeHeader.btnNotification.setOnClickListener(v ->
                navigateTo(R.id.action_managerHomeFragment_to_notificationFragment));
        binding.includeHeader.ivAvatar.setOnClickListener(v ->
                navigateTo(R.id.action_managerHomeFragment_to_managerProfileFragment));
    }

    private void setupTodayShifts() {
        binding.includeTodayShifts.cardEmptyShifts.setVisibility(View.VISIBLE);
        binding.includeTodayShifts.rvTodayShifts.setVisibility(View.GONE);

        binding.includeTodayShifts.cardEmptyShifts.setOnClickListener(v ->
                navigateTo(R.id.action_managerHomeFragment_to_managerScheduleFragment));

        binding.includeTodayShifts.tvSeeAllShifts.setOnClickListener(v ->
                navigateTo(R.id.action_managerHomeFragment_to_managerScheduleFragment));
    }

    private void updateTodayShifts(ManagerHomeViewModel.ManagerHomeUiState state) {
        List<TodayShiftSummary> shifts = state.getTodayShifts();
        if (shifts != null && !shifts.isEmpty()) {
            binding.includeTodayShifts.cardEmptyShifts.setVisibility(View.GONE);
            binding.includeTodayShifts.rvTodayShifts.setVisibility(View.VISIBLE);

            if (todayShiftAdapter == null) {
                todayShiftAdapter = new TodayShiftAdapter(new ArrayList<>(shifts), shift ->
                        navigateTo(R.id.action_managerHomeFragment_to_managerScheduleFragment));
                binding.includeTodayShifts.rvTodayShifts.setLayoutManager(new LinearLayoutManager(getContext()));
                binding.includeTodayShifts.rvTodayShifts.setAdapter(todayShiftAdapter);
            } else {
                todayShiftAdapter.updateData(shifts);
            }
        } else {
            binding.includeTodayShifts.cardEmptyShifts.setVisibility(View.VISIBLE);
            binding.includeTodayShifts.rvTodayShifts.setVisibility(View.GONE);
        }
    }

    private void setupRecentCheckIns() {
        recentCheckInAdapter = new RecentCheckInAdapter(new ArrayList<>());
        RecyclerView rv = binding.includeRecentCheckins.rvRecentCheckins;
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(recentCheckInAdapter);

        rv.setVisibility(View.GONE);
        binding.includeRecentCheckins.includeEmptyState.getRoot().setVisibility(View.VISIBLE);
        binding.includeRecentCheckins.includeEmptyState.tvEmptyTitle.setText("Chưa có check-in hôm nay");
        binding.includeRecentCheckins.includeEmptyState.tvEmptySubtitle.setText("Chưa có nhân viên nào check-in trong ngày hôm nay.");

        binding.includeRecentCheckins.tvSeeAllCheckins.setOnClickListener(v ->
                navigateTo(R.id.action_managerHomeFragment_to_managerScheduleFragment));
    }

    private void updateRecentCheckIns(ManagerHomeViewModel.ManagerHomeUiState state) {
        if (binding == null) return;
        List<RecentCheckInItem> checkins = state.getRecentCheckIns();
        boolean hasData = checkins != null && !checkins.isEmpty();

        RecyclerView rv = binding.includeRecentCheckins.rvRecentCheckins;
        View emptyState = binding.includeRecentCheckins.includeEmptyState.getRoot();

        if (hasData) {
            rv.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            if (recentCheckInAdapter != null) {
                recentCheckInAdapter.updateData(checkins);
            }
        } else {
            rv.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            binding.includeRecentCheckins.includeEmptyState.tvEmptyTitle.setText("Chưa có check-in hôm nay");
            binding.includeRecentCheckins.includeEmptyState.tvEmptySubtitle.setText("Chưa có nhân viên nào check-in trong ngày hôm nay.");
        }
    }

    private void navigateTo(int destinationId) {
        if (getView() == null) return;
        Navigation.findNavController(requireView()).navigate(destinationId);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadHomeData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        todayShiftAdapter = null;
        recentCheckInAdapter = null;
        binding = null;
    }
}

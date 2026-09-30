package com.example.attendanceapp.ui.manager.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.example.attendanceapp.domain.model.ApprovalRequestType;
import com.example.attendanceapp.domain.model.ApprovalStatus;
import com.example.attendanceapp.domain.model.ManagerApprovalItem;
import com.example.attendanceapp.ui.manager.approval.ManagerApprovalAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerHomeFragment extends Fragment {

    private FragmentManagerHomeBinding binding;
    private ManagerHomeViewModel viewModel;

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
        setupOverview();
        setupQuickActions();
        setupPendingPreview();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;

            updateHeader(state);
        });
    }

    private void updateHeader(ManagerHomeViewModel.ManagerHomeUiState state) {
        binding.includeHeader.tvGreeting.setText(state.getUserName());
        binding.includeHeader.tvCompanyName.setText(state.getCompanyName());

        Glide.with(this)
                .load(state.getAvatarUrl())
                .placeholder(R.drawable.ic_avatar)
                .error(R.drawable.ic_avatar)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.includeHeader.ivAvatar);
    }

    private void setupOverview() {
        binding.includeOverview.tvTotalEmployees.setText("24");
        binding.includeOverview.tvPresentCount.setText("18");
        binding.includeOverview.tvLateCount.setText("3");
        binding.includeOverview.tvAbsentCount.setText("1");
        binding.includeOverview.tvLeaveCount.setText("2");
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

    private void setupPendingPreview() {
        List<ManagerApprovalItem> pending = new ArrayList<>();
        // Refactor: Thay ManagerApprovalItem.STATUS_PENDING -> ApprovalStatus.PENDING
        pending.add(new ManagerApprovalItem("1", "Trần Thị Bình", null, "Nhân viên bán hàng",
                ApprovalRequestType.LEAVE, "Ngày áp dụng: 20/09/2026", "Nghỉ cả ngày",
                "Bị ốm đột xuất cần nghỉ để đi khám bệnh.", ApprovalStatus.PENDING,
                System.currentTimeMillis()));
        pending.add(new ManagerApprovalItem("2", "Lê Văn Cường", null, "Nhân viên kho",
                ApprovalRequestType.ADJUSTMENT, "Ngày áp dụng: 19/09/2026", "Bổ sung check-out",
                "Quên bấm check-out lúc tan ca do sự cố máy chấm công.", ApprovalStatus.PENDING,
                System.currentTimeMillis()));

        RecyclerView rv = binding.includePending.rvPendingPreview;
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new ManagerApprovalAdapter(pending, new ManagerApprovalAdapter.OnActionListener() {
            @Override
            public void onApprove(ManagerApprovalItem item, int position) {
                Toast.makeText(getContext(), "Đã duyệt yêu cầu của " + item.getEmployeeName(), Toast.LENGTH_SHORT).show();
                navigateTo(R.id.managerApprovalFragment);
            }

            @Override
            public void onReject(ManagerApprovalItem item, int position) {
                Toast.makeText(getContext(), "Đã từ chối yêu cầu của " + item.getEmployeeName(), Toast.LENGTH_SHORT).show();
                navigateTo(R.id.managerApprovalFragment);
            }
        }));

        boolean isEmpty = pending.isEmpty();
        rv.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.includePending.includeEmptyState.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        binding.includePending.tvSeeAllPending.setOnClickListener(v -> navigateTo(R.id.managerApprovalFragment));
    }

    private void navigateTo(int destinationId) {
        if (getView() == null) return;
        Navigation.findNavController(requireView()).navigate(destinationId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
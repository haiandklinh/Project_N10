package com.example.attendanceapp.ui.manager.home;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentManagerHomeBinding;
import com.example.attendanceapp.domain.model.ApprovalRequestType;
import com.example.attendanceapp.domain.model.ManagerApprovalItem;
import com.example.attendanceapp.ui.manager.ManagerMainActivity;
import com.example.attendanceapp.ui.manager.approval.ManagerApprovalAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerHomeFragment extends Fragment {

    private FragmentManagerHomeBinding binding;
    private ManagerHomeViewModel viewModel;
    private List<ManagerApprovalItem> pendingList;
    private ManagerApprovalAdapter approvalAdapter;

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

        setupOverview();
        setupQuickActions();
        setupPendingPreview();
        setupListeners();
    }

    private void setupOverview() {
        binding.includeOverview.tvTotalEmployees.setText("24");
        binding.includeOverview.tvPresentCount.setText("18");
        binding.includeOverview.tvLateCount.setText("3");
        binding.includeOverview.tvAbsentCount.setText("1");
        binding.includeOverview.tvLeaveCount.setText("2");
    }

    private void setupQuickActions() {
        // Loại bỏ các tính năng đã có dưới BottomBar (Xếp ca, Duyệt đơn, Mã mời) để tránh lặp và tối ưu UI
        List<ManagerQuickAction> actions = Arrays.asList(
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

    private void setupListeners(){
        binding.includeHeader.btnNotification.setOnClickListener(v ->
                navigateTo(R.id.action_managerHomeFragment_to_notificationFragment));
        
        binding.includeHeader.ivAvatar.setOnClickListener(v -> {
            if (requireActivity() instanceof ManagerMainActivity) {
                ((ManagerMainActivity) requireActivity()).navigateToTab(R.id.managerProfileFragment);
            } else {
                navigateTo(R.id.action_managerHomeFragment_to_managerProfileFragment);
            }
        });

        binding.includePending.tvSeeAllPending.setOnClickListener(v -> {
            if (requireActivity() instanceof ManagerMainActivity) {
                ((ManagerMainActivity) requireActivity()).navigateToTab(R.id.managerApprovalFragment);
            } else {
                navigateTo(R.id.action_managerHomeFragment_to_managerApprovalFragment);
            }
        });

        // Logic xử lý nút sao chép Mã mời ngay trên Trang chủ
        if (binding.includeHeader.btnHomeCopyCode != null) {
            binding.includeHeader.btnHomeCopyCode.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Invite Code", binding.includeHeader.tvHomeInviteCode.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), getString(R.string.manager_profile_copy_success), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupPendingPreview() {
        pendingList = new ArrayList<>();
        
        // VÍ DỤ 1: Yêu cầu gia nhập bằng mã mời (Mới bổ sung)
        pendingList.add(new ManagerApprovalItem("join_1", "Nguyễn Minh Hoàng", null, "Ứng viên mới",
                ApprovalRequestType.JOIN_REQUEST, "Mã mời sử dụng: BKS-CODE-2024", "Yêu cầu gia nhập",
                "Chào quản lý, tôi là nhân viên mới phòng Sale, tôi xin tham gia hệ thống.", ManagerApprovalItem.STATUS_PENDING,
                System.currentTimeMillis()));

        // VÍ DỤ 2: Đơn nghỉ phép
        pendingList.add(new ManagerApprovalItem("1", "Trần Thị Bình", null, "Nhân viên bán hàng",
                ApprovalRequestType.LEAVE, "Ngày áp dụng: 20/09/2026", "Nghỉ cả ngày",
                "Bị ốm đột xuất cần nghỉ để đi khám bệnh.", ManagerApprovalItem.STATUS_PENDING,
                System.currentTimeMillis()));

        // VÍ DỤ 3: Giải trình công
        pendingList.add(new ManagerApprovalItem("2", "Lê Văn Cường", null, "Nhân viên kho",
                ApprovalRequestType.ADJUSTMENT, "Ngày áp dụng: 19/09/2026", "Bổ sung check-out",
                "Quên bấm check-out lúc tan ca do sự cố máy chấm công.", ManagerApprovalItem.STATUS_PENDING,
                System.currentTimeMillis()));

        RecyclerView rv = binding.includePending.rvPendingPreview;
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        
        approvalAdapter = new ManagerApprovalAdapter(pendingList, new ManagerApprovalAdapter.OnActionListener() {
            @Override
            public void onApprove(ManagerApprovalItem item, int position) {
                if (item.getId().startsWith("join")) {
                    Toast.makeText(requireContext(), "Đã chấp nhận nhân viên " + item.getEmployeeName() + " vào doanh nghiệp", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Đã duyệt yêu cầu của " + item.getEmployeeName(), Toast.LENGTH_SHORT).show();
                }
                removeItemFromPreview(position);
            }

            @Override
            public void onReject(ManagerApprovalItem item, int position) {
                if (item.getId().startsWith("join")) {
                    Toast.makeText(requireContext(), "Đã từ chối yêu cầu gia nhập của " + item.getEmployeeName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Đã từ chối yêu cầu của " + item.getEmployeeName(), Toast.LENGTH_SHORT).show();
                }
                removeItemFromPreview(position);
            }
        });
        
        rv.setAdapter(approvalAdapter);

        updateEmptyState();
    }

    private void removeItemFromPreview(int position) {
        if (pendingList != null && position >= 0 && position < pendingList.size()) {
            pendingList.remove(position);
            approvalAdapter.notifyItemRemoved(position);
            approvalAdapter.notifyItemRangeChanged(position, pendingList.size());
            updateEmptyState();
        }
    }

    private void updateEmptyState() {
        if (binding == null) return;
        boolean isEmpty = pendingList == null || pendingList.isEmpty();
        binding.includePending.rvPendingPreview.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.includePending.includeEmptyState.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
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
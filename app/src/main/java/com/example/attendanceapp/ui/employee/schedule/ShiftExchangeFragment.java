package com.example.attendanceapp.ui.employee.schedule;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.databinding.FragmentShiftExchangeBinding;
import com.example.attendanceapp.domain.model.LeaveRequest;
import com.example.attendanceapp.domain.model.Shift;
import com.example.attendanceapp.domain.model.ShiftStatus;
import com.example.attendanceapp.ui.employee.schedule.shift.UpcomingShiftAdapter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ShiftExchangeFragment extends Fragment {

    private FragmentShiftExchangeBinding binding;
    private UpcomingShiftAdapter myShiftAdapter;
    private UpcomingShiftAdapter availableShiftAdapter;

    // QUẢN LÝ HẠN MỨC ĐỔI CA
    private static final int MAX_EXCHANGE_PER_MONTH = 4;
    private int usedExchangeCount = 2; // Giả lập: Người dùng đã đổi 2 lượt trong tháng

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentShiftExchangeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupQuotaDisplay();
        setupRecyclerViews();
        loadMyShiftsData();
        setupSubmitButton();
    }

    private void setupToolbar() {
        binding.toolbarApp.setTitle("Yêu cầu đổi ca");
        binding.toolbarApp.setOnBackClickListener(() -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
    }

    /**
     * Hiển thị và kiểm tra trạng thái Hạn mức
     */
    private void setupQuotaDisplay() {
        int remainingQuota = MAX_EXCHANGE_PER_MONTH - usedExchangeCount;

        if (binding.tvQuotaStatus != null) {
            String quotaText = "Hạn mức tháng này: Bạn còn " + remainingQuota + "/" + MAX_EXCHANGE_PER_MONTH + " lượt đổi ca.";
            binding.tvQuotaStatus.setText(quotaText);
        }

        // Nếu đã hết lượt đổi ca -> Cảnh báo và Disable nút gửi
        if (remainingQuota <= 0) {
            if (binding.tvQuotaStatus != null) {
                binding.tvQuotaStatus.setText("Bạn đã hết lượt xin đổi ca trong tháng này (" + MAX_EXCHANGE_PER_MONTH + "/" + MAX_EXCHANGE_PER_MONTH + ").");
            }
            binding.btnSubmitExchange.setEnabled(false);
            binding.btnSubmitExchange.setAlpha(0.5f);
        }
    }

    private void setupRecyclerViews() {
        // 1. Adapter cho Ca của tôi
        myShiftAdapter = new UpcomingShiftAdapter();
        binding.rvMyShifts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMyShifts.setAdapter(myShiftAdapter);

        // Lắng nghe chọn ca của tôi -> Bỏ chọn ca trống cũ và tải danh sách ca trống mới
        myShiftAdapter.setOnItemClickListener((shift, position) -> {
            if (availableShiftAdapter != null) {
                availableShiftAdapter.clearSelection();
            }
            loadAvailableShiftsFor(shift);
        });

        // 2. Adapter cho Ca trống muốn đổi sang
        availableShiftAdapter = new UpcomingShiftAdapter();
        binding.rvAvailableShifts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAvailableShifts.setAdapter(availableShiftAdapter);
    }

    private void loadMyShiftsData() {
        List<Shift> myShifts = new ArrayList<>();
        myShifts.add(new Shift(
                "200",
                "1000",
                LocalDate.now(),
                "Ca Sáng",
                LocalTime.of(10,2),
                LocalTime.of(12, 3),
                "08:00 - 17:00",
                "Văn phòng chính · Tầng 3",
                ShiftStatus.PENDING));
        myShifts.add(new Shift(
                "200",
                "1000",
                LocalDate.now(),
                "Ca Sáng",
                LocalTime.of(10,2),
                LocalTime.of(12, 3),
                "08:00 - 17:00",
                "Văn phòng chính · Tầng 3",
                ShiftStatus.PENDING));

        myShiftAdapter.submitList(myShifts);
    }

    private void loadAvailableShiftsFor(Shift myShift) {
        List<Shift> availableShifts = new ArrayList<>();

        // Giả lập ca trống trùng ngày hoặc ngày khác
        availableShifts.add(new Shift(
                "200",
                "1000",
                LocalDate.now(),
                "Ca Sáng",
                LocalTime.of(10,2),
                LocalTime.of(12, 3),
                "08:00 - 17:00",
                "Văn phòng chính · Tầng 3",
                ShiftStatus.PENDING));

        if (!availableShifts.isEmpty()) {
            availableShiftAdapter.submitList(availableShifts);
            binding.rvAvailableShifts.setVisibility(View.VISIBLE);
            binding.tvEmptyAvailableShifts.setVisibility(View.GONE);
        } else {
            availableShiftAdapter.submitList(new ArrayList<>());
            binding.rvAvailableShifts.setVisibility(View.GONE);
            binding.tvEmptyAvailableShifts.setVisibility(View.VISIBLE);
            binding.tvEmptyAvailableShifts.setText("Không có ca trống nào khả dụng cho ngày này.");
        }
    }

    private void setupSubmitButton() {
        binding.btnSubmitExchange.setOnClickListener(v -> {
            // Kiểm tra lại hạn mức trước khi xử lý
            if (usedExchangeCount >= MAX_EXCHANGE_PER_MONTH) {
                Toast.makeText(requireContext(), "Bạn đã dùng hết lượt đổi ca của tháng này!", Toast.LENGTH_LONG).show();
                return;
            }

            Shift mySelectedShift = myShiftAdapter.getSelectedShift();
            if (mySelectedShift == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn ca của bạn cần đổi", Toast.LENGTH_SHORT).show();
                return;
            }

            Shift targetShift = availableShiftAdapter.getSelectedShift();
            if (targetShift == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn ca trống muốn đổi sang", Toast.LENGTH_SHORT).show();
                return;
            }

            String reason = binding.etReason.getText().toString().trim();
            if (reason.isEmpty()) {
                binding.tilReason.setError("Vui lòng nhập lý do đổi ca");
                return;
            } else {
                binding.tilReason.setError(null);
            }

            // Tạo đối tượng LeaveRequest
            LeaveRequest request = new LeaveRequest();
            request.setId(String.valueOf(System.currentTimeMillis()));
            request.setType("Đổi ca / Đổi lịch");
            request.setTargetDate(mySelectedShift.getDate().toString());
            request.setShiftId(targetShift.getShiftName());
            request.setReason(reason);
            request.setStatus("PENDING");
            request.setCreatedAt(System.currentTimeMillis());

            // Trừ 1 lượt hạn mức sau khi tạo thành công
            usedExchangeCount++;
            setupQuotaDisplay();

            Toast.makeText(requireContext(), "Đã gửi yêu cầu đổi sang " + targetShift.getShiftName(), Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).popBackStack();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
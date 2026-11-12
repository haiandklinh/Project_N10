package com.example.attendanceapp.ui.manager.human;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.AvatarLoader;
import com.example.attendanceapp.databinding.BottomSheetEmployeeDetailBinding;
import com.example.attendanceapp.domain.model.EmployeeStatus;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.NumberFormat;
import java.util.Locale;

public class EmployeeDetailBottomSheet extends BottomSheetDialogFragment {

    public interface OnEmployeeStatusChangeListener {
        void onStatusChanged(ManagerEmployeeItem employee, EmployeeStatus newStatus);
    }

    private BottomSheetEmployeeDetailBinding binding;
    private ManagerEmployeeItem employee;
    private OnEmployeeStatusChangeListener onStatusChangeListener;

    public static EmployeeDetailBottomSheet newInstance(ManagerEmployeeItem employee) {
        EmployeeDetailBottomSheet sheet = new EmployeeDetailBottomSheet();
        sheet.employee = employee;
        return sheet;
    }

    public void setOnEmployeeStatusChangeListener(OnEmployeeStatusChangeListener listener) {
        this.onStatusChangeListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetEmployeeDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (employee == null) {
            dismiss();
            return;
        }

        // 1. Gán thông tin nhân viên
        binding.tvName.setText(employee.getFullName());
        binding.tvPosition.setText(employee.getPosition() + " · " + employee.getDepartment());

        // Load Avatar
        AvatarLoader.loadAvatar(requireContext(), employee.getAvatarUrl(), binding.ivAvatar);

        // Đồng bộ SĐT
        String phone = employee.getPhoneNumber();
        if (phone != null && !phone.trim().isEmpty()) {
            binding.tvPhone.setText(phone.trim());
        } else {
            binding.tvPhone.setText("Chưa cập nhật SĐT");
        }

        // Đồng bộ Ngày vào làm
        String joinDate = employee.getJoinDateLabel();
        if (joinDate != null && !joinDate.trim().isEmpty()) {
            binding.tvJoinDate.setText("Ngày vào làm: " + joinDate.trim());
        } else {
            binding.tvJoinDate.setText("Ngày vào làm: Chưa cập nhật");
        }

        binding.tvShiftCount.setText(String.valueOf(employee.getShiftCountThisMonth()));
        binding.tvLeaveCount.setText(String.valueOf(employee.getLeaveCountThisMonth()));

        // 2. Định dạng lương
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        binding.tvSalary.setText(currencyFormat.format(employee.getEstimatedSalaryThisMonth()) + " đ");

        // 3. Hiển thị Chip trạng thái và nút bấm thay đổi trạng thái
        updateStatusViews();

        // 4. Bắt sự kiện thay đổi trạng thái nhân viên (Nghỉ việc / Làm việc)
        binding.btnToggleStatus.setOnClickListener(v -> showToggleStatusConfirmationDialog());

        binding.btnClose.setOnClickListener(v -> dismiss());
    }

    private void updateStatusViews() {
        if (binding == null || getContext() == null) return;

        EmployeeStatus status = employee.getStatus() != null ? employee.getStatus() : EmployeeStatus.ACTIVE;
        binding.chipStatus.setText(status.getLabel());
        binding.chipStatus.setTextColor(ContextCompat.getColor(requireContext(), status.getTextColorRes()));
        binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), status.getContainerColorRes())
        ));

        if (status == EmployeeStatus.TERMINATED) {
            binding.btnToggleStatus.setText("Khôi phục làm việc");
            binding.btnToggleStatus.setIconResource(R.drawable.ic_check);
            binding.btnToggleStatus.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.attendance_present)
            ));
            binding.btnToggleStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            binding.btnToggleStatus.setIconTint(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), android.R.color.white)
            ));
        } else {
            binding.btnToggleStatus.setText("Đánh dấu không còn làm việc");
            binding.btnToggleStatus.setIconResource(R.drawable.ic_close);
            binding.btnToggleStatus.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.attendance_absent)
            ));
            binding.btnToggleStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            binding.btnToggleStatus.setIconTint(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), android.R.color.white)
            ));
        }
    }

    private void showToggleStatusConfirmationDialog() {
        if (getContext() == null) return;

        boolean isTerminated = employee.getStatus() == EmployeeStatus.TERMINATED;
        String title = isTerminated ? "Khôi phục trạng thái làm việc" : "Xác nhận thôi việc";
        String message = isTerminated
                ? "Bạn có chắc chắn muốn khôi phục nhân viên " + employee.getFullName() + " sang trạng thái 'Đang làm việc' không?"
                : "Bạn có chắc chắn muốn đánh dấu nhân viên " + employee.getFullName() + " không còn làm việc tại công ty? Nhân viên này sẽ được chuyển sang mục 'Nghỉ việc'.";
        String positiveButtonText = isTerminated ? "Khôi phục" : "Đánh dấu nghỉ việc";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, (dialog, which) -> {
                    EmployeeStatus newStatus = isTerminated ? EmployeeStatus.ACTIVE : EmployeeStatus.TERMINATED;
                    employee.setStatus(newStatus);
                    updateStatusViews();

                    // Cập nhật thông qua ViewModel để cập nhật UI danh sách nhân sự
                    try {
                        ManagerHumanViewModel vm = new ViewModelProvider(requireActivity()).get(ManagerHumanViewModel.class);
                        vm.updateEmployeeStatus(employee.getUserId(), newStatus);
                    } catch (Exception ignored) {
                    }

                    if (onStatusChangeListener != null) {
                        onStatusChangeListener.onStatusChanged(employee, newStatus);
                    }

                    Toast.makeText(
                            requireContext(),
                            "Đã cập nhật trạng thái: " + newStatus.getLabel(),
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

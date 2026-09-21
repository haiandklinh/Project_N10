package com.example.attendanceapp.ui.employee.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentJobInfoBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class JobInfoFragment extends Fragment {

    private FragmentJobInfoBinding binding;
    // Mô phỏng vai trò người dùng (Sau này lấy từ PreferenceManager / AuthViewModel)
    private boolean isAdminOrHr = false;

    public JobInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentJobInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        applyUserRolePermissions();
        setupListeners();
    }

    private void setupToolbar() {
        binding.layoutToolbar.setTitle("Thông tin công việc");
        binding.layoutToolbar.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );
    }

    private void applyUserRolePermissions() {
        if (!isAdminOrHr) {
            // KHÓA DÀNH CHO NHÂN VIÊN THƯỜNG: Chỉ xem, không chỉnh sửa trực tiếp
            setFormEditable(false);
            binding.layoutBottomActions.setVisibility(View.GONE);
            binding.btnDeleteJobInfo.setVisibility(View.GONE);
            binding.btnRequestChange.setVisibility(View.VISIBLE);
        } else {
            // MỞ CHO ADMIN / HR: Được phép chỉnh sửa và xóa
            setFormEditable(true);
            binding.layoutBottomActions.setVisibility(View.VISIBLE);
            binding.btnDeleteJobInfo.setVisibility(View.VISIBLE);
            binding.btnRequestChange.setVisibility(View.GONE);
        }
    }

    private void setFormEditable(boolean editable) {
        binding.etJobTitle.setEnabled(editable);
        binding.etDepartment.setEnabled(editable);
        binding.etManager.setEnabled(editable);
        binding.etContractType.setEnabled(editable);
        binding.etWorkShift.setEnabled(editable);
        binding.etWorkLocation.setEnabled(editable);
        binding.etStartDate.setEnabled(editable);
        binding.etEndDate.setEnabled(editable);
    }

    private void setupListeners() {
        // Luồng dành cho Nhân viên: Chuyển sang màn hình tạo Ticket gửi HR
        binding.btnRequestChange.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_jobInfoFragment_to_createChangeRequestFragment)
        );

        // Luồng dành cho HR/Admin: Lưu cập nhật trực tiếp
        binding.btnSaveJobInfo.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã lưu cập nhật thông tin công việc (Quyền HR)", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigateUp();
        });

        // Luồng dành cho HR/Admin: Xóa thông tin công việc
        binding.btnDeleteJobInfo.setOnClickListener(v -> showDeleteConfirmDialog());
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa hồ sơ công việc")
                .setMessage("Bạn có chắc chắn muốn xóa dữ liệu công việc này không? Thao tác này có thể ảnh hưởng đến việc tính công.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    Toast.makeText(requireContext(), "Đã xóa thành công hồ sơ công việc", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
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
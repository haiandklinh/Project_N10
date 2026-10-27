package com.example.attendanceapp.ui.manager.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentManagerRolePermissionBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerRolePermissionFragment extends Fragment {

    private FragmentManagerRolePermissionBinding binding;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "ROLE_PERMISSIONS_PREFS";

    private static final String ROLE_LEADER = "LEADER";
    private static final String ROLE_STAFF = "STAFF";
    private static final String ROLE_ADMIN = "ADMIN";

    private String currentSelectedRole = ROLE_LEADER;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerRolePermissionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        setupToolbar();
        setupRoleChips();
        loadRolePermissions(currentSelectedRole);
        setupActionButtons();
    }

    private void setupToolbar() {
        binding.toolbarApp.setTitle(getString(R.string.manager_profile_permissions));
        binding.toolbarApp.setOnBackClickListener(() -> {
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    private void setupRoleChips() {
        binding.chipGroupRoles.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_role_leader) {
                currentSelectedRole = ROLE_LEADER;
            } else if (checkedId == R.id.chip_role_staff) {
                currentSelectedRole = ROLE_STAFF;
            } else if (checkedId == R.id.chip_role_admin) {
                currentSelectedRole = ROLE_ADMIN;
            }
            loadRolePermissions(currentSelectedRole);
        });
    }

    private void loadRolePermissions(String role) {
        if (ROLE_ADMIN.equals(role)) {
            // Admin có tất cả quyền và không thể tắt
            setSwitchState(binding.switchAssignShift, true, false);
            setSwitchState(binding.switchViewAllSchedule, true, false);
            setSwitchState(binding.switchApproveLeave, true, false);
            setSwitchState(binding.switchApproveShiftChange, true, false);
            setSwitchState(binding.switchManualAttendance, true, false);
            setSwitchState(binding.switchApproveJoinRequest, true, false);
            setSwitchState(binding.switchViewStaffDetail, true, false);
            setSwitchState(binding.switchViewStatistics, true, false);
            setSwitchState(binding.switchManagePayroll, true, false);
            binding.btnSavePermissions.setEnabled(false);
            binding.btnResetPermissions.setEnabled(false);
        } else if (ROLE_LEADER.equals(role)) {
            // Trưởng ca / Leader: load từ prefs hoặc lấy mặc định
            binding.btnSavePermissions.setEnabled(true);
            binding.btnResetPermissions.setEnabled(true);
            setSwitchState(binding.switchAssignShift, prefs.getBoolean("perm_leader_assign_shift", true), true);
            setSwitchState(binding.switchViewAllSchedule, prefs.getBoolean("perm_leader_view_all_schedule", true), true);
            setSwitchState(binding.switchApproveLeave, prefs.getBoolean("perm_leader_approve_leave", true), true);
            setSwitchState(binding.switchApproveShiftChange, prefs.getBoolean("perm_leader_approve_shift_change", true), true);
            setSwitchState(binding.switchManualAttendance, prefs.getBoolean("perm_leader_manual_attendance", false), true);
            setSwitchState(binding.switchApproveJoinRequest, prefs.getBoolean("perm_leader_approve_join_request", false), true);
            setSwitchState(binding.switchViewStaffDetail, prefs.getBoolean("perm_leader_view_staff_detail", true), true);
            setSwitchState(binding.switchViewStatistics, prefs.getBoolean("perm_leader_view_statistics", true), true);
            setSwitchState(binding.switchManagePayroll, prefs.getBoolean("perm_leader_manage_payroll", false), true);
        } else {
            // Nhân viên thường: hầu hết các quyền quản lý tắt
            binding.btnSavePermissions.setEnabled(true);
            binding.btnResetPermissions.setEnabled(true);
            setSwitchState(binding.switchAssignShift, prefs.getBoolean("perm_staff_assign_shift", false), true);
            setSwitchState(binding.switchViewAllSchedule, prefs.getBoolean("perm_staff_view_all_schedule", false), true);
            setSwitchState(binding.switchApproveLeave, prefs.getBoolean("perm_staff_approve_leave", false), true);
            setSwitchState(binding.switchApproveShiftChange, prefs.getBoolean("perm_staff_approve_shift_change", false), true);
            setSwitchState(binding.switchManualAttendance, prefs.getBoolean("perm_staff_manual_attendance", false), true);
            setSwitchState(binding.switchApproveJoinRequest, prefs.getBoolean("perm_staff_approve_join_request", false), true);
            setSwitchState(binding.switchViewStaffDetail, prefs.getBoolean("perm_staff_view_staff_detail", false), true);
            setSwitchState(binding.switchViewStatistics, prefs.getBoolean("perm_staff_view_statistics", false), true);
            setSwitchState(binding.switchManagePayroll, prefs.getBoolean("perm_staff_manage_payroll", false), true);
        }
    }

    private void setSwitchState(com.google.android.material.materialswitch.MaterialSwitch switchView, boolean isChecked, boolean isEnabled) {
        switchView.setChecked(isChecked);
        switchView.setEnabled(isEnabled);
    }

    private void setupActionButtons() {
        binding.btnSavePermissions.setOnClickListener(v -> savePermissions(currentSelectedRole));
        binding.btnResetPermissions.setOnClickListener(v -> resetPermissions(currentSelectedRole));
    }

    private void savePermissions(String role) {
        if (ROLE_ADMIN.equals(role)) return;

        String prefix = ROLE_LEADER.equals(role) ? "perm_leader_" : "perm_staff_";
        prefs.edit()
                .putBoolean(prefix + "assign_shift", binding.switchAssignShift.isChecked())
                .putBoolean(prefix + "view_all_schedule", binding.switchViewAllSchedule.isChecked())
                .putBoolean(prefix + "approve_leave", binding.switchApproveLeave.isChecked())
                .putBoolean(prefix + "approve_shift_change", binding.switchApproveShiftChange.isChecked())
                .putBoolean(prefix + "manual_attendance", binding.switchManualAttendance.isChecked())
                .putBoolean(prefix + "approve_join_request", binding.switchApproveJoinRequest.isChecked())
                .putBoolean(prefix + "view_staff_detail", binding.switchViewStaffDetail.isChecked())
                .putBoolean(prefix + "view_statistics", binding.switchViewStatistics.isChecked())
                .putBoolean(prefix + "manage_payroll", binding.switchManagePayroll.isChecked())
                .apply();

        String roleName = ROLE_LEADER.equals(role) ? "Trưởng ca / Quản lý nhóm" : "Nhân viên chính thức";
        Toast.makeText(requireContext(), "Đã lưu phân quyền cho " + roleName, Toast.LENGTH_SHORT).show();
    }

    private void resetPermissions(String role) {
        if (ROLE_ADMIN.equals(role)) return;

        String prefix = ROLE_LEADER.equals(role) ? "perm_leader_" : "perm_staff_";
        prefs.edit()
                .remove(prefix + "assign_shift")
                .remove(prefix + "view_all_schedule")
                .remove(prefix + "approve_leave")
                .remove(prefix + "approve_shift_change")
                .remove(prefix + "manual_attendance")
                .remove(prefix + "approve_join_request")
                .remove(prefix + "view_staff_detail")
                .remove(prefix + "view_statistics")
                .remove(prefix + "manage_payroll")
                .apply();

        loadRolePermissions(role);
        Toast.makeText(requireContext(), "Đã khôi phục phân quyền mặc định", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

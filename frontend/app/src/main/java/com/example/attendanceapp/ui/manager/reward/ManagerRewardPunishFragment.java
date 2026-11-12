package com.example.attendanceapp.ui.manager.reward;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.data.dto.request.manager.CreateBonusRequest;
import com.example.attendanceapp.data.dto.request.manager.CreateDeductionRequest;
import com.example.attendanceapp.data.dto.response.manager.StaffItemResponse;
import com.example.attendanceapp.databinding.FragmentManagerRewardPunishBinding;
import com.example.attendanceapp.domain.repository.ManagerRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class ManagerRewardPunishFragment extends Fragment {

    private FragmentManagerRewardPunishBinding binding;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final List<StaffItemResponse> staffList = new ArrayList<>();
    private final List<String> employeeNames = new ArrayList<>();

    private String selectedStaffId = "";
    private String selectedStaffName = "";

    @Inject
    ManagerRepository managerRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerRewardPunishBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            selectedStaffId = getArguments().getString("staff_id", "");
            selectedStaffName = getArguments().getString("staff_name", "");
            if (!selectedStaffName.isEmpty()) {
                binding.tvSelectEmployee.setText(selectedStaffName);
            }
        }

        setupToolbar();
        loadEmployees();
        setupListeners();
    }

    private void setupToolbar() {
        binding.toolbarApp.setOnBackClickListener(() -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
        binding.toolbarApp.setTitle("Khen thưởng / Phạt");
    }

    private void loadEmployees() {
        disposables.add(
                managerRepository.getStaffs()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    staffList.clear();
                                    employeeNames.clear();
                                    if (response != null && response.getData() != null) {
                                        for (StaffItemResponse staff : response.getData()) {
                                            if (staff.getNameStaff() != null && !staff.getNameStaff().isEmpty()) {
                                                staffList.add(staff);
                                                employeeNames.add(staff.getNameStaff() + " (" + staff.getPosition() + ")");
                                                if (staff.getStaffId() != null && staff.getStaffId().equals(selectedStaffId)) {
                                                    selectedStaffName = staff.getNameStaff();
                                                    binding.tvSelectEmployee.setText(selectedStaffName);
                                                }
                                            }
                                        }
                                    }
                                },
                                error -> {
                                    Toast.makeText(getContext(), "Lỗi tải dữ liệu nhân sự: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                        )
        );
    }

    private void setupListeners() {
        binding.tvSelectEmployee.setOnClickListener(v -> {
            if (employeeNames.isEmpty()) {
                Toast.makeText(getContext(), "Danh sách nhân viên trống", Toast.LENGTH_SHORT).show();
                return;
            }

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Chọn nhân sự")
                    .setItems(employeeNames.toArray(new String[0]), (d, i) -> {
                        StaffItemResponse chosen = staffList.get(i);
                        selectedStaffId = chosen.getStaffId();
                        selectedStaffName = chosen.getNameStaff();
                        binding.tvSelectEmployee.setText(selectedStaffName);
                    })
                    .show();
        });

        binding.btnSubmit.setOnClickListener(v -> {
            if (selectedStaffId.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn nhân sự áp dụng", Toast.LENGTH_SHORT).show();
                return;
            }

            String amountStr = binding.etAmount.getText().toString().trim().replaceAll("[.,]", "");
            String reason = binding.etReason.getText().toString().trim();

            if (amountStr.isEmpty() || reason.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ số tiền và lý do", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.btnSubmit.setEnabled(false);

            if (binding.rbReward.isChecked()) {
                CreateBonusRequest request = new CreateBonusRequest(selectedStaffId, amount, reason, "");
                disposables.add(
                        managerRepository.createBonus(request)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        res -> {
                                            Toast.makeText(getContext(), "Đã lưu quyết định Khen thưởng thành công!", Toast.LENGTH_SHORT).show();
                                            Navigation.findNavController(v).navigateUp();
                                        },
                                        err -> {
                                            binding.btnSubmit.setEnabled(true);
                                            Toast.makeText(getContext(), "Lỗi khi lưu quyết định: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                )
                );
            } else {
                CreateDeductionRequest request = new CreateDeductionRequest(selectedStaffId, amount, "PENALTY", reason);
                disposables.add(
                        managerRepository.createDeduction(request)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        res -> {
                                            Toast.makeText(getContext(), "Đã lưu quyết định Khấu trừ/Phạt thành công!", Toast.LENGTH_SHORT).show();
                                            Navigation.findNavController(v).navigateUp();
                                        },
                                        err -> {
                                            binding.btnSubmit.setEnabled(true);
                                            Toast.makeText(getContext(), "Lỗi khi lưu quyết định: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                )
                );
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }
}

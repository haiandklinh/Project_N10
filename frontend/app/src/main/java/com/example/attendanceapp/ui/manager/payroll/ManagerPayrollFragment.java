package com.example.attendanceapp.ui.manager.payroll;

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

import com.example.attendanceapp.R;
import com.example.attendanceapp.data.dto.response.manager.StaffItemResponse;
import com.example.attendanceapp.databinding.FragmentManagerPayrollBinding;
import com.example.attendanceapp.domain.model.EmployeeStatus;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;
import com.example.attendanceapp.domain.repository.ManagerRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class ManagerPayrollFragment extends Fragment {

    private FragmentManagerPayrollBinding binding;
    private ManagerPayrollAdapter adapter;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    ManagerRepository managerRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerPayrollBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupList();
        setupListeners();
    }

    private void setupToolbar() {
        binding.toolbarApp.setOnBackClickListener(() -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
        binding.toolbarApp.setTitle("Quản lý tiền lương");
    }

    private void setupList() {
        binding.rvPayrollList.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ManagerPayrollAdapter(new ArrayList<>(), employee -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_managerPayrollFragment_to_managerPayrollDetailFragment);
        });

        binding.rvPayrollList.setAdapter(adapter);
        loadEmployeesFromApi();
    }

    private void loadEmployeesFromApi() {
        if (managerRepository == null) return;
        disposables.add(
                managerRepository.getStaffs()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                                        List<ManagerEmployeeItem> list = new ArrayList<>();
                                        for (StaffItemResponse staff : response.getData()) {
                                            String id = staff.getStaffId();
                                            String code = "NV-" + (id != null && id.length() >= 4 ? id.substring(id.length() - 4) : (id != null ? id : ""));
                                            EmployeeStatus status = "deactive".equalsIgnoreCase(staff.getStatus())
                                                    ? EmployeeStatus.TERMINATED
                                                    : EmployeeStatus.ACTIVE;
                                            String pos = staff.getPosition() != null && !staff.getPosition().isEmpty() ? staff.getPosition() : "Nhân viên";
                                            list.add(new ManagerEmployeeItem(
                                                    id, code,
                                                    staff.getNameStaff() != null ? staff.getNameStaff() : "Nhân viên",
                                                    staff.getAvatar() != null ? staff.getAvatar() : "",
                                                    pos, "Phòng ban chung", "",
                                                    staff.getDateOfBirth() != null ? staff.getDateOfBirth() : "",
                                                    status, 24, 0, 7500000
                                            ));
                                        }
                                        adapter.updateData(list);
                                    }
                                },
                                error -> {
                                    // Bỏ qua lỗi kết nối
                                }
                        )
        );
    }

    private void setupListeners() {
        binding.tvMonthPicker.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chọn tháng", Toast.LENGTH_SHORT).show();
        });

        binding.btnConfirmAll.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Đã xác nhận chốt lương toàn bộ nhân sự", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }
}
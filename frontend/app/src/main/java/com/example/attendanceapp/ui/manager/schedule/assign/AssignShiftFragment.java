package com.example.attendanceapp.ui.manager.schedule.assign;

import android.app.DatePickerDialog;
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

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentAssignShiftBinding;
import com.example.attendanceapp.domain.model.ShiftOption;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AssignShiftFragment extends Fragment {

    private FragmentAssignShiftBinding binding;
    private AssignShiftViewModel viewModel;
    private AssignedEmployeeAdapter employeeAdapter;
    private ShiftOptionAdapter shiftAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAssignShiftBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AssignShiftViewModel.class);

        setupToolbar();
        setupSelectedEmployeesRv();
        setupDatePicker();
        setupShiftOptionsRv();
        setupActions();

        observeViewModel();
    }

    private void setupToolbar() {
        binding.toolbarApp.setTitle("Xếp ca nhân viên");
        binding.toolbarApp.setOnBackClickListener(() -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setupSelectedEmployeesRv() {
        employeeAdapter = new AssignedEmployeeAdapter(new ArrayList<>(), (employee, position) -> {
            viewModel.removeEmployee(position);
        });

        binding.rvSelectedEmployees.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSelectedEmployees.setAdapter(employeeAdapter);
    }

    private void setupDatePicker() {
        binding.cardSelectDate.setOnClickListener(v -> {
            LocalDate currentDate = viewModel.getSelectedDateLiveData().getValue();
            if (currentDate == null) currentDate = LocalDate.now();

            Calendar cal = Calendar.getInstance();
            cal.set(currentDate.getYear(), currentDate.getMonthValue() - 1, currentDate.getDayOfMonth());

            DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
                viewModel.setSelectedDate(LocalDate.of(year, month + 1, dayOfMonth));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

            dialog.show();
        });
    }

    private void setupShiftOptionsRv() {
        shiftAdapter = new ShiftOptionAdapter(new ArrayList<>(), null, new ShiftOptionAdapter.OnShiftActionListener() {
            @Override
            public void onShiftSelected(ShiftOption shift) {
                viewModel.selectShift(shift);
            }

            @Override
            public void onShiftEdit(ShiftOption shift, int position) {
                showEditShiftBottomSheet(shift);
            }

            @Override
            public void onShiftDelete(ShiftOption shift, int position) {
                showConfirmDeleteDialog(shift);
            }
        });

        binding.rvShiftOptions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvShiftOptions.setAdapter(shiftAdapter);

        // Mở BottomSheet thêm ca làm việc mới
        binding.btnAddOtherShift.setOnClickListener(v -> {
            AddShiftBottomSheet bottomSheet = AddShiftBottomSheet.newInstance((shiftName, startTime, endTime, description) -> {
                viewModel.addShift(shiftName, startTime, endTime, description);
                Toast.makeText(getContext(), "Đang tạo " + shiftName, Toast.LENGTH_SHORT).show();
            });

            bottomSheet.show(getChildFragmentManager(), "add_shift_bottom_sheet");
        });
    }

    private void observeViewModel() {
        // Quan sát danh sách nhân viên được chọn
        viewModel.getSelectedEmployeesLiveData().observe(getViewLifecycleOwner(), employees -> {
            int count = employees != null ? employees.size() : 0;
            binding.tvLabelEmployees.setText("Nhân viên được chọn (" + count + ")");
            employeeAdapter.updateData(employees);

            if (count == 0) {
                Toast.makeText(getContext(), "Danh sách nhân viên trống!", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSelectedDateLiveData().observe(getViewLifecycleOwner(), this::updateDateText);

        viewModel.getShiftsLiveData().observe(getViewLifecycleOwner(), shifts -> {
            boolean isEmpty = shifts == null || shifts.isEmpty();
            binding.cardEmptyShiftOptions.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.rvShiftOptions.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

            ShiftOption selected = viewModel.getSelectedShiftLiveData().getValue();
            shiftAdapter.updateData(shifts, selected);
        });

        viewModel.getSelectedShiftLiveData().observe(getViewLifecycleOwner(), selected -> {
            List<ShiftOption> currentList = viewModel.getShiftsLiveData().getValue();
            if (currentList != null) {
                shiftAdapter.updateData(currentList, selected);
            }
        });

        // Quan sát trạng thái Loading
        viewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            binding.btnConfirmAssign.setEnabled(!isLoading);
        });

        // Quan sát kết quả API gửi yêu cầu xếp ca
        viewModel.getAssignSuccessLiveData().observe(getViewLifecycleOwner(), isSuccess -> {
            if (Boolean.TRUE.equals(isSuccess)) {
                Navigation.findNavController(requireView()).navigate(R.id.action_assignShiftFragment_to_assignShiftSuccessFragment);
            }
        });

        viewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDateText(LocalDate selectedDate) {
        String dayOfWeek = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("vi"));
        String formatted = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        binding.tvSelectedDate.setText(formatted + " (" + dayOfWeek + ")");
    }

    private void showConfirmDeleteDialog(ShiftOption shift) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa ca làm việc")
                .setMessage("Bạn có chắc chắn muốn xóa " + shift.getTitle() + " không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteShift(shift);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditShiftBottomSheet(ShiftOption shift) {
        AddShiftBottomSheet bottomSheet = AddShiftBottomSheet.newInstanceForEdit(shift, (shiftName, startTime, endTime, description) -> {
            viewModel.updateShift(shift.getId(), shiftName, startTime, endTime, description);
            Toast.makeText(getContext(), "Đang cập nhật " + shiftName, Toast.LENGTH_SHORT).show();
        });

        bottomSheet.show(getChildFragmentManager(), "edit_shift_bottom_sheet");
    }

    private void setupActions() {
        binding.btnConfirmAssign.setOnClickListener(v -> {
            viewModel.submitAssignShifts();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
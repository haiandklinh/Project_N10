package com.example.attendanceapp.ui.manager.schedule.assign;

import android.app.DatePickerDialog;
import android.os.Build;
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
import com.example.attendanceapp.databinding.FragmentManagerAssignShiftBinding;
import com.example.attendanceapp.domain.model.ShiftOption;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AssignShiftFragment extends Fragment {

    private FragmentManagerAssignShiftBinding binding;
    private AssignShiftViewModel viewModel;
    private AssignedEmployeeAdapter employeeAdapter;
    private ShiftOptionAdapter shiftAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerAssignShiftBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AssignShiftViewModel.class);

        setupToolbar();
        setupSelectedEmployeesRv();
        setupDatePicker();
        setupShiftOptionsRv();
        setupActions();

        // Đăng ký lắng nghe kết quả THÊM CA MỚI từ AddShiftBottomSheet
        setupBottomSheetResultListener();

        observeViewModel();
    }

    private void setupToolbar() {
        binding.toolbarApp.setTitle("Xếp ca nhân viên");
        binding.toolbarApp.setOnBackClickListener(() -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
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
            List<LocalDate> selectedDates = viewModel.getSelectedDatesLiveData().getValue();
            LocalDate initialDate = (selectedDates != null && !selectedDates.isEmpty()) 
                    ? selectedDates.get(0) : LocalDate.now();

            DatePickerDialog dialog = new DatePickerDialog(
                    requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        LocalDate picked = LocalDate.of(year, month + 1, dayOfMonth);
                        viewModel.toggleDateSelection(picked);
                    },
                    initialDate.getYear(),
                    initialDate.getMonthValue() - 1,
                    initialDate.getDayOfMonth()
            );
            dialog.show();
        });
    }

    private void setupShiftOptionsRv() {
        // Khởi tạo ShiftOptionAdapter chỉ với listener Chọn ca
        shiftAdapter = new ShiftOptionAdapter(new ArrayList<>(), null, shift -> viewModel.selectShift(shift));

        binding.rvShiftOptions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvShiftOptions.setAdapter(shiftAdapter);

        // Nút mở BottomSheet để THÊM CA MỚI
        binding.btnAddOtherShift.setOnClickListener(v -> {
            AddShiftBottomSheet bottomSheet = AddShiftBottomSheet.newInstance();
            bottomSheet.show(getChildFragmentManager(), "add_shift_bottom_sheet");
        });
    }

    /**
     * Lắng nghe dữ liệu tạo ca mới từ AddShiftBottomSheet qua FragmentResult API
     */
    private void setupBottomSheetResultListener() {
        getChildFragmentManager().setFragmentResultListener(
                AddShiftBottomSheet.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    ShiftOption resultShift;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        resultShift = bundle.getParcelable(AddShiftBottomSheet.EXTRA_RESULT_SHIFT, ShiftOption.class);
                    } else {
                        resultShift = bundle.getParcelable(AddShiftBottomSheet.EXTRA_RESULT_SHIFT);
                    }

                    if (resultShift != null) {
                        // Thêm ca làm việc mới
                        viewModel.addShift(
                                resultShift.getTitle(),
                                resultShift.getStartTime(),
                                resultShift.getEndTime(),
                                resultShift.getLatitude(),
                                resultShift.getLongitude()
                        );
                        Toast.makeText(getContext(), "Đã tạo " + resultShift.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void observeViewModel() {
        viewModel.getSelectedEmployeesLiveData().observe(getViewLifecycleOwner(), employees -> {
            int count = employees != null ? employees.size() : 0;
            binding.tvLabelEmployees.setText("Nhân viên được chọn (" + count + ")");
            employeeAdapter.updateData(employees);
        });

        viewModel.getSelectedDatesLiveData().observe(getViewLifecycleOwner(), this::updateDateText);

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

        viewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            binding.btnConfirmAssign.setEnabled(!isLoading);
        });

        viewModel.getAssignSuccessLiveData().observe(getViewLifecycleOwner(), isSuccess -> {
            if (Boolean.TRUE.equals(isSuccess)) {
                viewModel.clearAssignSuccessEvent();
                Navigation.findNavController(requireView()).navigate(R.id.action_assignShiftFragment_to_assignShiftSuccessFragment);
            }
        });

        viewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.clearErrorMessage();
            }
        });
    }

    private void updateDateText(List<LocalDate> selectedDates) {
        if (selectedDates == null || selectedDates.isEmpty()) {
            binding.tvSelectedDate.setText("Chọn ngày...");
            return;
        }

        if (selectedDates.size() == 1) {
            LocalDate date = selectedDates.get(0);
            String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("vi"));
            String formatted = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            binding.tvSelectedDate.setText(formatted + " (" + dayOfWeek + ")");
        } else {
            binding.tvSelectedDate.setText("Đã chọn " + selectedDates.size() + " ngày");
        }
    }

    private void setupActions() {
        binding.btnConfirmAssign.setOnClickListener(v -> {
            String note = binding.edtNote.getText() != null ? binding.edtNote.getText().toString() : "";
            viewModel.submitAssignShifts(note);
        });

        binding.tvQuickSelectWeek.setOnClickListener(v -> {
            List<LocalDate> current = viewModel.getSelectedDatesLiveData().getValue();
            LocalDate baseDate = (current != null && !current.isEmpty()) ? current.get(0) : LocalDate.now();
            
            // Tìm ngày thứ 2 của tuần chứa baseDate
            LocalDate monday = baseDate;
            while (monday.getDayOfWeek() != java.time.DayOfWeek.MONDAY) {
                monday = monday.minusDays(1);
            }
            
            viewModel.selectFullWeek(monday);
            Toast.makeText(getContext(), "Đã chọn cả tuần", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
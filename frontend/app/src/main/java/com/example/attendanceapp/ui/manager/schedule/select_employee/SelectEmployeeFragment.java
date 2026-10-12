package com.example.attendanceapp.ui.manager.schedule.select_employee;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.attendanceapp.databinding.FragmentSelectEmployeeBinding;
import com.example.attendanceapp.domain.model.EmployeeStatus;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;
import com.example.attendanceapp.ui.manager.schedule.assign.AssignShiftViewModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SelectEmployeeFragment extends Fragment {

    private FragmentSelectEmployeeBinding binding;
    private SelectEmployeeAdapter adapter;
    private AssignShiftViewModel viewModel;

    private List<ManagerEmployeeItem> allEmployees = new ArrayList<>();
    private final Set<String> selectedEmployeeIds = new HashSet<>();
    private List<ManagerEmployeeItem> filteredEmployees = new ArrayList<>();

    private String searchQuery = "";
    private int selectedFilterStatus = FILTER_ALL;

    private static final int FILTER_ALL = 0;
    private static final int FILTER_WORKING = 1;
    private static final int FILTER_ABSENT = 2;
    private static final int FILTER_LEAVE = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSelectEmployeeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AssignShiftViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupFilterChips();
        setupBottomBar();
        setupSelectAll();
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getAllEmployeesLiveData().observe(getViewLifecycleOwner(), employees -> {
            if (employees != null) {
                this.allEmployees = employees;
                updateFilteredList();
            }
        });

        viewModel.getSelectedEmployeesLiveData().observe(getViewLifecycleOwner(), selected -> {
            selectedEmployeeIds.clear();

            if (selected != null) {
                for (ManagerEmployeeItem item : selected) {
                    selectedEmployeeIds.add(item.getUserId());
                }
            }

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            updateSelectionAllText();
            updateBottomBarCount();
        });

        viewModel.getSelectedDateLiveData().observe(getViewLifecycleOwner(), date -> {
            if (date != null) {
                String dayOfWeekName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("vi"));
                String dateFormatted = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                binding.tvAssignDateHint.setText("Xếp ca cho: " + dayOfWeekName + ", " + dateFormatted);
            }
        });
    }

    private void setupToolbar() {
        binding.toolbarApp.setTitle("Chọn nhân viên xếp ca");
        binding.toolbarApp.setOnBackClickListener(() -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
    }

    private void setupRecyclerView() {
        adapter = new SelectEmployeeAdapter(new ArrayList<>(), selectedEmployeeIds, (employee, isChecked) -> {
            viewModel.toggleEmployeeSelection(employee);
        });

        binding.rvEmployees.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvEmployees.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim().toLowerCase(Locale.ROOT);
                updateFilteredList();
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilterChips() {
        binding.chipAll.setOnClickListener(v -> {
            selectedFilterStatus = FILTER_ALL;
            updateFilteredList();
        });

        binding.chipWorking.setOnClickListener(v -> {
            selectedFilterStatus = FILTER_WORKING;
            updateFilteredList();
        });

        binding.chipAbsent.setOnClickListener(v -> {
            selectedFilterStatus = FILTER_ABSENT;
            updateFilteredList();
        });

        binding.chipLeave.setOnClickListener(v -> {
            selectedFilterStatus = FILTER_LEAVE;
            updateFilteredList();
        });
    }

    private void updateFilteredList() {
        List<ManagerEmployeeItem> filtered = new ArrayList<>();

        for (ManagerEmployeeItem item : allEmployees) {
            boolean matchesSearch = searchQuery.isEmpty()
                    || (item.getFullName() != null
                    && item.getFullName().toLowerCase(Locale.ROOT).contains(searchQuery))
                    || (item.getEmployeeCode() != null
                    && item.getEmployeeCode().toLowerCase(Locale.ROOT).contains(searchQuery));

            boolean matchesFilter = true;

            if (selectedFilterStatus == FILTER_WORKING) {
                matchesFilter = item.getStatus() == EmployeeStatus.ACTIVE;
            } else if (selectedFilterStatus == FILTER_LEAVE) {
                matchesFilter = item.getStatus() == EmployeeStatus.ON_LEAVE;
            } else if (selectedFilterStatus == FILTER_ABSENT) {
                matchesFilter = false;
            }

            if (matchesSearch && matchesFilter) {
                filtered.add(item);
            }
        }

        filteredEmployees = filtered;

        adapter.updateData(filteredEmployees);
        updateSelectionAllText();
        updateBottomBarCount();
    }
    private void setupSelectAll() {
        binding.tvSelectAll.setOnClickListener(v -> {
            if (filteredEmployees.isEmpty()) {
                return;
            }

            boolean allSelected = true;
            for (ManagerEmployeeItem employee : filteredEmployees) {
                if (!selectedEmployeeIds.contains(employee.getUserId())) {
                    allSelected = false;
                    break;
                }
            }

            List<ManagerEmployeeItem> currentSelected = viewModel.getSelectedEmployeesLiveData().getValue();
            if (currentSelected == null) currentSelected = new ArrayList<>();
            List<ManagerEmployeeItem> newList = new ArrayList<>(currentSelected);

            if (allSelected) {
                // Hủy chọn tất cả các nhân viên đang hiển thị
                for (ManagerEmployeeItem filtered : filteredEmployees) {
                    newList.removeIf(item -> item.getUserId().equals(filtered.getUserId()));
                }
            } else {
                // Chọn tất cả các nhân viên đang hiển thị (tránh trùng)
                for (ManagerEmployeeItem filtered : filteredEmployees) {
                    boolean alreadyIn = false;
                    for (ManagerEmployeeItem item : newList) {
                        if (item.getUserId().equals(filtered.getUserId())) {
                            alreadyIn = true;
                            break;
                        }
                    }
                    if (!alreadyIn) {
                        newList.add(filtered);
                    }
                }
            }

            viewModel.setSelectedEmployees(newList);
        });
    }
    private void updateSelectionAllText() {
        if (filteredEmployees.isEmpty()) {
            binding.tvSelectAll.setText("Chọn tất cả");
            return;
        }

        boolean allSelected = true;

        for (ManagerEmployeeItem employee : filteredEmployees) {
            if (!selectedEmployeeIds.contains(employee.getUserId())) {
                allSelected = false;
                break;
            }
        }

        binding.tvSelectAll.setText(
                allSelected
                        ? "Hủy chọn tất cả"
                        : "Chọn tất cả"
        );
    }
    private void setupBottomBar() {
        updateBottomBarCount();

        binding.btnContinue.setOnClickListener(v -> {
            if (selectedEmployeeIds.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn ít nhất 1 nhân viên!", Toast.LENGTH_SHORT).show();
                return;
            }
            Navigation.findNavController(v).navigate(R.id.action_selectEmployeeFragment_to_assignShiftFragment);
        });
    }

    private void updateBottomBarCount() {
        int count = selectedEmployeeIds.size();
        binding.tvSelectedCount.setText("Đã chọn " + count + " nhân viên");
        binding.btnContinue.setEnabled(count > 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
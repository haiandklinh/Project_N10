package com.example.attendanceapp.ui.manager.human;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentHumanTabBinding;
import com.example.attendanceapp.domain.model.EmployeeStatus;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@AndroidEntryPoint
public class HumanTabFragment extends Fragment {

    public static final String ARG_TAB_TYPE = "tab_type";
    public enum TabType { EMPLOYEES, DEPARTMENTS, POSITIONS }

    private FragmentHumanTabBinding binding;
    private ManagerHumanViewModel viewModel;
    private TabType tabType;

    private final CompositeDisposable disposables = new CompositeDisposable();

    private ManagerEmployeeAdapter employeeAdapter;
    private ManagerDepartmentAdapter departmentAdapter;
    private ManagerPositionAdapter positionAdapter;
    
    private final List<ManagerEmployeeItem> allEmployees = new ArrayList<>();
    private final List<String> allDepartments = new ArrayList<>();
    private final List<String> allPositions = new ArrayList<>();

    private EmployeeStatus currentStatusFilter = null;
    private String currentSearchQuery = "";

    public static HumanTabFragment newInstance(TabType type) {
        HumanTabFragment fragment = new HumanTabFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TAB_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabType = (TabType) getArguments().getSerializable(ARG_TAB_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHumanTabBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Dùng requireActivity() để chia sẻ chung ViewModel với Activity & các fragment khác
        viewModel = new ViewModelProvider(requireActivity()).get(ManagerHumanViewModel.class);

        setupUI();
        observeViewModel();
        loadData();
    }

    private void setupUI() {
        binding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        
        switch (tabType) {
            case EMPLOYEES:
                binding.scrollFilter.setVisibility(View.VISIBLE);
                binding.btnAddAction.setVisibility(View.VISIBLE);
                binding.btnAddAction.setText(R.string.human_invite_employee);
                binding.btnAddAction.setOnClickListener(v -> showCompanyInvitationBottomSheet());
                binding.layoutEmpty.getRoot().setOnClickListener(v -> showCompanyInvitationBottomSheet());
                setupEmployeeTab();
                break;
            case DEPARTMENTS:
                binding.btnAddAction.setVisibility(View.VISIBLE);
                binding.btnAddAction.setText(R.string.human_add_department);
                setupDepartmentTab();
                break;
            case POSITIONS:
                binding.btnAddAction.setVisibility(View.VISIBLE);
                binding.btnAddAction.setText(R.string.human_add_position);
                setupPositionTab();
                break;
        }

        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadData();
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    private void setupEmployeeTab() {
        employeeAdapter = new ManagerEmployeeAdapter(new ArrayList<>(), employee ->
                EmployeeDetailBottomSheet.newInstance(employee).show(getChildFragmentManager(), "employee_detail"));
        binding.rvList.setAdapter(employeeAdapter);

        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_all)) currentStatusFilter = null;
            else if (checkedIds.contains(R.id.chip_active)) currentStatusFilter = EmployeeStatus.ACTIVE;
            else if (checkedIds.contains(R.id.chip_on_leave)) currentStatusFilter = EmployeeStatus.ON_LEAVE;
            else if (checkedIds.contains(R.id.chip_terminated)) currentStatusFilter = EmployeeStatus.TERMINATED;
            
            applyFilters();
        });
    }

    private void setupDepartmentTab() {
        departmentAdapter = new ManagerDepartmentAdapter(new ArrayList<>(), new ManagerDepartmentAdapter.OnActionListener() {
            @Override public void onEdit(String id) {}
            @Override public void onDelete(String id) {}
        });
        binding.rvList.setAdapter(departmentAdapter);
    }

    private void setupPositionTab() {
        positionAdapter = new ManagerPositionAdapter(new ArrayList<>(), new ManagerPositionAdapter.OnActionListener() {
            @Override public void onEdit(String id) {}
            @Override public void onDelete(String id) {}
        });
        binding.rvList.setAdapter(positionAdapter);
    }

    private void observeViewModel() {
        switch (tabType) {
            case EMPLOYEES:
                viewModel.employeeSearchQuery.observe(getViewLifecycleOwner(), query -> {
                    currentSearchQuery = query.toLowerCase(Locale.ROOT).trim();
                    applyFilters();
                });
                viewModel.employees.observe(getViewLifecycleOwner(), list -> {
                    allEmployees.clear();
                    if (list != null) {
                        allEmployees.addAll(list);
                    }
                    updateFilterChipCounts();
                    applyFilters();
                });
                break;
            case DEPARTMENTS:
                viewModel.departmentSearchQuery.observe(getViewLifecycleOwner(), query -> {
                    currentSearchQuery = query.toLowerCase(Locale.ROOT).trim();
                    applyFilters();
                });
                break;
            case POSITIONS:
                viewModel.positionSearchQuery.observe(getViewLifecycleOwner(), query -> {
                    currentSearchQuery = query.toLowerCase(Locale.ROOT).trim();
                    applyFilters();
                });
                break;
        }
    }

    private void loadData() {
        binding.progressBar.setVisibility(View.GONE);
        
        if (tabType == TabType.EMPLOYEES) {
            viewModel.loadStaffs();
        } else if (tabType == TabType.DEPARTMENTS) {
            allDepartments.clear();
            applyFilters();
        } else if (tabType == TabType.POSITIONS) {
            allPositions.clear();
            applyFilters();
        }
    }

    private void updateFilterChipCounts() {
        int active = 0, onLeave = 0, terminated = 0;
        for (ManagerEmployeeItem e : allEmployees) {
            if (e.getStatus() == EmployeeStatus.ACTIVE) active++;
            else if (e.getStatus() == EmployeeStatus.ON_LEAVE) onLeave++;
            else if (e.getStatus() == EmployeeStatus.TERMINATED) terminated++;
        }
        
        binding.chipAll.setText(String.format(getString(R.string.human_filter_all), allEmployees.size()));
        binding.chipActive.setText(String.format(getString(R.string.human_filter_active), active));
        binding.chipOnLeave.setText(String.format(getString(R.string.human_filter_on_leave), onLeave));
        binding.chipTerminated.setText(String.format(getString(R.string.human_filter_terminated), terminated));
    }

    private void applyFilters() {
        if (tabType == TabType.EMPLOYEES) {
            List<ManagerEmployeeItem> filtered = new ArrayList<>();
            for (ManagerEmployeeItem e : allEmployees) {
                boolean matchesStatus = currentStatusFilter == null || e.getStatus() == currentStatusFilter;
                boolean matchesSearch = currentSearchQuery.isEmpty()
                        || e.getFullName().toLowerCase(Locale.ROOT).contains(currentSearchQuery)
                        || e.getEmployeeCode().toLowerCase(Locale.ROOT).contains(currentSearchQuery);

                if (matchesStatus && matchesSearch) {
                    filtered.add(e);
                }
            }
            employeeAdapter.updateData(filtered);
            binding.tvTotalCount.setText(String.format(getString(R.string.human_total_count), filtered.size()));
            showEmptyState(filtered.isEmpty());
        } else if (tabType == TabType.DEPARTMENTS) {
            List<String> filtered = new ArrayList<>();
            for (String d : allDepartments) {
                if (currentSearchQuery.isEmpty() || d.toLowerCase(Locale.ROOT).contains(currentSearchQuery)) {
                    filtered.add(d);
                }
            }
            departmentAdapter.updateData(filtered);
            String countText = String.format(getString(R.string.human_total_count), filtered.size()).replace("nhân viên", "phòng ban");
            binding.tvTotalCount.setText(countText);
            showEmptyState(filtered.isEmpty());
        } else if (tabType == TabType.POSITIONS) {
            List<String> filtered = new ArrayList<>();
            for (String p : allPositions) {
                if (currentSearchQuery.isEmpty() || p.toLowerCase(Locale.ROOT).contains(currentSearchQuery)) {
                    filtered.add(p);
                }
            }
            positionAdapter.updateData(filtered);
            String countText = String.format(getString(R.string.human_total_count), filtered.size()).replace("nhân viên", "chức vụ");
            binding.tvTotalCount.setText(countText);
            showEmptyState(filtered.isEmpty());
        }
    }

    private void showEmptyState(boolean isEmpty) {
        if (binding == null) return;
        binding.rvList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.layoutEmpty.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        if (isEmpty) {
            if (tabType == TabType.EMPLOYEES) {
                binding.layoutEmpty.tvEmptyTitle.setText("Chưa có nhân sự nào");
                binding.layoutEmpty.tvEmptySubtitle.setText("Công ty chưa có nhân sự nào. Bấm vào nút Thêm nhân sự để tạo mã mời.");
            } else if (tabType == TabType.DEPARTMENTS) {
                binding.layoutEmpty.tvEmptyTitle.setText("Chưa có phòng ban");
                binding.layoutEmpty.tvEmptySubtitle.setText("Công ty chưa thiết lập phòng ban nào.");
            } else if (tabType == TabType.POSITIONS) {
                binding.layoutEmpty.tvEmptyTitle.setText("Chưa có chức vụ");
                binding.layoutEmpty.tvEmptySubtitle.setText("Công ty chưa thiết lập chức vụ nào.");
            }
        }
    }

    private void showCompanyInvitationBottomSheet() {
        CompanyInvitationBottomSheet sheet = CompanyInvitationBottomSheet.newInstance();
        sheet.show(getChildFragmentManager(), CompanyInvitationBottomSheet.TAG);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (tabType == TabType.EMPLOYEES) {
            loadData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }
}

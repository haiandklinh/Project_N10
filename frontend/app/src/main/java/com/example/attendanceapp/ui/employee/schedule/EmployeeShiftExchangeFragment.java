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

import com.example.attendanceapp.data.dto.request.staff.ChangeShiftRequest;
import com.example.attendanceapp.data.dto.response.staff.StaffWorkScheduleResponse;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.databinding.FragmentShiftExchangeBinding;
import com.example.attendanceapp.domain.model.Shift;
import com.example.attendanceapp.domain.model.ShiftOption;
import com.example.attendanceapp.domain.model.ShiftStatus;
import com.example.attendanceapp.domain.repository.ShiftRepository;
import com.example.attendanceapp.domain.repository.StaffRepository;
import com.example.attendanceapp.ui.employee.schedule.shift.EmployeeUpcomingShiftAdapter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class EmployeeShiftExchangeFragment extends Fragment {

    private FragmentShiftExchangeBinding binding;
    private EmployeeUpcomingShiftAdapter myShiftAdapter;
    private EmployeeUpcomingShiftAdapter availableShiftAdapter;

    @Inject
    StaffRepository staffRepository;

    @Inject
    ShiftRepository shiftRepository;

    @Inject
    SharedPreferenceApp sharedPreferenceApp;

    private final CompositeDisposable disposables = new CompositeDisposable();

    // QUẢN LÝ HẠN MỨC ĐỔI CA (4 lần / tháng)
    private static final int MAX_EXCHANGE_PER_MONTH = 4;
    private int usedExchangeCount = 0;
    private YearMonth currentMonth;

    // Tiền chọn ca khi navigate từ màn hình Home / Schedule
    @Nullable
    private String preselectedShiftId = null;
    @Nullable
    private String preselectedShiftDate = null;

    private final List<Shift> myAllUpcomingShifts = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentShiftExchangeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentMonth = YearMonth.now();
        if (sharedPreferenceApp != null) {
            usedExchangeCount = sharedPreferenceApp.getMonthlyShiftExchangeCount(currentMonth.toString());
        }

        if (getArguments() != null) {
            preselectedShiftId = getArguments().getString("selected_shift_id", null);
            preselectedShiftDate = getArguments().getString("selected_shift_date", null);
        }

        setupToolbar();
        setupQuotaDisplay();
        setupSwipeRefresh();
        setupRecyclerViews();
        setupSubmitButton();
        loadMyShiftsData();
    }

    private void setupToolbar() {
        if (getParentFragment() instanceof com.example.attendanceapp.ui.employee.schedule.leave.EmployeeCreateRequestContainerFragment) {
            binding.toolbarApp.setVisibility(View.GONE);
        } else {
            binding.toolbarApp.setVisibility(View.VISIBLE);
            binding.toolbarApp.setTitle("Yêu cầu đổi ca");
            binding.toolbarApp.setOnBackClickListener(() -> {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            });
        }
    }

    private void setupQuotaDisplay() {
        int remaining = Math.max(0, MAX_EXCHANGE_PER_MONTH - usedExchangeCount);
        binding.tvQuotaStatus.setText("Hạn mức tháng này: Bạn còn " + remaining + "/" + MAX_EXCHANGE_PER_MONTH + " lần xin đổi ca.");
        binding.progressQuota.setMax(MAX_EXCHANGE_PER_MONTH);
        binding.progressQuota.setProgress(usedExchangeCount);

        if (remaining <= 0) {
            binding.btnSubmitExchange.setEnabled(false);
            binding.btnSubmitExchange.setText("Đã hết hạn mức đổi ca");
        } else {
            binding.btnSubmitExchange.setEnabled(true);
            binding.btnSubmitExchange.setText("Gửi yêu cầu đổi ca");
        }
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setColorSchemeColors(
                com.google.android.material.color.MaterialColors.getColor(
                        binding.swipeRefreshLayout,
                        com.example.attendanceapp.R.attr.colorPrimary
                )
        );
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadMyShiftsData);
    }

    private void setupRecyclerViews() {
        // RecyclerView 1: Ca của tôi (Chọn ca muốn nhường)
        myShiftAdapter = new EmployeeUpcomingShiftAdapter();
        binding.rvMyShifts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMyShifts.setAdapter(myShiftAdapter);

        // Lắng nghe khi người dùng chọn ca của mình -> lọc ca trống phù hợp từ hệ thống theo API thật
        myShiftAdapter.setOnItemClickListener((shift, position) -> {
            loadAvailableShiftsForExchange(shift);
        });

        // RecyclerView 2: Ca khả dụng trong hệ thống (Chọn ca muốn nhận)
        availableShiftAdapter = new EmployeeUpcomingShiftAdapter();
        binding.rvAvailableShifts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAvailableShifts.setAdapter(availableShiftAdapter);
    }

    /**
     * Tải danh sách ca làm việc sắp tới của nhân viên từ API thật (StaffRepository.getWorkSchedules).
     * Chỉ lấy các ca từ hôm nay trở đi (chưa qua giờ làm).
     */
    private void loadMyShiftsData() {
        binding.progressMyShifts.setVisibility(View.VISIBLE);

        disposables.add(staffRepository.getWorkSchedules()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if (binding == null) return;
                            binding.swipeRefreshLayout.setRefreshing(false);
                            binding.progressMyShifts.setVisibility(View.GONE);

                            myAllUpcomingShifts.clear();
                            LocalDate today = LocalDate.now();
                            LocalTime now = LocalTime.now();

                            if (response != null && response.getData() != null) {
                                for (StaffWorkScheduleResponse item : response.getData()) {
                                    if (item == null || item.getShifts() == null) continue;

                                    LocalDate date = parseDate(item.getWorkDay());
                                    if (date == null) continue;

                                    for (StaffWorkScheduleResponse.ShiftInfo si : item.getShifts()) {
                                        if (si == null) continue;

                                        LocalTime st = parseTime(si.getStartTime(), LocalTime.of(8, 0));
                                        LocalTime et = parseTime(si.getEndTime(), LocalTime.of(17, 0));

                                        // Chỉ lấy ca chưa kết thúc (ngày tương lai hoặc hôm nay nhưng chưa hết ca)
                                        boolean isUpcoming = date.isAfter(today) || (date.isEqual(today) && (et == null || et.isAfter(now)));
                                        if (!isUpcoming) continue;

                                        String shiftName = (si.getShiftName() != null && !si.getShiftName().trim().isEmpty())
                                                ? si.getShiftName().trim()
                                                : "Ca làm việc";

                                        // Ưu tiên id_shift từ server
                                        String realShiftId = (si.getShiftId() != null && !si.getShiftId().trim().isEmpty())
                                                ? si.getShiftId().trim()
                                                : (si.getWorkScheduleId() != null ? si.getWorkScheduleId().trim() : "");

                                        String location = resolveShiftLocation(shiftName);

                                        myAllUpcomingShifts.add(new Shift(
                                                realShiftId,
                                                "current_user",
                                                date,
                                                shiftName,
                                                st,
                                                et,
                                                location,
                                                ShiftStatus.PENDING
                                        ));
                                    }
                                }
                            }

                            // Sắp xếp ca sắp tới theo thứ tự thời gian gần nhất lên đầu
                            myAllUpcomingShifts.sort(Comparator.comparing(
                                    Shift::getDate,
                                    Comparator.nullsLast(Comparator.naturalOrder())
                            ).thenComparing(
                                    Shift::getStartTime,
                                    Comparator.nullsLast(Comparator.naturalOrder())
                            ));

                            if (myAllUpcomingShifts.isEmpty()) {
                                binding.rvMyShifts.setVisibility(View.GONE);
                                binding.tvEmptyMyShifts.setVisibility(View.VISIBLE);
                                binding.tvEmptyMyShifts.setText("Bạn hiện không có ca làm việc sắp tới nào để đổi.");

                                // Ẩn và xoá danh sách ca khả dụng
                                availableShiftAdapter.submitList(new ArrayList<>());
                                binding.rvAvailableShifts.setVisibility(View.GONE);
                                binding.tvEmptyAvailableShifts.setVisibility(View.VISIBLE);
                                binding.tvEmptyAvailableShifts.setText("Vui lòng có ca làm việc sắp tới để thực hiện đổi ca.");

                                binding.btnSubmitExchange.setEnabled(false);
                            } else {
                                binding.rvMyShifts.setVisibility(View.VISIBLE);
                                binding.tvEmptyMyShifts.setVisibility(View.GONE);
                                myShiftAdapter.submitList(new ArrayList<>(myAllUpcomingShifts));

                                if (usedExchangeCount < MAX_EXCHANGE_PER_MONTH) {
                                    binding.btnSubmitExchange.setEnabled(true);
                                }

                                // Tự động chọn ca (nếu có preselected từ arguments hoặc chọn ca đầu tiên)
                                int selectIndex = 0;
                                if (preselectedShiftId != null) {
                                    for (int i = 0; i < myAllUpcomingShifts.size(); i++) {
                                        Shift s = myAllUpcomingShifts.get(i);
                                        if (preselectedShiftId.equals(s.getId())) {
                                            selectIndex = i;
                                            break;
                                        }
                                    }
                                } else if (preselectedShiftDate != null) {
                                    for (int i = 0; i < myAllUpcomingShifts.size(); i++) {
                                        Shift s = myAllUpcomingShifts.get(i);
                                        if (s.getDate() != null && preselectedShiftDate.equals(s.getDate().toString())) {
                                            selectIndex = i;
                                            break;
                                        }
                                    }
                                }

                                myShiftAdapter.setSelectedPosition(selectIndex);
                                loadAvailableShiftsForExchange(myAllUpcomingShifts.get(selectIndex));
                            }
                        },
                        throwable -> {
                            if (binding == null) return;
                            binding.swipeRefreshLayout.setRefreshing(false);
                            binding.progressMyShifts.setVisibility(View.GONE);

                            if (myAllUpcomingShifts.isEmpty()) {
                                binding.rvMyShifts.setVisibility(View.GONE);
                                binding.tvEmptyMyShifts.setVisibility(View.VISIBLE);
                                binding.tvEmptyMyShifts.setText("Không thể tải danh sách ca: " + throwable.getMessage());
                            }
                            Toast.makeText(requireContext(), "Lỗi tải lịch: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                )
        );
    }

    /**
     * Tải danh sách ca làm việc thực tế của công ty từ API thật (ShiftRepository.getShifts)
     * và lọc bỏ ca mà nhân viên đang làm trong ngày đó.
     */
    private void loadAvailableShiftsForExchange(Shift myShift) {
        if (myShift == null) return;
        LocalDate targetDate = myShift.getDate() != null ? myShift.getDate() : LocalDate.now();
        String currentShiftId = myShift.getId();
        String currentShiftName = myShift.getShiftName();

        // Thu thập các ca mà nhân viên đã được xếp lịch trong ngày này để tránh trùng
        Set<String> alreadyAssignedShiftNamesOnDay = new HashSet<>();
        Set<String> alreadyAssignedShiftIdsOnDay = new HashSet<>();
        for (Shift s : myAllUpcomingShifts) {
            if (s.getDate() != null && s.getDate().equals(targetDate)) {
                if (s.getShiftName() != null) alreadyAssignedShiftNamesOnDay.add(s.getShiftName().trim().toLowerCase());
                if (s.getId() != null) alreadyAssignedShiftIdsOnDay.add(s.getId().trim().toLowerCase());
            }
        }

        binding.progressAvailableShifts.setVisibility(View.VISIBLE);
        binding.rvAvailableShifts.setVisibility(View.GONE);
        binding.tvEmptyAvailableShifts.setVisibility(View.GONE);

        disposables.add(shiftRepository.getShifts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        shiftOptions -> {
                            if (binding == null) return;
                            binding.progressAvailableShifts.setVisibility(View.GONE);

                            List<Shift> availableShifts = new ArrayList<>();
                            if (shiftOptions != null) {
                                for (ShiftOption option : shiftOptions) {
                                    if (option == null) continue;

                                    String optId = option.getId() != null ? option.getId().trim() : "";
                                    String optTitle = option.getTitle() != null ? option.getTitle().trim() : "";

                                    // Loại trừ ca hiện tại đang chọn
                                    if (!optId.isEmpty() && optId.equalsIgnoreCase(currentShiftId)) {
                                        continue;
                                    }
                                    if (!optTitle.isEmpty() && optTitle.equalsIgnoreCase(currentShiftName)) {
                                        continue;
                                    }

                                    // Loại trừ các ca khác mà nhân viên cũng đã có lịch trong ngày đó
                                    if (!optId.isEmpty() && alreadyAssignedShiftIdsOnDay.contains(optId.toLowerCase())) {
                                        continue;
                                    }
                                    if (!optTitle.isEmpty() && alreadyAssignedShiftNamesOnDay.contains(optTitle.toLowerCase())) {
                                        continue;
                                    }

                                    LocalTime st = parseTime(option.getStartTime(), LocalTime.of(8, 0));
                                    LocalTime et = parseTime(option.getEndTime(), LocalTime.of(17, 0));

                                    String location = resolveShiftLocation(optTitle);

                                    availableShifts.add(new Shift(
                                            optId,
                                            "available_pool",
                                            targetDate,
                                            !optTitle.isEmpty() ? optTitle : "Ca làm việc",
                                            st,
                                            et,
                                            location,
                                            ShiftStatus.PENDING
                                    ));
                                }
                            }

                            // Sắp xếp các ca khả dụng theo giờ bắt đầu
                            availableShifts.sort(Comparator.comparing(
                                    Shift::getStartTime,
                                    Comparator.nullsLast(Comparator.naturalOrder())
                            ));

                            if (!availableShifts.isEmpty()) {
                                availableShiftAdapter.submitList(availableShifts);
                                binding.rvAvailableShifts.setVisibility(View.VISIBLE);
                                binding.tvEmptyAvailableShifts.setVisibility(View.GONE);
                                availableShiftAdapter.setSelectedPosition(0);
                            } else {
                                availableShiftAdapter.submitList(new ArrayList<>());
                                binding.rvAvailableShifts.setVisibility(View.GONE);
                                binding.tvEmptyAvailableShifts.setVisibility(View.VISIBLE);
                                String dateDisplay = targetDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                binding.tvEmptyAvailableShifts.setText("Không có ca làm việc nào khác khả dụng để đổi trong ngày " + dateDisplay + ".");
                            }
                        },
                        throwable -> {
                            if (binding == null) return;
                            binding.progressAvailableShifts.setVisibility(View.GONE);
                            binding.rvAvailableShifts.setVisibility(View.GONE);
                            binding.tvEmptyAvailableShifts.setVisibility(View.VISIBLE);
                            binding.tvEmptyAvailableShifts.setText("Không thể tải danh sách ca từ hệ thống: " + throwable.getMessage());
                        }
                )
        );
    }

    private void setupSubmitButton() {
        binding.btnSubmitExchange.setOnClickListener(v -> {
            if (usedExchangeCount >= MAX_EXCHANGE_PER_MONTH) {
                Toast.makeText(requireContext(), "Bạn đã dùng hết lượt đổi ca của tháng này (" + MAX_EXCHANGE_PER_MONTH + " lần)!", Toast.LENGTH_LONG).show();
                return;
            }

            Shift mySelectedShift = myShiftAdapter.getSelectedShift();
            if (mySelectedShift == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn ca của bạn cần đổi", Toast.LENGTH_SHORT).show();
                return;
            }

            Shift targetShift = availableShiftAdapter.getSelectedShift();
            if (targetShift == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn ca khả dụng muốn đổi sang", Toast.LENGTH_SHORT).show();
                return;
            }

            String reason = binding.etReason.getText() != null ? binding.etReason.getText().toString().trim() : "";
            if (reason.isEmpty()) {
                binding.tilReason.setError("Vui lòng nhập lý do đổi ca");
                return;
            } else {
                binding.tilReason.setError(null);
            }

            binding.btnSubmitExchange.setEnabled(false);
            binding.btnSubmitExchange.setText("Đang gửi yêu cầu...");

            String curShiftId = mySelectedShift.getId();
            String tgtShiftId = targetShift.getId();
            String dateStr = mySelectedShift.getDate() != null ? mySelectedShift.getDate().toString() : LocalDate.now().toString();

            ChangeShiftRequest req = new ChangeShiftRequest(curShiftId, tgtShiftId, dateStr, reason);

            disposables.add(staffRepository.requestChangeShift(req)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            () -> {
                                if (isAdded()) {
                                    if (sharedPreferenceApp != null) {
                                        sharedPreferenceApp.incrementMonthlyShiftExchangeCount(currentMonth.toString());
                                    }
                                    usedExchangeCount++;
                                    setupQuotaDisplay();
                                    Toast.makeText(requireContext(), "Gửi yêu cầu đổi sang " + targetShift.getShiftName() + " thành công!", Toast.LENGTH_SHORT).show();
                                    Navigation.findNavController(requireView()).popBackStack();
                                }
                            },
                            throwable -> {
                                if (isAdded()) {
                                    binding.btnSubmitExchange.setEnabled(true);
                                    binding.btnSubmitExchange.setText("Gửi yêu cầu đổi ca");
                                    Toast.makeText(requireContext(), "Lỗi gửi yêu cầu: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                    )
            );
        });
    }

    private String resolveShiftLocation(String shiftTitle) {
        if (sharedPreferenceApp != null) {
            String addr = sharedPreferenceApp.getShiftAddress(shiftTitle);
            if (addr != null && !addr.trim().isEmpty()) {
                return addr.trim();
            }
            String latestAddr = sharedPreferenceApp.getLatestShiftAddress();
            if (latestAddr != null && !latestAddr.trim().isEmpty()) {
                return latestAddr.trim();
            }
            String compAddr = sharedPreferenceApp.getCompanyAddress();
            if (compAddr != null && !compAddr.trim().isEmpty()) {
                return compAddr.trim();
            }
        }
        return "Vị trí ca làm việc";
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (Exception e) {
            try {
                return LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception ex) {
                try {
                    return LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                } catch (Exception ignored) {}
            }
            return null;
        }
    }

    private LocalTime parseTime(String timeStr, LocalTime defaultTime) {
        if (timeStr == null || timeStr.trim().isEmpty()) return defaultTime;
        try {
            return LocalTime.parse(timeStr.trim(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            try {
                return LocalTime.parse(timeStr.trim(), DateTimeFormatter.ofPattern("HH:mm:ss"));
            } catch (Exception ex) {
                return defaultTime;
            }
        }
    }

    @Override
    public void onDestroyView() {
        disposables.clear();
        binding = null;
        super.onDestroyView();
    }
}

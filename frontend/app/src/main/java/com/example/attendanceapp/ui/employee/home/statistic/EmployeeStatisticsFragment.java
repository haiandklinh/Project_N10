package com.example.attendanceapp.ui.employee.home.statistic;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.FragmentStatisticsBinding;
import com.example.attendanceapp.databinding.LayoutStatisticBodyBinding;
import com.example.attendanceapp.databinding.LayoutStatisticChartDonutBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class EmployeeStatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private EmployeeStatAdapter employeeStatAdapter;

    // Lưu trữ màu dynamic được resolve từ Theme Attrs
    private int colorPrimary;
    private int colorPrimaryContainer;
    private int colorOnSurfaceVariant;
    private int colorSurfaceVariant;

    private int colorPresent;
    private int colorPresentContainer;
    private int colorLate;
    private int colorLateContainer;
    private int colorAbsent;
    private int colorAbsentContainer;
    private int colorEarlyLeave;
    private int colorEarlyLeaveContainer;
    private int colorNotChecked;
    private int colorNotCheckedContainer;
    private int colorWeekend;

    private YearMonth currentMonth = YearMonth.now();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo và resolve toàn bộ bảng màu từ Theme Attrs qua ColorUtil
        initThemeColors();

        ViewCompat.setOnApplyWindowInsetsListener(binding.includeHeader.layoutHeader, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarHeight + getResources().getDimensionPixelSize(R.dimen.spacing_m),
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        setupToolbar();
        setupRecyclerView();
        setupStatCards();
        setupToggleListener();
        setupMonthNavigation();
        // Mặc định nạp chế độ Tuần
        loadWeekData();
    }

    /**
     * Tải dynamic các màu từ theme attributes thông qua ColorUtil
     */
    private void initThemeColors() {
        android.content.Context context = requireContext();

        colorPrimary = ColorUtil.getThemeColor(context, R.attr.colorPrimary);
        colorPrimaryContainer = ColorUtil.getThemeColor(context, R.attr.colorPrimaryContainer);
        colorOnSurfaceVariant = ColorUtil.getThemeColor(context, R.attr.colorOnSurfaceVariant);
        colorSurfaceVariant = ColorUtil.getThemeColor(context, R.attr.colorSurfaceVariant);

        colorPresent = ColorUtil.getThemeColor(context, R.attr.colorAttendancePresent);
        colorPresentContainer = ColorUtil.getThemeColor(context, R.attr.colorAttendancePresentContainer);

        colorLate = ColorUtil.getThemeColor(context, R.attr.colorAttendanceLate);
        colorLateContainer = ColorUtil.getThemeColor(context, R.attr.colorAttendanceLateContainer);

        colorAbsent = ColorUtil.getThemeColor(context, R.attr.colorAttendanceAbsent);
        colorAbsentContainer = ColorUtil.getThemeColor(context, R.attr.colorAttendanceAbsentContainer);

        colorEarlyLeave = ColorUtil.getThemeColor(context, R.attr.colorAttendanceEarlyLeave);
        colorEarlyLeaveContainer = ColorUtil.getThemeColor(context, R.attr.colorAttendanceEarlyLeaveContainer);

        colorNotChecked = ColorUtil.getThemeColor(context, R.attr.colorAttendanceNotChecked);
        colorNotCheckedContainer = ColorUtil.getThemeColor(context, R.attr.colorAttendanceNotCheckedContainer);

        colorWeekend = ColorUtil.getThemeColor(context, R.attr.colorAttendanceWeekend);
    }

    private void setupToolbar() {
        binding.includeHeader.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );
    }

    private void setupRecyclerView() {
        LayoutStatisticBodyBinding body = binding.includeBody;
        employeeStatAdapter = new EmployeeStatAdapter();

        body.rvStatCards.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        body.rvStatCards.setNestedScrollingEnabled(false);
        body.rvStatCards.setAdapter(employeeStatAdapter);
    }

    private void setupStatCards() {
        List<EmployeeStatItem> statList = new ArrayList<>();
        // Sử dụng trực tiếp các giá trị màu container & main color đã resolve từ theme
        statList.add(new EmployeeStatItem(R.drawable.ic_check, "9", "ngày", "Đúng giờ", colorPresentContainer, colorPresent));
        statList.add(new EmployeeStatItem(R.drawable.ic_check, "98.4", "giờ", "Tổng giờ", colorPrimaryContainer, colorPrimary));
        statList.add(new EmployeeStatItem(R.drawable.ic_warning, "3", "lần", "Đi muộn", colorLateContainer, colorLate));
        statList.add(new EmployeeStatItem(R.drawable.ic_close, "2", "ngày", "Vắng mặt", colorAbsentContainer, colorAbsent));
        statList.add(new EmployeeStatItem(R.drawable.ic_check, "1", "lần", "Về sớm", colorEarlyLeaveContainer, colorEarlyLeave));
        statList.add(new EmployeeStatItem(R.drawable.ic_remove, "1", "lần", "Thiếu checkout", colorNotCheckedContainer, colorNotChecked));

        employeeStatAdapter.setItems(statList);
    }

    private void setupToggleListener() {
        binding.includeHeader.toggleGroupTime.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_mode_week) {
                    loadWeekData();
                } else if (checkedId == R.id.btn_mode_month) {
                    loadMonthData();
                }
            }
        });
    }

    private void updateMonthLabel() {
        binding.includeHeader.tvHeaderSubtitle.setText(
                getString(R.string.statistic_month_format,
                        currentMonth.getMonthValue(), currentMonth.getYear()));
    }

    private void setupMonthNavigation() {
        updateMonthLabel();
        binding.includeHeader.btnPrevMonth.setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1);
            updateMonthLabel();
        });
        binding.includeHeader.btnNextMonth.setOnClickListener(v -> {
            if (currentMonth.isBefore(YearMonth.now())) {   // không cho xem tháng tương lai
                currentMonth = currentMonth.plusMonths(1);
                updateMonthLabel();
            }
        });
    }

    // ==================== CẤU HÌNH DỮ LIỆU TUẦN ====================
    private void loadWeekData() {
        LayoutStatisticBodyBinding body = binding.includeBody;

        body.layoutBarChart.tvChartTitle.setText("Giờ làm tuần này");
        body.layoutBarChart.tvChartSubtitle.setText("08/09 – 13/09");
        body.layoutDonutChart.tvDonutTitle.setText("Phân bố tình trạng (tuần)");

        setupBarChartWeek(body.layoutBarChart.barChart);
        setupDonutChart(body.layoutDonutChart, 6, 4, 1, 1, 0); // Tổng 6 ngày
    }

    // ==================== CẤU HÌNH DỮ LIỆU THÁNG ====================
    private void loadMonthData() {
        LayoutStatisticBodyBinding body = binding.includeBody;

        body.layoutBarChart.tvChartTitle.setText("Giờ làm theo tuần");
        body.layoutBarChart.tvChartSubtitle.setText("Tháng 9/2026");
        body.layoutDonutChart.tvDonutTitle.setText("Phân bố tình trạng (tháng)");

        setupBarChartMonth(body.layoutBarChart.barChart);
        setupDonutChart(body.layoutDonutChart, 22, 16, 3, 1, 2); // Tổng 22 ngày
    }

    private void setupBarChartWeek(BarChart chart) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 9.3f)); // T2 - Overtime
        entries.add(new BarEntry(1, 8.5f)); // T3 - Standard
        entries.add(new BarEntry(2, 9.3f)); // T4 - Overtime
        entries.add(new BarEntry(3, 0.0f)); // T5 - Nghỉ
        entries.add(new BarEntry(4, 9.0f)); // T6 - Overtime
        entries.add(new BarEntry(5, 4.5f)); // T7 - Under standard

        int[] colors = new int[]{
                colorPrimary,          // T2
                colorPrimaryContainer, // T3
                colorPrimary,          // T4
                colorWeekend,          // T5 (Nghỉ/Cuối tuần)
                colorPrimary,          // T6
                colorLate              // T7 (Thiếu giờ)
        };
        renderBarChart(chart, entries, colors, new String[]{"T2", "T3", "T4", "T5", "T6", "T7"});
    }

    private void setupBarChartMonth(BarChart chart) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 43.5f)); // Tuần 1
        entries.add(new BarEntry(1, 40.0f)); // Tuần 2
        entries.add(new BarEntry(2, 45.2f)); // Tuần 3
        entries.add(new BarEntry(3, 21.8f)); // Tuần 4

        int[] colors = new int[]{
                colorPrimary,          // T1
                colorPrimaryContainer, // T2
                colorPrimary,          // T3
                colorLate              // T4
        };
        renderBarChart(chart, entries, colors, new String[]{"T1", "T2", "T3", "T4"});
    }

    private void renderBarChart(BarChart chart, List<BarEntry> entries, int[] colors, String[] labels) {
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(colorOnSurfaceVariant);

        // Định dạng hiển thị số trên đầu cột (Ẩn giá trị = 0)
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value > 0 ? String.format("%.1fh", value) : "";
            }
        });

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.38f);

        chart.setData(data);
        configureChartAxes(chart, labels);
    }

    private void configureChartAxes(BarChart chart, String[] labels) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(false);
        chart.setFitBars(true);

        // Trục X
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(colorOnSurfaceVariant);
        xAxis.setTextSize(11f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        // Trục Y
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(colorSurfaceVariant);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getAxisLeft().setTextColor(colorOnSurfaceVariant);
        chart.getAxisLeft().setAxisMinimum(0f);

        chart.getAxisRight().setEnabled(false);
        chart.animateY(800);
        chart.invalidate();
    }

    private void setupDonutChart(LayoutStatisticChartDonutBinding donutBinding, int totalDays, int onTime, int late, int early, int absent) {
        PieChart chart = donutBinding.pieChart;

        // Cập nhật số liệu hiển thị
        donutBinding.tvCenterTotalDays.setText(String.valueOf(totalDays));
        donutBinding.tvCountOnTime.setText(String.valueOf(onTime));
        donutBinding.tvCountLate.setText(String.valueOf(late));
        donutBinding.tvCountEarly.setText(String.valueOf(early));
        donutBinding.tvCountAbsent.setText(String.valueOf(absent));

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if (onTime > 0) { entries.add(new PieEntry(onTime)); colors.add(colorPresent); }
        if (late > 0)   { entries.add(new PieEntry(late));   colors.add(colorLate); }
        if (early > 0)  { entries.add(new PieEntry(early));  colors.add(colorEarlyLeave); }
        if (absent > 0) { entries.add(new PieEntry(absent)); colors.add(colorAbsent); }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setDrawValues(false);
        dataSet.setSliceSpace(2.5f); // Khoảng trống giữa các lát cắt

        PieData data = new PieData(dataSet);
        chart.setData(data);

        // Cấu hình tỉ lệ Donut mỏng hiện đại
        chart.setHoleRadius(76f);
        chart.setTransparentCircleRadius(0f);
        chart.setHoleColor(Color.TRANSPARENT);

        // Khóa tương tác thừa & cấu hình chung
        chart.setTouchEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateXY(700, 700);
        chart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
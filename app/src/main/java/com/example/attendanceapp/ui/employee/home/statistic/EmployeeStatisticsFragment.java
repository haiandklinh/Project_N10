package com.example.attendanceapp.ui.employee.home.statistic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentStatisticsBinding;
import com.example.attendanceapp.databinding.ItemStatCardBinding;
import com.example.attendanceapp.databinding.LayoutStatisticBodyBinding;
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

import java.util.ArrayList;
import java.util.List;

public class EmployeeStatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private EmployeeStatAdapter employeeStatAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.includeHeader.layoutHeader, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

            // Giữ nguyên padding gốc từ XML, chỉ cộng thêm chiều cao Status Bar vào paddingTop
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarHeight + getResources().getDimensionPixelSize(R.dimen.spacing_m),
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        setupRecyclerView();
        setupStatCards();
        setupToggleListener();

        // Mặc định nạp chế độ Tuần
        loadWeekData();
    }

    private void setupRecyclerView() {
        LayoutStatisticBodyBinding body = binding.includeBody;
        employeeStatAdapter = new EmployeeStatAdapter();

        // Lưới 3 cột
        body.rvStatCards.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        body.rvStatCards.setNestedScrollingEnabled(false); // Tránh xung đột cuộn với NestedScrollView
        body.rvStatCards.setAdapter(employeeStatAdapter);
    }

    private void setupStatCards() {
        List<EmployeeStatItem> statList = new ArrayList<>();

        // 1. Ngày đúng giờ
        statList.add(new EmployeeStatItem(R.drawable.ic_check, "9", "ngày", "Đúng giờ", 0xFFE8F5E9, 0xFF2E7D32));
        // 2. Tổng giờ làm
        statList.add(new EmployeeStatItem(R.drawable.ic_check, "98.4", "giờ", "Tổng giờ", 0xFFF3E5F5, 0xFF7B1FA2));
        // 3. Lần đi muộn
        statList.add(new EmployeeStatItem(R.drawable.ic_warning, "3", "lần", "Đi muộn", 0xFFFFFDE7, 0xFFF57F17));
        // 4. Ngày vắng mặt
        statList.add(new EmployeeStatItem(R.drawable.ic_close, "2", "ngày", "Vắng mặt", 0xFFFFEBEE, 0xFFC62828));
        // 5. Lần về sớm
        statList.add(new EmployeeStatItem(R.drawable.ic_check, "1", "lần", "Về sớm", 0xFFFFF3E0, 0xFFE65100));
        // 6. Thiếu check-out
        statList.add(new EmployeeStatItem(R.drawable.ic_remove, "1", "lần", "Thiếu checkout", 0xFFEDE7F6, 0xFF512DA8));

        employeeStatAdapter.setItems(statList);
    }

    private void bindStatCard(ItemStatCardBinding card, int iconRes, String value, String unit, String label, int bgColor, int textColor) {
        card.cardRoot.setCardBackgroundColor(bgColor);
        card.ivStatIcon.setImageResource(iconRes);
        card.tvStatValue.setText(value);
        card.tvStatValue.setTextColor(textColor);
        card.tvStatUnit.setText(unit);
        card.tvStatUnit.setTextColor(textColor);
        card.tvStatLabel.setText(label);
    }

    private void setupToggleListener() {
        // Gọi nút Toggle qua sub-binding includeHeader
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

    // ==================== CẤU HÌNH DỮ LIỆU TUẦN ====================
    private void loadWeekData() {
        LayoutStatisticBodyBinding body = binding.includeBody;

        body.layoutBarChart.tvChartTitle.setText("Giờ làm tuần này");
        body.layoutBarChart.tvChartSubtitle.setText("08/09 – 13/09");
        body.layoutDonutChart.tvDonutTitle.setText("Phân bố tình trạng (tuần)");

        // Biểu đồ cột Tuần (T2 - T7)
        setupBarChartWeek(body.layoutBarChart.barChart);

        // Biểu đồ tròn Tuần
        setupDonutChart(body.layoutDonutChart.pieChart);
    }

    // ==================== CẤU HÌNH DỮ LIỆU THÁNG ====================
    private void loadMonthData() {
        LayoutStatisticBodyBinding body = binding.includeBody;

        body.layoutBarChart.tvChartTitle.setText("Giờ làm theo tuần");
        body.layoutBarChart.tvChartSubtitle.setText("Tháng 9/2026");
        body.layoutDonutChart.tvDonutTitle.setText("Phân bố tình trạng (tháng)");

        // Biểu đồ cột Tháng (T1 - T4)
        setupBarChartMonth(body.layoutBarChart.barChart);

        // Biểu đồ tròn Tháng
        setupDonutChart(body.layoutDonutChart.pieChart);
    }

    private void setupBarChartWeek(BarChart chart) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 9.3f)); // T2
        entries.add(new BarEntry(1, 8.5f)); // T3
        entries.add(new BarEntry(2, 9.3f)); // T4
        entries.add(new BarEntry(3, 0f));   // T5 (Nghỉ)
        entries.add(new BarEntry(4, 9.0f)); // T6
        entries.add(new BarEntry(5, 4.5f)); // T7

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(new int[]{0xFF283593, 0xFF1E88E5, 0xFF283593, 0xFFE0E0E0, 0xFF283593, 0xFFFDD835});
        dataSet.setValueTextSize(11f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        chart.setData(data);
        configureChartAxes(chart, new String[]{"T2", "T3", "T4", "T5", "T6", "T7"});
    }

    private void setupBarChartMonth(BarChart chart) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 43.5f)); // T1
        entries.add(new BarEntry(1, 40.0f)); // T2
        entries.add(new BarEntry(2, 45.2f)); // T3
        entries.add(new BarEntry(3, 21.8f)); // T4

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(new int[]{0xFF283593, 0xFF283593, 0xFF283593, 0xFF90CAF9});
        dataSet.setValueTextSize(11f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        chart.setData(data);
        configureChartAxes(chart, new String[]{"T1", "T2", "T3", "T4"});
    }

    private void configureChartAxes(BarChart chart, String[] labels) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawGridBackground(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
        chart.invalidate();
    }

    private void setupDonutChart(PieChart chart) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(8f)); // Đúng giờ
        entries.add(new PieEntry(3f)); // Đi muộn
        entries.add(new PieEntry(1f)); // Về sớm
        entries.add(new PieEntry(2f)); // Vắng mặt

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{0xFF00BFA5, 0xFFFF9800, 0xFFFF5722, 0xFFF44336});
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.setHoleRadius(75f);
        chart.setTransparentCircleRadius(0f);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
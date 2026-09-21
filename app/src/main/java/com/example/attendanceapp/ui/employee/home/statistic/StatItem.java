package com.example.attendanceapp.ui.employee.home.statistic;

public class StatItem {
    private final int iconRes;
    private final String value;
    private final String unit;
    private final String label;
    private final int bgColor;
    private final int textColor;

    public StatItem(int iconRes, String value, String unit, String label, int bgColor, int textColor) {
        this.iconRes = iconRes;
        this.value = value;
        this.unit = unit;
        this.label = label;
        this.bgColor = bgColor;
        this.textColor = textColor;
    }

    public int getIconRes() { return iconRes; }
    public String getValue() { return value; }
    public String getUnit() { return unit; }
    public String getLabel() { return label; }
    public int getBgColor() { return bgColor; }
    public int getTextColor() { return textColor; }
}
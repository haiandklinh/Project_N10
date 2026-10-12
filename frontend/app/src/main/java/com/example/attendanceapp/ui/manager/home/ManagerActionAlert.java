package com.example.attendanceapp.ui.manager.home;

public class ManagerActionAlert {
    private String title;
    private String description;
    private int iconRes;
    private int iconColorAttr;
    private int backgroundColorAttr;
    private Runnable action;

    public ManagerActionAlert(String title, String description, int iconRes, int iconColorAttr, int backgroundColorAttr, Runnable action) {
        this.title = title;
        this.description = description;
        this.iconRes = iconRes;
        this.iconColorAttr = iconColorAttr;
        this.action = action;
        this.backgroundColorAttr = backgroundColorAttr;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getIconRes() { return iconRes; }
    public int getIconColorAttr() { return iconColorAttr; }
    public Runnable getAction() { return action; }
    public int getBackgroundColorAttr() { return backgroundColorAttr; }
}
package com.example.attendanceapp.core.utils;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;

public class ColorUtil {

    public static int getThemeColor(Context context, int attrRes) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrRes, typedValue, true);
        return typedValue.data;
    }

    // Hàm tiện ích truyền thẳng View vào
    public static int getThemeColor(View view, int attrRes) {
        return getThemeColor(view.getContext(), attrRes);
    }
}
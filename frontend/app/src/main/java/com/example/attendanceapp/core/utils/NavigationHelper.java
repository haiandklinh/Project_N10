package com.example.attendanceapp.core.utils;

import androidx.navigation.NavOptions;
import com.example.attendanceapp.R;

public class NavigationHelper {

    /**
     * Tạo NavOptions mặc định với hiệu ứng trượt (slide transition) cho toàn bộ ứng dụng.
     */
    public static NavOptions slideNavOptions() {
        return new NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
                .build();
    }

    /**
     * Tạo NavOptions chuẩn cho Bottom Navigation (chuyển tab tức thì không animation giống YouTube/Instagram,
     * đồng thời duy trì trạng thái các tab: SingleTop, RestoreState, PopUpTo).
     */
    public static NavOptions bottomNavOptions(int startDestinationId) {
        return new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(
                        startDestinationId,
                        false,
                        true
                )
                .build();
    }
}

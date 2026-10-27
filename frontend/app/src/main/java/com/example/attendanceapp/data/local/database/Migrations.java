package com.example.attendanceapp.data.local.database;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Quy tắc: mỗi lần tăng version trong @Database thì thêm đúng 1 Migration (n -> n+1)
 * và đăng ký vào ALL. KHÔNG sửa Migration đã phát hành.
 *
 * SQL phải khớp 100% với entity (tên cột, kiểu, NOT NULL, DEFAULT, index),
 * nếu không Room sẽ crash với lỗi "Migration didn't properly handle...".
 */
public final class Migrations {

    private Migrations() {
    }

    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE `shifts` ADD COLUMN `allowedCheckInTime` TEXT");
            db.execSQL("ALTER TABLE `shifts` ADD COLUMN `checkInRadius` REAL");
        }
    };

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE `notifications` ADD COLUMN `role` TEXT");
        }
    };

    /**
     * Thêm migration mới vào đây theo thứ tự. Ví dụ:
     * public static final Migration[] ALL = { MIGRATION_1_2, MIGRATION_2_3 };
     */
    public static final Migration[] ALL = {
            MIGRATION_4_5,
            MIGRATION_5_6
    };
}

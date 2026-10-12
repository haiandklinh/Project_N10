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

    // ===== Ví dụ 1: thêm cột =====
    // Entity: @ColumnInfo(name = "phone") private String phone;   (nullable)
    // public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    //     @Override
    //     public void migrate(@NonNull SupportSQLiteDatabase db) {
    //         db.execSQL("ALTER TABLE users ADD COLUMN phone TEXT");
    //     }
    // };
    //
    // Cột NOT NULL thì bắt buộc có DEFAULT, và entity cũng phải khai báo defaultValue:
    //   db.execSQL("ALTER TABLE users ADD COLUMN status INTEGER NOT NULL DEFAULT 0");
    //   @ColumnInfo(name = "status", defaultValue = "0") private int status;

    // ===== Ví dụ 2: thêm bảng mới =====
    // public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
    //     @Override
    //     public void migrate(@NonNull SupportSQLiteDatabase db) {
    //         db.execSQL("CREATE TABLE IF NOT EXISTS `leave_requests` ("
    //                 + "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
    //                 + "`user_id` INTEGER NOT NULL, "
    //                 + "`reason` TEXT, "
    //                 + "FOREIGN KEY(`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE)");
    //         db.execSQL("CREATE INDEX IF NOT EXISTS `index_leave_requests_user_id` "
    //                 + "ON `leave_requests` (`user_id`)");
    //     }
    // };

    // ===== Ví dụ 3: thêm index =====
    //   db.execSQL("CREATE INDEX IF NOT EXISTS `index_shifts_user_id` ON `shifts` (`user_id`)");

    // ===== Ví dụ 4: đổi tên / xóa / đổi kiểu cột (tạo lại bảng) =====
    // SQLite không hỗ trợ đầy đủ, nên làm theo 4 bước:
    // public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
    //     @Override
    //     public void migrate(@NonNull SupportSQLiteDatabase db) {
    //         db.execSQL("CREATE TABLE `users_new` ("
    //                 + "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
    //                 + "`full_name` TEXT NOT NULL)");              // cấu trúc MỚI
    //         db.execSQL("INSERT INTO `users_new` (`id`, `full_name`) "
    //                 + "SELECT `id`, `name` FROM `users`");        // copy dữ liệu
    //         db.execSQL("DROP TABLE `users`");
    //         db.execSQL("ALTER TABLE `users_new` RENAME TO `users`");
    //         // nhớ tạo lại index nếu bảng cũ có
    //     }
    // };

    /**
     * Thêm migration mới vào đây theo thứ tự. Ví dụ:
     * public static final Migration[] ALL = { MIGRATION_1_2, MIGRATION_2_3 };
     */
    public static final Migration[] ALL = {
    };
}
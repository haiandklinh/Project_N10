package com.example.attendanceapp.domain.usecase.schedule;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.example.attendanceapp.domain.model.ManagerEmployeeItem;
import com.example.attendanceapp.domain.model.ShiftOption;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ExportScheduleUseCase {

    private final Context context;

    @Inject
    public ExportScheduleUseCase(Context context) {
        this.context = context;
    }

    public Single<Uri> execute(List<ManagerEmployeeItem> employees, ShiftOption shift, LocalDate date) {
        return Single.fromCallable(() -> {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Lịch xếp ca");

            // Tạo Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"STT", "Mã NV", "Họ và Tên", "Chức vụ", "Ngày", "Ca làm việc", "Thời gian"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Điền dữ liệu
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dateStr = date.format(dateFormatter);
            
            for (int i = 0; i < employees.size(); i++) {
                ManagerEmployeeItem emp = employees.get(i);
                Row row = sheet.createRow(i + 1);

                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(emp.getEmployeeCode());
                row.createCell(2).setCellValue(emp.getFullName());
                row.createCell(3).setCellValue(emp.getPosition());
                row.createCell(4).setCellValue(dateStr);
                row.createCell(5).setCellValue(shift.getTitle());
                row.createCell(6).setCellValue(shift.getTimeRange());
            }

            // Lưu file vào cache directory của app để dễ dàng chia sẻ qua FileProvider
            String fileName = "XepCa_" + System.currentTimeMillis() + ".xlsx";
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
                workbook.close();
            } catch (IOException e) {
                throw new RuntimeException("Lỗi lưu file: " + e.getMessage());
            }

            return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        }).subscribeOn(Schedulers.io());
    }
}

package com.example.attendanceapp.core.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    public static File getFileFromUri(Context context, Uri uri) throws IOException {
        if (context == null || uri == null) {
            throw new IOException("Context or Uri cannot be null");
        }
        String fileName = getFileName(context, uri);
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "file_" + System.currentTimeMillis();
        }
        // Sanitize file name to avoid path traversal
        fileName = new File(fileName).getName();

        File tempFile = new File(context.getCacheDir(), fileName);
        if (!tempFile.exists()) {
            tempFile.createNewFile();
        }

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            if (inputStream != null) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
            }
        }
        return tempFile;
    }

    private static String getFileName(Context context, Uri uri) {
        if (uri == null) return null;
        String result = null;
        String scheme = uri.getScheme();
        if ("content".equalsIgnoreCase(scheme) && context != null) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (result == null) {
            String path = uri.getPath();
            if (path != null) {
                int cut = path.lastIndexOf('/');
                if (cut != -1) {
                    result = path.substring(cut + 1);
                } else {
                    result = path;
                }
            }
        }
        if (result == null || result.trim().isEmpty()) {
            result = "file_" + System.currentTimeMillis();
        }
        
        // Ensure extension
        if (!result.contains(".") && context != null) {
            try {
                String mimeType = context.getContentResolver().getType(uri);
                if (mimeType != null) {
                    String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                    if (extension != null) {
                        result += "." + extension;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        
        return result;
    }
}

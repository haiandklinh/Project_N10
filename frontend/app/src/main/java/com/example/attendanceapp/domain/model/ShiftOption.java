package com.example.attendanceapp.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class ShiftOption implements Parcelable {
    private final String id;
    private final String title;
    private final String startTime; // "08:00"
    private final String endTime;   // "12:00"
    private final Double latitude;
    private final Double longitude;

    public ShiftOption(String id, String title, String startTime, String endTime, Double latitude, Double longitude) {
        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    //1. CONSTRUCTOR ĐỌC DỮ LIỆU TỪ PARCEL
    protected ShiftOption(Parcel in) {
        id = in.readString();
        title = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        if (in.readByte() == 0) {
            latitude = null;
        } else {
            latitude = in.readDouble();
        }
        if (in.readByte() == 0) {
            longitude = null;
        } else {
            longitude = in.readDouble();
        }
    }


    //3. BIẾN CREATOR BẮT BUỘC ĐỂ ANDROID KHÔI PHỤC OBJECT
    public static final Creator<ShiftOption> CREATOR = new Creator<ShiftOption>() {
        @Override
        public ShiftOption createFromParcel(Parcel in) {
            return new ShiftOption(in);
        }

        @Override
        public ShiftOption[] newArray(int size) {
            return new ShiftOption[size];
        }
    };

    //2. GHI DỮ LIỆU VÀO PARCEL
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(startTime);
        dest.writeString(endTime);
        if (latitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(latitude);
        }
        if (longitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(longitude);
        }
    }
    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }

    /** Getter động phục vụ hiển thị UI: "08:00 - 12:00" */
    public String getTimeRange() {
        if (startTime != null && endTime != null) {
            return formatTime(startTime) + " - " + formatTime(endTime);
        }
        return "";
    }

    private String formatTime(String time) {
        if (time != null && time.length() >= 5) {
            return time.substring(0, 5);
        }
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShiftOption that = (ShiftOption) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
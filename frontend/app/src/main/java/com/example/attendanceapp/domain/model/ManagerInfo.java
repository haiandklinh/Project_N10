package com.example.attendanceapp.domain.model;

import androidx.annotation.Nullable;

import java.util.Objects;

/** Thông tin công khai của quản lý, hiển thị cho nhân viên khi nhập mã mời. */
public final class ManagerInfo {

    private final String managerId;
    private final String managerName;
    private final String companyName;
    @Nullable private final String avatarUrl;

    public ManagerInfo(String managerId, String managerName, String companyName,
                       @Nullable String avatarUrl) {
        this.managerId = Objects.requireNonNull(managerId);
        this.managerName = Objects.requireNonNull(managerName);
        this.companyName = Objects.requireNonNull(companyName);
        this.avatarUrl = avatarUrl;
    }

    public String getManagerId() { return managerId; }
    public String getManagerName() { return managerName; }
    public String getCompanyName() { return companyName; }
    @Nullable public String getAvatarUrl() { return avatarUrl; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ManagerInfo)) return false;
        ManagerInfo m = (ManagerInfo) o;
        return managerId.equals(m.managerId) && managerName.equals(m.managerName)
                && companyName.equals(m.companyName) && Objects.equals(avatarUrl, m.avatarUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(managerId, managerName, companyName, avatarUrl);
    }
}
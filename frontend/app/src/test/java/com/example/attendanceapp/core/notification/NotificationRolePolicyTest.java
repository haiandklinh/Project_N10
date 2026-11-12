package com.example.attendanceapp.core.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.attendanceapp.domain.model.NotificationType;
import com.example.attendanceapp.domain.model.UserRole;

import org.junit.Test;

public class NotificationRolePolicyTest {

    @Test
    public void scheduleAssignedNotifications_onlyAllowedForEmployee() {
        assertTrue(NotificationRolePolicy.isAllowedForRole("ASSIGN_WORK_SCHEDULE", UserRole.EMPLOYEE));
        assertTrue(NotificationRolePolicy.isAllowedForRole("SCHEDULE_ASSIGNED", UserRole.EMPLOYEE));
        assertTrue(NotificationRolePolicy.isAllowedForRole("WORK_SCHEDULE_ASSIGNED", UserRole.EMPLOYEE));
        assertTrue(NotificationRolePolicy.isAllowedForRole("SCHEDULE_UPDATED", UserRole.EMPLOYEE));

        assertFalse(NotificationRolePolicy.isAllowedForRole("ASSIGN_WORK_SCHEDULE", UserRole.MANAGER));
        assertFalse(NotificationRolePolicy.isAllowedForRole("SCHEDULE_ASSIGNED", UserRole.MANAGER));
        assertFalse(NotificationRolePolicy.isAllowedForRole("WORK_SCHEDULE_ASSIGNED", UserRole.MANAGER));
        assertFalse(NotificationRolePolicy.isAllowedForRole("SCHEDULE_UPDATED", UserRole.MANAGER));
    }

    @Test
    public void approvalResponseNotifications_onlyAllowedForEmployee() {
        assertTrue(NotificationRolePolicy.isAllowedForRole("JOIN_COMPANY_REQUEST_RESPONSE", UserRole.EMPLOYEE));
        assertTrue(NotificationRolePolicy.isAllowedForRole("CHANGE_WORK_SHIFT_RESPONSE", UserRole.EMPLOYEE));
        assertTrue(NotificationRolePolicy.isAllowedForRole("CHANGE_LEAVE_REQUEST_RESPONSE", UserRole.EMPLOYEE));
        assertTrue(NotificationRolePolicy.isAllowedForRole("ATTENDANCE_ADJUSTMENT_RESPONSE", UserRole.EMPLOYEE));
        assertTrue(NotificationRolePolicy.isAllowedForRole("PAYROLL", UserRole.EMPLOYEE));

        assertFalse(NotificationRolePolicy.isAllowedForRole("JOIN_COMPANY_REQUEST_RESPONSE", UserRole.MANAGER));
        assertFalse(NotificationRolePolicy.isAllowedForRole("CHANGE_WORK_SHIFT_RESPONSE", UserRole.MANAGER));
        assertFalse(NotificationRolePolicy.isAllowedForRole("CHANGE_LEAVE_REQUEST_RESPONSE", UserRole.MANAGER));
        assertFalse(NotificationRolePolicy.isAllowedForRole("ATTENDANCE_ADJUSTMENT_RESPONSE", UserRole.MANAGER));
        assertFalse(NotificationRolePolicy.isAllowedForRole("PAYROLL", UserRole.MANAGER));
    }

    @Test
    public void incomingRequestNotifications_onlyAllowedForManager() {
        assertTrue(NotificationRolePolicy.isAllowedForRole("JOIN_COMPANY_REQUEST", UserRole.MANAGER));
        assertTrue(NotificationRolePolicy.isAllowedForRole("CHANGE_WORK_SHIFT", UserRole.MANAGER));
        assertTrue(NotificationRolePolicy.isAllowedForRole("SHIFT_CHANGE_REQUEST", UserRole.MANAGER));
        assertTrue(NotificationRolePolicy.isAllowedForRole("CHANGE_LEAVE_REQUEST", UserRole.MANAGER));
        assertTrue(NotificationRolePolicy.isAllowedForRole("LEAVE_REQUEST", UserRole.MANAGER));
        assertTrue(NotificationRolePolicy.isAllowedForRole("ATTENDANCE_ADJUSTMENT_REQUEST", UserRole.MANAGER));

        assertFalse(NotificationRolePolicy.isAllowedForRole("JOIN_COMPANY_REQUEST", UserRole.EMPLOYEE));
        assertFalse(NotificationRolePolicy.isAllowedForRole("CHANGE_WORK_SHIFT", UserRole.EMPLOYEE));
        assertFalse(NotificationRolePolicy.isAllowedForRole("SHIFT_CHANGE_REQUEST", UserRole.EMPLOYEE));
        assertFalse(NotificationRolePolicy.isAllowedForRole("CHANGE_LEAVE_REQUEST", UserRole.EMPLOYEE));
        assertFalse(NotificationRolePolicy.isAllowedForRole("LEAVE_REQUEST", UserRole.EMPLOYEE));
        assertFalse(NotificationRolePolicy.isAllowedForRole("ATTENDANCE_ADJUSTMENT_REQUEST", UserRole.EMPLOYEE));
    }

    @Test
    public void nullRole_rejectsNotifications() {
        assertFalse(NotificationRolePolicy.isAllowedForRole("ASSIGN_WORK_SCHEDULE", null));
        assertFalse(NotificationRolePolicy.isAllowedForRole("JOIN_COMPANY_REQUEST", null));
        assertFalse(NotificationRolePolicy.isAllowedForRole("SYSTEM", null));
        assertFalse(NotificationRolePolicy.isTargetRoleAllowed(UserRole.EMPLOYEE, null));
        assertFalse(NotificationRolePolicy.isTargetRoleAllowed(null, null));
    }

    @Test
    public void targetRoleCheck_blocksCrossRoleDispatch() {
        assertFalse(NotificationRolePolicy.isTargetRoleAllowed(UserRole.EMPLOYEE, UserRole.MANAGER));
        assertFalse(NotificationRolePolicy.isTargetRoleAllowed(UserRole.MANAGER, UserRole.EMPLOYEE));
        assertTrue(NotificationRolePolicy.isTargetRoleAllowed(UserRole.EMPLOYEE, UserRole.EMPLOYEE));
        assertTrue(NotificationRolePolicy.isTargetRoleAllowed(UserRole.MANAGER, UserRole.MANAGER));
        assertTrue(NotificationRolePolicy.isTargetRoleAllowed(null, UserRole.MANAGER));
    }

    @Test
    public void mapNotificationType_mapsScheduleAssignedToSchedule() {
        assertEquals(NotificationType.SCHEDULE, NotificationRolePolicy.mapNotificationType("ASSIGN_WORK_SCHEDULE"));
        assertEquals(NotificationType.SCHEDULE, NotificationRolePolicy.mapNotificationType("SCHEDULE_ASSIGNED"));
        assertEquals(NotificationType.PAYROLL, NotificationRolePolicy.mapNotificationType("PAYROLL"));
        assertEquals(NotificationType.LEAVE, NotificationRolePolicy.mapNotificationType("CHANGE_LEAVE_REQUEST"));
        assertEquals(NotificationType.ATTENDANCE, NotificationRolePolicy.mapNotificationType("ATTENDANCE_ADJUSTMENT_REQUEST"));
    }
}

package com.example.attendanceapp.data.dto.response.profile;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class JoinRequestsListResponse {
    @SerializedName("join_requests")
    private List<JoinRequestItem> joinRequests;

    public JoinRequestsListResponse() {
    }

    public JoinRequestsListResponse(List<JoinRequestItem> joinRequests) {
        this.joinRequests = joinRequests;
    }

    public List<JoinRequestItem> getJoinRequests() {
        return joinRequests;
    }

    public void setJoinRequests(List<JoinRequestItem> joinRequests) {
        this.joinRequests = joinRequests;
    }

    public static class JoinRequestItem {
        @SerializedName("request_id")
        private String requestId;

        @SerializedName("full_name")
        private String fullName;

        @SerializedName("avatar_url")
        private String avatarUrl;

        @SerializedName("phone_number")
        private String phoneNumber;

        @SerializedName("invitation_code")
        private String invitationCode;

        @SerializedName("requested_at")
        private String requestedAt;

        private String status;

        public JoinRequestItem() {
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getInvitationCode() {
            return invitationCode;
        }

        public void setInvitationCode(String invitationCode) {
            this.invitationCode = invitationCode;
        }

        public String getRequestedAt() {
            return requestedAt;
        }

        public void setRequestedAt(String requestedAt) {
            this.requestedAt = requestedAt;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}

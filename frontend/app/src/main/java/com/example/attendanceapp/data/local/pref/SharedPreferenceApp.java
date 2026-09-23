package com.example.attendanceapp.data.local.pref;

import android.content.SharedPreferences;

import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.model.UserStatus;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SharedPreferenceApp {

    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_IS_ACCOUNT_SAVED = "is_account_saved";
    private static final String KEY_IS_FIRST_LAUNCH = "is_first_launch";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_STATUS = "user_status";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_PASSWORD = "password";

    private final SharedPreferences sharedPreferences;

    @Inject
    public SharedPreferenceApp(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean isLoggedIn) {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply();
    }

    public boolean isAccountSaved() {
        return sharedPreferences.getBoolean(KEY_IS_ACCOUNT_SAVED, false);
    }

    public void setAccountSaved(boolean isSaved) {
        sharedPreferences.edit().putBoolean(KEY_IS_ACCOUNT_SAVED, isSaved).apply();
    }

    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true);
    }

    public void setFirstLaunchDone() {
        sharedPreferences.edit().putBoolean(KEY_IS_FIRST_LAUNCH, false).apply();
    }

    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, "EMPLOYEE");
    }

    public void setUserRole(String role) {
        sharedPreferences.edit().putString(KEY_USER_ROLE, role).apply();
    }



    //Login - Register - ForgotPW
    public void saveAuthToken(String token){
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply();
    }

    public String getAuthToken(){
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null);
    }

    public void saveUserStatus(String status){
        sharedPreferences.edit().putString(KEY_USER_STATUS, status).apply();
    }

    public String getUserStatus(){
        return sharedPreferences.getString(KEY_USER_STATUS, null);
    }

    public void saveUserInfo(UserAccount user){
        sharedPreferences.edit()
                .putString(KEY_USER_ID, user.getUserId())
                .putString(KEY_USERNAME, user.getUsername())
                .apply();
    }
    public String getUserId(){
        return sharedPreferences.getString(KEY_USER_ID, null);
    }
    public void saveUsername(String username){
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply();
    }
    public String getUsername(){
        return sharedPreferences.getString(KEY_USERNAME, null);
    }
    public UserAccount getUserInfo(){
        String userId = getUserId();
        String username = getUsername();
        String phone = getPhone();
        String roleStr = getUserRole();
        String statusStr = getUserStatus();
        
        if (userId == null && username == null && phone == null) {
            return null;
        }
        
        UserRole role = UserRole.EMPLOYEE;
        try {
            if (roleStr != null) {
                role = UserRole.valueOf(roleStr);
            }
        } catch (Exception ignored) {}
        
        UserStatus status = UserStatus.NEEDS_ROLE_SELECTION;
        try {
            if (statusStr != null) {
                status = UserStatus.valueOf(statusStr);
            }
        } catch (Exception ignored) {}
        
        return new UserAccount(
                userId != null ? userId : "",
                username != null ? username : "",
                phone != null ? phone : "",
                role,
                false,
                true,
                null,
                status
        );
    }

    public void savePhone(String phone){
        sharedPreferences.edit().putString(KEY_PHONE, phone).apply();
    }
    public String getPhone(){
        return sharedPreferences.getString(KEY_PHONE, null);
    }

    public void savePassword(String password){
        sharedPreferences.edit().putString(KEY_PASSWORD, password).apply();
    }
    public String getPassword(){
        return sharedPreferences.getString(KEY_PASSWORD, null);
    }

    /**
     * Xóa sạch trạng thái phiên làm việc khi người dùng Đăng xuất
     */
    public void clearSession() {
        sharedPreferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .putBoolean(KEY_IS_ACCOUNT_SAVED, false)
                .remove(KEY_USER_ROLE)
                .remove(KEY_AUTH_TOKEN)
                .remove(KEY_USER_STATUS)
                .remove(KEY_USER_ID)
                .remove(KEY_USERNAME)
                .remove(KEY_PHONE)
                .remove(KEY_PASSWORD)
                .apply();
    }

}
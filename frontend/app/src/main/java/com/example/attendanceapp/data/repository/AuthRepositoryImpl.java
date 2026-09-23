package com.example.attendanceapp.data.repository;

import android.os.Handler;
import android.os.Looper;

import com.example.attendanceapp.data.local.dao.UserDao;
import com.example.attendanceapp.data.local.entity.UserEntity;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.model.UserStatus;
import com.example.attendanceapp.domain.repository.AuthRepository;
import com.example.attendanceapp.domain.repository.UserRepository;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthRepositoryImpl implements AuthRepository {

    private final UserDao userDao;
    private final UserRepository userRepository;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    @Inject
    public AuthRepositoryImpl(UserDao userDao, UserRepository userRepository) {
        this.userDao = userDao;
        this.userRepository = userRepository;
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void register(String fullName, String phoneNumber, String password, DataCallback<UserAccount> callback) {
        executorService.execute(() -> {
            try {
                // Kiểm tra xem SĐT đã tồn tại chưa
                if (userDao.isPhoneExists(phoneNumber)) {
                    postError(callback, "Số điện thoại này đã được đăng ký!");
                    return;
                }

                // Tạo Entity và chèn vào Database (role mặc định EMPLOYEE)
                UserEntity entity = new UserEntity(fullName, phoneNumber, password, UserRole.EMPLOYEE.name());
                long id = userDao.insertUser(entity);

                if (id > 0) {
                    // Tạo UserAccount từ database result
                    UserAccount newUser = new UserAccount(
                            String.valueOf(id),
                            fullName,
                            phoneNumber,
                            UserRole.EMPLOYEE,
                            true,           // isFirstLogin
                            true,           // isActive
                            Instant.now(),
                            UserStatus.NEEDS_ROLE_SELECTION
                    );

                    // Lưu session thông qua UserRepository
                    mainHandler.post(() -> {
                        userRepository.saveUserSession(newUser, false); // Chưa login, chỉ lưu info
                        callback.onSuccess(newUser);
                    });
                } else {
                    postError(callback, "Đăng ký thất bại, vui lòng thử lại!");
                }
            } catch (Exception e) {
                postError(callback, "Lỗi cơ sở dữ liệu: " + e.getMessage());
            }
        });
    }

    @Override
    public void checkPhoneExists(String phoneNumber, DataCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                boolean exists = userDao.isPhoneExists(phoneNumber);
                mainHandler.post(() -> callback.onSuccess(exists));
            } catch (Exception e) {
                postError(callback, "Lỗi kiểm tra số điện thoại: " + e.getMessage());
            }
        });
    }

    @Override
    public void login(String phoneNumber, String password, DataCallback<UserAccount> callback) {
        executorService.execute(() -> {
            try {
                UserEntity entity = userDao.login(phoneNumber, password);
                if (entity != null) {
                    UserRole role = UserRole.EMPLOYEE;
                    try {
                        if (entity.getRole() != null) {
                            role = UserRole.valueOf(entity.getRole());
                        }
                    } catch (Exception ignored) {}

                    // Mapping từ Entity sang Domain Model
                    UserAccount userAccount = new UserAccount(
                            String.valueOf(entity.getId()),
                            entity.getName(),
                            entity.getPhone(),
                            role,
                            false,
                            true,
                            Instant.now(),
                            UserStatus.APPROVED
                    );

                    // Lưu session thông qua UserRepository
                    mainHandler.post(() -> {
                        userRepository.saveUserSession(userAccount, true);
                        callback.onSuccess(userAccount);
                    });
                } else {
                    postError(callback, "Số điện thoại hoặc mật khẩu không chính xác!");
                }
            } catch (Exception e) {
                postError(callback, "Lỗi đăng nhập: " + e.getMessage());
            }
        });
    }

    @Override
    public void selectRole(UserRole role, DataCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                if (role == null) {
                    postError(callback, "Vui lòng chọn vai trò");
                    return;
                }
                
                // Lấy phone number từ current user để update trong database
                UserAccount currentUser = userRepository.getCurrentUser();
                if (currentUser == null || currentUser.getPhoneNumber() == null) {
                    postError(callback, "Không tìm thấy thông tin người dùng");
                    return;
                }
                
                // Cập nhật role trong Room database
                userDao.updateRole(currentUser.getPhoneNumber(), role.name());
                
                // Lưu role vào session thông qua UserRepository
                mainHandler.post(() -> {
                    userRepository.saveUserRole(role);
                    callback.onSuccess(null);
                });
            } catch (Exception e) {
                postError(callback, "Lỗi chọn vai trò: " + e.getMessage());
            }
        });
    }

    @Override
    public void createCompany(String companyName, DataCallback<String> callback) {
        executorService.execute(() -> {
            try {
                if (companyName == null || companyName.isEmpty()) {
                    postError(callback, "Tên công ty không được để trống");
                    return;
                }
                
                // TODO: Gọi API để tạo company và update user status trong database
                // Hiện tại chỉ mock lưu session
                mainHandler.post(() -> {
                    userRepository.saveUserStatus("APPROVED");
                    callback.onSuccess(companyName);
                });
            } catch (Exception e) {
                postError(callback, "Lỗi tạo công ty: " + e.getMessage());
            }
        });
    }

    @Override
    public void joinCompanyWithCode(String invitationCode, DataCallback<String> callback) {
        executorService.execute(() -> {
            try {
                if (invitationCode == null || invitationCode.length() < 4) {
                    postError(callback, "Mã mời không hợp lệ hoặc không tồn tại");
                    return;
                }
                
                // TODO: Gọi API để verify code và join company
                // Hiện tại chỉ mock
                String companyName = "Công ty Công nghệ ABC";
                mainHandler.post(() -> {
                    userRepository.saveUserStatus("PENDING_APPROVAL");
                    callback.onSuccess(companyName);
                });
            } catch (Exception e) {
                postError(callback, "Lỗi tham gia công ty: " + e.getMessage());
            }
        });
    }

    @Override
    public void sendOtp(String phoneNumber, DataCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                // TODO: Gọi API để gửi OTP
                if (phoneNumber == null || phoneNumber.length() < 10) {
                    postError(callback, "Số điện thoại không hợp lệ");
                } else {
                    mainHandler.post(() -> callback.onSuccess(null));
                }
            } catch (Exception e) {
                postError(callback, "Lỗi gửi OTP: " + e.getMessage());
            }
        });
    }

    @Override
    public void verifyOtp(String phoneNumber, String otp, DataCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                // TODO: Gọi API để verify OTP
                if ("123456".equals(otp)) {
                    mainHandler.post(() -> callback.onSuccess(null));
                } else {
                    postError(callback, "Mã OTP không đúng hoặc đã hết hạn");
                }
            } catch (Exception e) {
                postError(callback, "Lỗi xác thực OTP: " + e.getMessage());
            }
        });
    }

    @Override
    public void resetPassword(String phoneNumber, String newPassword, DataCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                if (newPassword == null || newPassword.length() < 6) {
                    postError(callback, "Mật khẩu phải chứa ít nhất 6 ký tự");
                    return;
                }
                
                // Cập nhật password trong database
                userDao.updatePassword(phoneNumber, newPassword);
                
                mainHandler.post(() -> callback.onSuccess(null));
            } catch (Exception e) {
                postError(callback, "Lỗi đặt lại mật khẩu: " + e.getMessage());
            }
        });
    }

    @Override
    public void logout(DataCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                // Xóa session thông qua UserRepository
                mainHandler.post(() -> {
                    userRepository.clearUserSession();
                    callback.onSuccess(null);
                });
            } catch (Exception e) {
                postError(callback, "Lỗi đăng xuất: " + e.getMessage());
            }
        });
    }

    // =========================================================
    // HELPER METHODS FOR MAIN THREAD POSTING
    // =========================================================

    private <T> void postSuccess(DataCallback<T> callback, T result) {
        if (callback != null) {
            mainHandler.post(() -> callback.onSuccess(result));
        }
    }

    private <T> void postError(DataCallback<T> callback, String message) {
        if (callback != null) {
            mainHandler.post(() -> callback.onError(message));
        }
    }
}
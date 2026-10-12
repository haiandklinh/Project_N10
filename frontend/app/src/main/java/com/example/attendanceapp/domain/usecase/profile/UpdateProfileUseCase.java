package com.example.attendanceapp.domain.usecase.profile;

import android.net.Uri;
import com.example.attendanceapp.domain.model.Gender;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.UserRepository;

import java.time.LocalDate;
import javax.inject.Inject;
import io.reactivex.rxjava3.core.Completable;

public class UpdateProfileUseCase {

    private final UserRepository userRepository;

    @Inject
    public UpdateProfileUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Completable execute(String fullName, LocalDate dob, Gender gender, Uri avatarUri, UserRole role, String companyName) {
        return userRepository.updateProfile(fullName, dob, gender, avatarUri, role, companyName);
    }
}

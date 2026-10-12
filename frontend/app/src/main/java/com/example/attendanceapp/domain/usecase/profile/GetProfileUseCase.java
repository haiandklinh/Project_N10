package com.example.attendanceapp.domain.usecase.profile;

import com.example.attendanceapp.domain.model.UserProfile;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.UserRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class GetProfileUseCase {

    private final UserRepository userRepository;

    @Inject
    public GetProfileUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Single<UserProfile> execute() {
        return userRepository.getCurrentUser()
                .flatMapSingle(account -> {
                    UserRole role = account.getProfile().getRole();
                    // Depending on how UserRepository is implemented, we might need a separate method for fetching
                    // But usually, getCurrentUser() might be enough if it's synced with server.
                    // For now, let's assume we want to refresh from server via a dedicated method in repository if available.
                    // If not, we use the account info.
                    return Single.just(account.getProfile());
                })
                .toSingle();
    }
}

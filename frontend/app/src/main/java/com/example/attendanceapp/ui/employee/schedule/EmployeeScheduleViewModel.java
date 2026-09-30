package com.example.attendanceapp.ui.employee.schedule;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.attendanceapp.data.dto.response.schedule.StaffWorkScheduleResponse;
import com.example.attendanceapp.domain.usecase.schedule.GetScheduleByDateUseCase;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class EmployeeScheduleViewModel extends ViewModel {

    private final GetScheduleByDateUseCase getScheduleByDateUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<ScheduleUiState> scheduleUiState = new MutableLiveData<>();

    @Inject
    public EmployeeScheduleViewModel(GetScheduleByDateUseCase getScheduleByDateUseCase) {
        this.getScheduleByDateUseCase = getScheduleByDateUseCase;
    }

    public void loadScheduleByDate(String date) {
        scheduleUiState.setValue(ScheduleUiState.loading());
        disposables.add(
                getScheduleByDateUseCase.execute(date)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                schedules -> scheduleUiState.setValue(ScheduleUiState.success(schedules)),
                                error -> scheduleUiState.setValue(ScheduleUiState.error(error.getMessage()))
                        )
        );
    }

    public LiveData<ScheduleUiState> getScheduleUiState() {
        return scheduleUiState;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }

    public static class ScheduleUiState {
        private final boolean isLoading;
        private final List<StaffWorkScheduleResponse> schedules;
        private final String error;

        public ScheduleUiState(boolean isLoading, List<StaffWorkScheduleResponse> schedules, String error) {
            this.isLoading = isLoading;
            this.schedules = schedules;
            this.error = error;
        }

        public static ScheduleUiState loading() {
            return new ScheduleUiState(true, null, null);
        }

        public static ScheduleUiState success(List<StaffWorkScheduleResponse> schedules) {
            return new ScheduleUiState(false, schedules, null);
        }

        public static ScheduleUiState error(String error) {
            return new ScheduleUiState(false, null, error);
        }

        public boolean isLoading() {
            return isLoading;
        }

        public List<StaffWorkScheduleResponse> getSchedules() {
            return schedules;
        }

        public String getError() {
            return error;
        }
    }
}

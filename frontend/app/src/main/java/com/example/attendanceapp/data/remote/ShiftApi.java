package com.example.attendanceapp.data.remote;

import com.example.attendanceapp.data.dto.request.shift.CreateShiftRequest;
import com.example.attendanceapp.data.dto.request.shift.UpdateShiftRequest;
import com.example.attendanceapp.data.dto.response.shift.ShiftResponse;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ShiftApi {

    @GET("shift")
    Single<List<ShiftResponse>> getShifts();

    @GET("shift/{shift_id}")
    Single<ShiftResponse> getShiftDetail(@Path("shift_id") String shiftId);

    @POST("shift")
    Single<ShiftResponse> createShift(@Body CreateShiftRequest request);

    @PUT("shift/{shift_id}")
    Single<ShiftResponse> updateShift(
            @Path("shift_id") String shiftId,
            @Body UpdateShiftRequest request
    );

    @DELETE("shift/{shift_id}")
    Completable deleteShift(@Path("shift_id") String shiftId);
}
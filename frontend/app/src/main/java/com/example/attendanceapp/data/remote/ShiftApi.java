package com.example.attendanceapp.data.remote;

import com.example.attendanceapp.data.dto.request.shift.ShiftItemRequest;
import com.example.attendanceapp.data.dto.response.BaseListResponse;
import com.example.attendanceapp.data.dto.response.shift.ShiftItemResponse;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ShiftApi {

    // 1. GET /shifts - Get All Shifts
    @GET("shifts")
    Single<BaseListResponse<ShiftItemResponse>> getShifts();

    // 2. GET /shifts/{id_shift} - Get Shift Detail
    @GET("shifts/{id_shift}")
    Single<ShiftItemResponse> getShiftDetail(@Path("id_shift") String shiftId);

    // 3. POST /shifts - Create Shift
    @POST("shifts")
    Single<ShiftItemResponse> createShift(@Body ShiftItemRequest request);

    // 4. PATCH /shifts/{id_shift} - Update Shift (Sửa @PUT -> @PATCH)
    @PATCH("shifts/{id_shift}")
    Single<ShiftItemResponse> updateShift(
            @Path("id_shift") String shiftId,
            @Body ShiftItemRequest request
    );

    // 5. DELETE /shifts/{id_shift} - Delete Shift
    @DELETE("shifts/{id_shift}")
    Single<ShiftItemResponse> deleteShift(@Path("id_shift") String shiftId);
}
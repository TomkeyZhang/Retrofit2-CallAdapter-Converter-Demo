package com.example.tomkeyzhang.retrofit2_demo.call.impl;

import android.support.annotation.NonNull;

import com.example.tomkeyzhang.retrofit2_demo.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by tomkeyzhang on 27/12/17.
 */

public class DefaultCall<T> extends BaseCall<T> {
    private final Call<ApiResponse<T>> call;

    public DefaultCall(@NonNull Call<ApiResponse<T>> call, @NonNull List<com.example.tomkeyzhang.retrofit2_demo.call.Call.Interceptor> interceptors) {
        super(interceptors);
        this.call = call;
    }

    @Override
    protected void onFirstActive() {
        call.enqueue(new Callback<ApiResponse<T>>() {
            @Override
            public void onResponse(Call<ApiResponse<T>> call, Response<ApiResponse<T>> response) {
                postValue(parseResponse(response));
            }

            @Override
            public void onFailure(Call<ApiResponse<T>> call, Throwable throwable) {
                postValue(ApiResponse.unknownError(throwable));
            }
        });
    }

    /**
     * 发起同步网络请求
     */
    public ApiResponse<T> execute() {
        ApiResponse<T> apiResponse;
        try {
            apiResponse = parseResponse(call.execute());
        } catch (Throwable e) {
            apiResponse = ApiResponse.unknownError(e);
        }
        return apiResponse;
    }

    /***
     *
     * @param response
     * @return
     */
    private ApiResponse<T> parseResponse(Response<ApiResponse<T>> response) {
        ApiResponse<T> apiResponse;
        if (response.isSuccessful()) {
            apiResponse = response.body();
            apiResponse.setCode(response.code());
        } else {
            apiResponse = ApiResponse.responseError(response.code(), response.message());
        }
        return apiResponse;
    }

    @Override
    public void cancel() {
        super.cancel();
        call.cancel();
    }

    @Override
    public boolean isCancelled() {
        return call.isCanceled() || super.isCancelled();
    }
}

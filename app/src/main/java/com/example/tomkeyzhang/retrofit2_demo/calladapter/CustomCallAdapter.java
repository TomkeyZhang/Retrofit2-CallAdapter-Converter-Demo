package com.example.tomkeyzhang.retrofit2_demo.calladapter;

/**
 * Created by tomkeyzhang on 26/12/17.
 */

import com.example.tomkeyzhang.retrofit2_demo.ApiResponse;
import com.example.tomkeyzhang.retrofit2_demo.call.impl.DefaultCall;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.CallAdapter;

/**
 * A Retrofit adapter that converts the Call into a DadaCall of ApiResponse.
 */
public class CustomCallAdapter<R> implements CallAdapter<ApiResponse<R>, com.example.tomkeyzhang.retrofit2_demo.call.Call<R>> {
    private final Type responseType;
    private final List<com.example.tomkeyzhang.retrofit2_demo.call.Call.Interceptor> interceptors;

    public CustomCallAdapter(Type responseType, List<com.example.tomkeyzhang.retrofit2_demo.call.Call.Interceptor> interceptors) {
        this.responseType = responseType;
        this.interceptors = interceptors;
    }

    @Override
    public Type responseType() {
        return responseType;
    }

    @Override
    public com.example.tomkeyzhang.retrofit2_demo.call.Call<R> adapt(Call<ApiResponse<R>> call) {
        return new DefaultCall<>(call, interceptors);
    }
}


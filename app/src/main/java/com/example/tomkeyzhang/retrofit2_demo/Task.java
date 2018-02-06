package com.example.tomkeyzhang.retrofit2_demo;

import android.support.annotation.Nullable;

import com.example.tomkeyzhang.retrofit2_demo.call.LifeState;


/**
 * Created by tomkeyzhang on 11/1/18.
 * http请求任务抽象，执行一个任务，将对象F转换成T
 * F:from type
 * T:to type
 */

public interface Task<F, T> {
    ApiResponse<T> doTask(@Nullable LifeState lifeState, @Nullable ApiResponse<F> apiResponse);
}
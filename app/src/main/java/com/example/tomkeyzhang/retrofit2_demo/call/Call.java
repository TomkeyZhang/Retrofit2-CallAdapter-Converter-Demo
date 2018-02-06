package com.example.tomkeyzhang.retrofit2_demo.call;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;

import com.example.tomkeyzhang.retrofit2_demo.ApiResponse;
import com.example.tomkeyzhang.retrofit2_demo.ProgressOperation;

/**
 * Created by tomkeyzhang on 5/2/18.
 */

public interface Call<T> {
    /**
     * 设置http200 ok的回调
     */
    Call<T> ok(@NonNull Observer<T> observer);

    /**
     * 设置http200 fail的回调
     */
    Call<T> fail(@NonNull Observer<ApiResponse<T>> observer);

    /**
     * 设置error的回调
     */
    Call<T> error(@NonNull Observer<ApiResponse<T>> observer);

    /**
     * 设置进度监听
     */
    Call<T> progress(@NonNull ProgressOperation progressOperation);

    /**
     * 执行异步请求，绑定组件生命周期(获取全部状态结果)
     */
    void enqueue(@NonNull LifecycleOwner owner, @NonNull Observer<ApiResponse<T>> observer);

    /**
     * 执行异步请求，绑定组件生命周期（获取部分状态结果）
     */
    void enqueue(@NonNull LifecycleOwner owner);

    /**
     * 执行异步请求，但不需要绑定组件生命周期(获取部分状态结果)
     */
    void enqueue();

    /**
     * 执行异步请求，但不需要绑定组件生命周期(获取全部状态结果)
     */
    void enqueue(@NonNull Observer<ApiResponse<T>> observer);

    /**
     * 发起同步网络请求
     */
    ApiResponse<T> execute();

    /**
     * 取消请求
     * 1、对于单个http请求，取消时如果还没有开始执行，则不执行；如果在执行中，则执行结束不会回调
     * 2、对于多个连续http请求，除了1的特性外，取消后剩下的未开始执行请求也不会被执行
     */
    void cancel();

    /**
     * 是否被取消
     *
     * @return
     */
    boolean isCancelled();

    interface Interceptor {

        /**
         * 返回true表示停止下一步执行
         */
        boolean preExecute();

        /**
         * 对于异步请求，返回true表示停止下一步执行
         */
        boolean onResponse(ApiResponse apiResponse);
    }

    Observer OBSERVER_NONE = (t) -> {
    };
}

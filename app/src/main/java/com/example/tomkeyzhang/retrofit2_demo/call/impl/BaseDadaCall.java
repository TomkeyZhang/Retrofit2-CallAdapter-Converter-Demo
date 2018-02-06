package com.example.tomkeyzhang.retrofit2_demo.call.impl;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.tomkeyzhang.retrofit2_demo.ApiResponse;
import com.example.tomkeyzhang.retrofit2_demo.ProgressOperation;
import com.example.tomkeyzhang.retrofit2_demo.call.Call;
import com.example.tomkeyzhang.retrofit2_demo.call.LifeState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tomkeyzhang on 4/1/18.
 */

public abstract class BaseDadaCall<T> extends LiveData<ApiResponse<T>> implements Call<T> {
    private AtomicBoolean started = new AtomicBoolean(false);
    private Observer<T> ok = Call.OBSERVER_NONE;
    private Observer<ApiResponse<T>> fail = Call.OBSERVER_NONE;
    private Observer<ApiResponse<T>> error = Call.OBSERVER_NONE;
    private ProgressOperation progress = ProgressOperation.NONE;
    private List<Interceptor> interceptors = new ArrayList<>();
    protected LifeState lifeState;

    public BaseDadaCall(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }

    private static final LifecycleOwner ALWAYS_ON = new LifecycleOwner() {

        private LifecycleRegistry mRegistry = init();

        private LifecycleRegistry init() {
            LifecycleRegistry registry = new LifecycleRegistry(this);
            registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
            registry.handleLifecycleEvent(Lifecycle.Event.ON_START);
            registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
            return registry;
        }

        @Override
        public Lifecycle getLifecycle() {
            return mRegistry;
        }
    };

    @Override
    protected final void onActive() {
        super.onActive();
        if (started.compareAndSet(false, true)) {
            if (isCancelled())
                return;//被手动取消
            onFirstActive();
        }
    }

    public boolean isCancelled() {
        return lifeState != null && lifeState.isCancelled();
    }

    protected abstract void onFirstActive();

    /**
     * 设置http200 ok的回调
     */
    public Call<T> ok(@NonNull Observer<T> observer) {
        this.ok = observer;
        return this;
    }

    /**
     * 设置http200 fail的回调
     */
    public Call<T> fail(@NonNull Observer<ApiResponse<T>> observer) {
        this.fail = observer;
        return this;
    }

    /**
     * 设置error的回调
     */
    public Call<T> error(@NonNull Observer<ApiResponse<T>> observer) {
        this.error = observer;
        return this;
    }

    @Override
    public Call<T> progress(@NonNull ProgressOperation progressOperation) {
        this.progress = progressOperation;
        return this;
    }

    /**
     * 执行异步请求，绑定组件生命周期（获取部分状态结果）
     */
    public void enqueue(@NonNull LifecycleOwner owner) {
        observe(owner, apiResponse -> {
            if (isCancelled())
                return;//被手动取消
            if (apiResponse.isOk()) {
                progress.showContent();
            } else {
                progress.showFailed();
            }//转圈
            for (Interceptor interceptor : interceptors) {
                if (interceptor.onResponse(apiResponse))
                    return;
            }//拦截器
            if (apiResponse.isOk()) {
                ok.onChanged(apiResponse.getContent());
            } else if (apiResponse.isFail()) {
                fail.onChanged(apiResponse);
            } else if (apiResponse.isError()) {
                error.onChanged(apiResponse);
            }//业务逻辑
        });
    }

    /**
     * 执行异步请求，绑定组件生命周期(获取全部状态结果)
     */
    public void enqueue(@NonNull LifecycleOwner owner, @NonNull Observer<ApiResponse<T>> observer) {
        observe(owner, observer);
    }

    /**
     * 执行异步请求，但不需要绑定组件生命周期(获取部分状态结果)
     */
    public void enqueue() {
        enqueue(ALWAYS_ON);
    }

    /**
     * 执行异步请求，但不需要绑定组件生命周期(获取全部状态结果)
     */
    public void enqueue(@NonNull Observer<ApiResponse<T>> observer) {
        enqueue(ALWAYS_ON, observer);
    }

    @Override
    public void cancel() {
        lifeState.cancel();
    }

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<ApiResponse<T>> observer) {
        lifeState = new LifeState(owner.getLifecycle());
        for (Interceptor interceptor : interceptors) {
            if (interceptor.preExecute())
                return;
        }
        progress.showProgress();
        //确保每次调用，Observer回调只会被执行一次
        super.observe(owner, new Observer<ApiResponse<T>>() {
            @Override
            public void onChanged(@Nullable ApiResponse<T> apiResponse) {
                try {
                    observer.onChanged(apiResponse);
                }catch (Throwable e){
                    e.printStackTrace();
                }
                removeObserver(this);
            }
        });
    }
}

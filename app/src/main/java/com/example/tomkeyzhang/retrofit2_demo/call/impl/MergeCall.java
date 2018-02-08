package com.example.tomkeyzhang.retrofit2_demo.call.impl;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.example.tomkeyzhang.retrofit2_demo.ApiResponse;
import com.example.tomkeyzhang.retrofit2_demo.CheckTokenInterceptor;
import com.example.tomkeyzhang.retrofit2_demo.Task;
import com.example.tomkeyzhang.retrofit2_demo.call.Call;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by tomkeyzhang on 3/1/18.
 * 异步的执行多个DadaCall
 */

public class MergeCall extends BaseCall {
    private List<Task> tasks;
    private Executor executor;


    private MergeCall(@NonNull List<Task> tasks, List<Call.Interceptor> interceptors, Executor executor) {
        super(interceptors);
        if (tasks.isEmpty())
            throw new RuntimeException("tasks must at lest have one task!");
        this.tasks = tasks;
        this.executor = executor;
    }

    @Override
    protected void onFirstActive() {
        new AsyncTask<Void, Void, ApiResponse>() {
            @Override
            protected ApiResponse doInBackground(Void[] dadaCalls) {
                return MergeCall.this.execute();
            }


            @Override
            protected void onPostExecute(ApiResponse apiResponse) {
                setValue(apiResponse);
            }
        }.executeOnExecutor(executor);
    }


    @Override
    public ApiResponse execute() {
        Task task;
        ApiResponse apiResponse = null;
        Throwable throwable = null;
        for (int i = 0; i < tasks.size(); i++) {
            if (isCancelled())
                break;
            if (apiResponse == null || apiResponse.isOk()) {
                task = tasks.get(i);
                try {
                    apiResponse = task.doTask(lifeState, apiResponse);
                } catch (Throwable e) {
                    throwable = e;
                }
            }
            if (apiResponse == null)
                break;
        }
        return apiResponse != null ? apiResponse : ApiResponse.unknownError(throwable != null ? throwable : new RuntimeException("no response,request may be cancelled!"));
    }

    public static Call task(Executor executor, List<Interceptor> interceptors, Task... tasks) {
        return new MergeCall(Arrays.asList(tasks), interceptors, executor);
    }

    public static Call task(Task... tasks) {
        return task(AsyncTask.THREAD_POOL_EXECUTOR, tasks);
    }

    public static Call task(Executor executor, Task... tasks) {
        return MergeCall.task(executor, Arrays.asList(CheckTokenInterceptor.INSTANCE), tasks);
    }
}

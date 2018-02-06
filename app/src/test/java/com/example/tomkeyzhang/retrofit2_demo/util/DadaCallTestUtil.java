package com.example.tomkeyzhang.retrofit2_demo.util;

import com.example.tomkeyzhang.retrofit2_demo.ApiResponse;
import com.example.tomkeyzhang.retrofit2_demo.call.Call;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by tomkeyzhang on 28/12/17.
 * 同步的从 DadaCall 中取数据
 */

public class DadaCallTestUtil {
    public static <T> T getValue(Call<T> liveData) {
        final Object[] data = new Object[1];
        CountDownLatch latch = new CountDownLatch(1);

        liveData.enqueue(response->{
            data[0] = response;
            latch.countDown();
        });
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //noinspection unchecked
        return (T) data[0];
    }

    public static <T> T getValueOk(Call<T> liveData) {
        final Object[] data = new Object[1];
        CountDownLatch latch = new CountDownLatch(1);

        liveData.ok(response->{
            data[0] = response;
            latch.countDown();
        }).enqueue();
        try {
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //noinspection unchecked
        return (T) data[0];
    }

    public static <T> ApiResponse<T> getValueFail(Call<T> liveData) {
        final Object[] data = new Object[1];
        CountDownLatch latch = new CountDownLatch(1);

        liveData.fail(response->{
            data[0] = response;
            latch.countDown();
        }).enqueue();
        try {
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //noinspection unchecked
        return (ApiResponse<T>) data[0];
    }

    public static <T> ApiResponse<T> getValueError(Call<T> liveData) {
        final Object[] data = new Object[1];
        CountDownLatch latch = new CountDownLatch(1);

        liveData.error(response->{
            data[0] = response;
            latch.countDown();
        }).enqueue();
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //noinspection unchecked
        return (ApiResponse<T>) data[0];
    }

}

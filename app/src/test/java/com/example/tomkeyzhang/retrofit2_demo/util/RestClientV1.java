package com.example.tomkeyzhang.retrofit2_demo.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.tomkeyzhang.retrofit2_demo.call.Call;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by tomkeyzhang on 28/12/17.
 */

public interface RestClientV1 {

    @GET("order/detail")
    String getOrderDetail(@Query("orderId") long orderId);


    /**
     * test code=200 status=ok
     */
    @GET("/200/ok/content")
    Call<Content> ok(@Query("id") String id);

    /**
     * test code=200 status=fail
     */
    @POST("/200/fail")
    Call<Content> fail(@Query("id")String id);

    /**
     * test code=200 status=ok
     */
    @GET("/200/unknown")
    Call<Content> unknown200(@Query("id") String id);


    /**
     * test code=404
     */
    @POST("/404/responseError")
    Call<Content> error404(@Query("id")String id);

    /**
     * test code=502
     */
    @POST("/502/responseError")
    Call<Content> error502(@Query("id")String id);

    /**
     * test code=0
     */
    @POST("/throwable/responseError")
    Call<Content> errorThrowable(@Query("id")String id);


    /**
     * test code=200 status=ok
     */
    @GET("/200/ok/json/object")
    Call<JSONObject> okObject(@Query("id") String id);

    /**
     * test code=200 status=ok
     */
    @GET("/200/ok/json/array")
    Call<JSONArray> okArray(@Query("id") String id);

}

package com.example.tomkeyzhang.retrofit2_demo.util;

import com.alibaba.fastjson.JSON;
import com.example.tomkeyzhang.retrofit2_demo.ApiResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * Created by tomkeyzhang on 4/1/18.
 */

public class MockDispatcher extends Dispatcher {
    @Override
    public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        String url = request.getPath();
        if (url.contains("404")) {
            return new MockResponse().setResponseCode(404).setBody("server responseError 404!");
        } else if (url.contains("502")) {
            return new MockResponse().setResponseCode(502).setBody("server responseError 502!");
        } else if (url.contains("200/ok/content")) {
            ApiResponse<Content> apiResponse = new ApiResponse<>();
            apiResponse.setStatus(ApiResponse.OK);
            apiResponse.setContent(new Content("tomkey"));
            return new MockResponse().setResponseCode(200).setBody(JSON.toJSONString(apiResponse));
        }else if (url.contains("200/ok/json/object")) {
            ApiResponse<Content> apiResponse = new ApiResponse<>();
            apiResponse.setStatus(ApiResponse.OK);
            apiResponse.setContent(new Content("tomkey_object"));
            return new MockResponse().setResponseCode(200).setBody(JSON.toJSONString(apiResponse));
        }else if (url.contains("200/ok/json/array")) {
            ApiResponse<List<Content>> apiResponse = new ApiResponse<>();
            apiResponse.setStatus(ApiResponse.OK);
            List<Content> list=new ArrayList<>();
            list.add(new Content("tomkey_item"));
            apiResponse.setContent(list);
            return new MockResponse().setResponseCode(200).setBody(JSON.toJSONString(apiResponse));
        }


        ///
        else if (url.contains("200/fail")) {
            ApiResponse<Content> apiResponse = new ApiResponse<>();
            apiResponse.setStatus(ApiResponse.FAIL);
            apiResponse.setErrorMsg("输入参数不对！");
            return new MockResponse().setResponseCode(200).setBody(JSON.toJSONString(apiResponse));
        } else if (url.contains("200/unknown")) {
            return new MockResponse().setResponseCode(200).setBody("unknown");
        } else if(url.contains("order/detail")){

            return new MockResponse().setResponseCode(200).setBody("hi man,this is order detail(orderId="+request.getRequestUrl().queryParameter("orderId")+")");
        }else{
            return new MockResponse().setBody("xdlskaksaksajajskjakjskjaksjkajskjakjskajdjkasjdksajdksajdksajdkjasdkjs")
                    .throttleBody(1, 5, TimeUnit.SECONDS);
        }
    }
}
package com.example.tomkeyzhang.retrofit2_demo.converter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.example.tomkeyzhang.retrofit2_demo.ApiResponse;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;

public class FastJsonResponseBodyConverter<T> implements Converter<ResponseBody, ApiResponse<T>> {
    private final Type type;

    public FastJsonResponseBodyConverter(Type type) {
        this.type = type;
    }

    /*
    * 转换方法
    */
    @Override
    public ApiResponse<T> convert(ResponseBody value) throws IOException {
        try {
            ApiResponse<T> apiResponse = JSON.parseObject(value.string(), new TypeReference<ApiResponse<T>>() {
            });
            T t = apiResponse.getContent();
            //JSONObject和JSONArray不需要转换
            if (t != null && JSONObject.class != type && JSONArray.class != type) {
                apiResponse.setContent(JSON.parseObject(t.toString(), type));
            }
            return apiResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.unknownError(e);
        }
    }
}

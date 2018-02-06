package com.example.tomkeyzhang.retrofit2_demo;

/**
 * Created by tomkeyzhang on 6/2/18.
 */

import com.example.tomkeyzhang.retrofit2_demo.call.Call;

/**
 * Created by tomkeyzhang on 3/1/18.
 * 检测token过期拦截器
 */

public class CheckTokenInterceptor implements Call.Interceptor {

    public static final CheckTokenInterceptor INSTANCE = new CheckTokenInterceptor();

    /**
     * 返回true表示停止下一步执行
     */
    @Override
    public boolean preExecute() {
        return false;
    }

    /**
     * 对于异步请求，返回true表示停止下一步执行
     */
    @Override
    public boolean onResponse(ApiResponse apiResponse) {
        return checkTokenExpired(apiResponse.getErrorCode());
    }

    private boolean checkTokenExpired(String errorCode) {
        //检查token是否过期
        return false;
    }

}
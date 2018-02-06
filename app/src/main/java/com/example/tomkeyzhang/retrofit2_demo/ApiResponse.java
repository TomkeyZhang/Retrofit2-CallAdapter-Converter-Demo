package com.example.tomkeyzhang.retrofit2_demo;

import lombok.Data;

/**
 * Created by tomkeyzhang on 26/12/17.
 */
@Data
public class ApiResponse<T> {
    public static final String OK = "ok";
    public static final String FAIL = "fail";
    private static final String RESPONSE_ERROR = "response_error";
    private static final String UNKNOWN_ERROR = "unknown_error";

    /**
     * http响应码
     */
    private int code;
    /**
     * api响应状态
     */
    private String status;
    /**
     * api业务数据
     */
    private T content;
    /**
     * api响应错误码
     */
    private String errorCode;
    /**
     * 错误字符串信息
     */
    private String errorMsg;
    /**
     * 错误异常信息
     */
    private Throwable throwable;

    //for json2object generate
    public ApiResponse() {
    }

    public static <T> ApiResponse<T> unknownError(Throwable error) {
        ApiResponse apiResponse = new ApiResponse<>();
        apiResponse.setThrowable(error);
        apiResponse.setStatus(UNKNOWN_ERROR);
        return apiResponse;
    }

    public static <T> ApiResponse<T> responseError(int code, String errorMsg) {
        ApiResponse apiResponse = new ApiResponse<>();
        apiResponse.setCode(code);
        apiResponse.setErrorMsg(errorMsg);
        apiResponse.setStatus(RESPONSE_ERROR);
        return apiResponse;
    }

    public static <T> ApiResponse<T> ok(T content) {
        ApiResponse apiResponse = new ApiResponse<>();
        apiResponse.setCode(200);
        apiResponse.setContent(content);
        apiResponse.setStatus(OK);
        return apiResponse;
    }

    public static <T> ApiResponse<T> fail(String errorCode,String errorMsg) {
        ApiResponse apiResponse = new ApiResponse<>();
        apiResponse.setCode(200);
        apiResponse.setErrorCode(errorCode);
        apiResponse.setErrorMsg(errorMsg);
        apiResponse.setStatus(FAIL);
        return apiResponse;
    }

    public boolean isOk() {
        return OK.equals(status);
    }

    public boolean isFail() {
        return FAIL.equals(status);
    }

    public boolean isError() {
        return RESPONSE_ERROR.equals(status) || UNKNOWN_ERROR.equals(status);
    }
}

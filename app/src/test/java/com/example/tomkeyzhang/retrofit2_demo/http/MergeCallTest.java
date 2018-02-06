package com.example.tomkeyzhang.retrofit2_demo.http;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LifecycleOwner;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.tomkeyzhang.retrofit2_demo.ApiResponse;
import com.example.tomkeyzhang.retrofit2_demo.ProgressOperation;
import com.example.tomkeyzhang.retrofit2_demo.Task;
import com.example.tomkeyzhang.retrofit2_demo.call.Call;
import com.example.tomkeyzhang.retrofit2_demo.call.impl.MergeCall;
import com.example.tomkeyzhang.retrofit2_demo.calladapter.CustomCallAdapterFactory;
import com.example.tomkeyzhang.retrofit2_demo.converter.FastJsonConverterFactory;
import com.example.tomkeyzhang.retrofit2_demo.util.Content;
import com.example.tomkeyzhang.retrofit2_demo.util.CountStep;
import com.example.tomkeyzhang.retrofit2_demo.util.DadaCallTestUtil;
import com.example.tomkeyzhang.retrofit2_demo.util.MockDispatcher;
import com.example.tomkeyzhang.retrofit2_demo.util.RestClientV1;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by tomkeyzhang on 4/1/18.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23)
public class MergeCallTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    RestClientV1 restClientV1;
    MockWebServer mockWebServer;

    @Before
    public void create() {
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new MockDispatcher());
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS).build();
        restClientV1 = new Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .client(client)
                .addCallAdapterFactory(new CustomCallAdapterFactory())
                .addConverterFactory(FastJsonConverterFactory.create())
                .build()
                .create(RestClientV1.class);
        //        mockWebServer.setDispatcher();
    }

    @After
    public void destroy() throws IOException {
        mockWebServer.shutdown();
    }

    @org.junit.Test
    public void test404() {
        Call.Interceptor interceptor = Mockito.mock(Call.Interceptor.class);
        ProgressOperation progressOperation = Mockito.mock(ProgressOperation.class);
        ApiResponse<Content> ok1 = ApiResponse.ok(new Content("tomeky1"));
        ApiResponse<Content> error = ApiResponse.responseError(404, "");
        CountStep count = Mockito.mock(CountStep.class);

        Task task1 = ((lifeState, apiResponse) -> {
            count.step1();
            return ok1;
        });

        Task task2 = ((lifeState, apiResponse) -> {
            count.step2();
            assertEquals(ok1, apiResponse);
            return error;
        });

        InOrder inOrder = Mockito.inOrder(progressOperation, count, interceptor);

        //        ApiResponse apiResponse = ApiResponse.responseError(404, "");
        //        Mockito.when(task.doTask()).thenReturn(apiResponse);

        Call<Content> netLiveData = MergeCall.task((command) -> command.run(), Arrays.asList(interceptor), task1, task2);
        netLiveData.progress(progressOperation);

        ApiResponse<Content> apiResponse2 = DadaCallTestUtil.getValueError(netLiveData);
        //验证是否按顺序的执行了拦截器、进度操作和实际任务
        inOrder.verify(interceptor).preExecute();
        inOrder.verify(progressOperation).showProgress();
        inOrder.verify(count).step1();
        inOrder.verify(count).step2();
        inOrder.verify(progressOperation).showFailed();
        inOrder.verify(interceptor).onResponse(error);
        //确保error回调拿到的apiResponse和前面Task返回的是同一个
        assertEquals(apiResponse2, error);
    }

    @org.junit.Test
    public void test200_intercept_return_true() {
        Call.Interceptor interceptor = Mockito.mock(Call.Interceptor.class);
        ApiResponse<Content> ok = ApiResponse.ok(new Content("tomeky1"));
        ApiResponse fail = ApiResponse.fail("11", null);

        Task task1 = ((lifeState, apiResponse) -> ok);

        Task task2 = ((lifeState, apiResponse) -> {
            assertEquals(ok, apiResponse);
            return fail;
        });

        Mockito.when(interceptor.onResponse(fail)).thenReturn(true);

        Call<Content> call = MergeCall.task((command) -> command.run(), Arrays.asList(interceptor), task1, task2);
        ApiResponse<Content> apiResponse2 = DadaCallTestUtil.getValueFail(call);

        //因为被拦截了，这时应该拿不到回调
        assertNull(apiResponse2);
    }


    @org.junit.Test
    public void test200_intercept_return_true1() {
        LifecycleOwner lifecycleOwner=null;
        Task task1 = ((lifeState, apiResponse) -> ApiResponse.ok(new Content("test")));

        Task task2 = ((lifeState, apiResponse) -> ApiResponse.fail("11", null));

        Call<Content> call = MergeCall.task(task1, task2);

        call.enqueue(lifecycleOwner,apiResponse -> {
            if(apiResponse.isOk()){
                //更新UI
            }else{
                //显示错误信息
            }
        });
        ApiResponse<Content> apiResponse2 = DadaCallTestUtil.getValueFail(call);

        //因为被拦截了，这时应该拿不到回调
        assertNull(apiResponse2);
    }

    @org.junit.Test
    public void test_cancelled() {
        ApiResponse<Content> ok = ApiResponse.ok(new Content("tomeky1"));
        ApiResponse fail = ApiResponse.fail("11", null);

        CountStep countStep = Mockito.mock(CountStep.class);
        InOrder inOrder = Mockito.inOrder(countStep);
        Task task1 = ((lifeState, apiResponse) -> {
            assertNull(apiResponse);
            lifeState.cancel();
            countStep.step1();
            return ok;
        });

        Task task2 = ((lifeState, apiResponse) -> {
            countStep.step2();
            assertEquals(ok, apiResponse);
            return fail;
        });


        Call<Content> netLiveData = MergeCall.task((command) -> command.run(), new ArrayList<>(), task1, task2);
        Content content = DadaCallTestUtil.getValueOk(netLiveData);

        //验证执行了step1,没有执行step2
        inOrder.verify(countStep).step1();
        inOrder.verify(countStep, Mockito.never()).step2();
        //因为被拦截了，这时应该拿不到content
        assertNull(content);
    }

    @org.junit.Test
    public void test_doTask_return_null() {
        ApiResponse<Content> ok = ApiResponse.ok(new Content("tomeky1"));

        Task task1 = ((lifeState, apiResponse) -> ok);
        Task task2 = ((lifeState, apiResponse) -> null);

        Call<Content> netLiveData = MergeCall.task((command) -> command.run(), new ArrayList<>(), task1, task2);
        ApiResponse<Content> apiResponse = DadaCallTestUtil.getValueError(netLiveData);

        //因为Task中返回了null，是个异常情况，程序应该正常返回一个error的apiResponse
        assertEquals(apiResponse.getThrowable().getMessage(), "no response,request may be cancelled!");
    }

    @org.junit.Test
    public void test200ok_with_jsonObject_jsonArray_customObject() {
        Task<Void,JSONObject> task1 = ((lifeState, apiResponse) -> restClientV1.okObject("").execute());
        Task<JSONObject,JSONArray> task2 = ((lifeState, apiResponse) -> {
            //检查前一步返回结果是否正确
            Assert.assertEquals(apiResponse.getContent().getString("name"),"tomkey_object");
            return restClientV1.okArray("").execute();
        });
        Task<JSONArray,Content> task3 = ((lifeState, apiResponse) -> {
            //检查前一步返回结果是否正确
            String name=apiResponse.getContent().getJSONObject(0).getString("name");
            assertEquals(name,"tomkey_item");
            return restClientV1.ok("").execute();
        });
        Call<Content> netLiveData = MergeCall.task((command) -> command.run(), new ArrayList<>(), task1, task2,task3);
        Content content = DadaCallTestUtil.getValueOk(netLiveData);

        //检查最终返回结果是否正确
        Assert.assertEquals(content.getName(), "tomkey");
    }
}

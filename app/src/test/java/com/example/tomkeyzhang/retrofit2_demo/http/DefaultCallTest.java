package com.example.tomkeyzhang.retrofit2_demo.http;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LifecycleOwner;

import com.example.tomkeyzhang.retrofit2_demo.ApiResponse;
import com.example.tomkeyzhang.retrofit2_demo.CheckTokenInterceptor;
import com.example.tomkeyzhang.retrofit2_demo.ProgressOperation;
import com.example.tomkeyzhang.retrofit2_demo.call.Call;
import com.example.tomkeyzhang.retrofit2_demo.calladapter.CustomCallAdapterFactory;
import com.example.tomkeyzhang.retrofit2_demo.converter.FastJsonConverterFactory;
import com.example.tomkeyzhang.retrofit2_demo.util.Content;
import com.example.tomkeyzhang.retrofit2_demo.util.DadaCallTestUtil;
import com.example.tomkeyzhang.retrofit2_demo.util.MockDispatcher;
import com.example.tomkeyzhang.retrofit2_demo.util.RestClientV1;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


/**
 * Created by tomkeyzhang on 28/12/17.
 */
@RunWith(JUnit4.class)
public class DefaultCallTest {

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
                .addCallAdapterFactory(new CustomCallAdapterFactory(CheckTokenInterceptor.INSTANCE))
                .addConverterFactory(FastJsonConverterFactory.create())
                .build()
                .create(RestClientV1.class);

        //        mockWebServer.setDispatcher();
        ProgressOperation progress=ProgressOperation.NONE;
        LifecycleOwner lifecycleOwner=null;
        restClientV1.ok("1").progress(progress).ok(content -> {
            System.out.println(content.getName());
        }).enqueue(lifecycleOwner);
    }

    @After
    public void destroy() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testSyn200Ok() {
        Call<Content> netLiveData = restClientV1.ok("");
        ApiResponse<Content> apiResponse = netLiveData.execute();
        assertNotNull(apiResponse.getContent());
    }

    @Test
    public void testSyn200Unknown() {
        Call<Content> netLiveData = restClientV1.unknown200("");
        ApiResponse<Content> apiResponse = netLiveData.execute();
        assertEquals(apiResponse.getCode(), 200);
        assertThat(apiResponse.getContent(), CoreMatchers.nullValue());
    }

    @Test
    public void testSyn200Fail() {
        Call<Content> netLiveData = restClientV1.fail("");
        ApiResponse<Content> apiResponse = netLiveData.execute();
        assertEquals(apiResponse.getCode(), 200);
        assertTrue(apiResponse.isFail());
    }

    @Test
    public void testSyn404() {
        Call<Content> netLiveData = restClientV1.error404("");
        ApiResponse<Content> apiResponse = netLiveData.execute();
        assertEquals(apiResponse.getCode(), 404);
    }

    @Test
    public void testSyn502() {
        Call<Content> netLiveData = restClientV1.error502("");
        ApiResponse<Content> apiResponse = netLiveData.execute();
        assertEquals(apiResponse.getCode(), 502);
    }

    @Test
    public void testSynThrowable() {
        Call<Content> netLiveData = restClientV1.errorThrowable("");
        ApiResponse<Content> apiResponse = netLiveData.execute();
        assertTrue(apiResponse.isError());
        assertEquals(apiResponse.getThrowable().toString(), "java.net.SocketTimeoutException: timeout");
    }

    @Test
    public void test200Ok() {
        Call<Content> netLiveData = restClientV1.ok("");
        Content content = DadaCallTestUtil.getValueOk(netLiveData);
        assertNotNull(content);
    }

    @Test
    public void test200Unknown() {
        Call<Content> netLiveData = restClientV1.unknown200("");
        ApiResponse<Content> apiResponse = DadaCallTestUtil.getValueError(netLiveData);
        assertEquals(apiResponse.getCode(), 200);
        assertThat(apiResponse.getContent(), CoreMatchers.nullValue());
    }

    @Test
    public void test200Fail() {
        Call<Content> netLiveData = restClientV1.fail("");
        ApiResponse<Content> apiResponse = DadaCallTestUtil.getValueFail(netLiveData);
        assertEquals(apiResponse.getCode(), 200);
        assertTrue(apiResponse.isFail());
    }

    @Test
    public void test404() {
        Call<Content> netLiveData = restClientV1.error404("");
        ApiResponse<Content> apiResponse = DadaCallTestUtil.getValueError(netLiveData);
        assertEquals(apiResponse.getCode(), 404);
    }

    @Test
    public void test502() {
        Call<Content> netLiveData = restClientV1.error502("");
        ApiResponse<Content> apiResponse = DadaCallTestUtil.getValueError(netLiveData);
        assertEquals(apiResponse.getCode(), 502);
    }

    @Test
    public void testThrowable() {
        Call<Content> netLiveData = restClientV1.errorThrowable("");
        ApiResponse<Content> apiResponse = DadaCallTestUtil.getValueError(netLiveData);
        assertTrue(apiResponse.isError());
        assertEquals(apiResponse.getThrowable().toString(), "java.net.SocketTimeoutException: timeout");
    }





}

package com.example.tomkeyzhang.retrofit2_demo;

import com.example.tomkeyzhang.retrofit2_demo.calladapter.StringCallAdapterFactory;
import com.example.tomkeyzhang.retrofit2_demo.converter.StringConverterFactory;
import com.example.tomkeyzhang.retrofit2_demo.util.MockDispatcher;
import com.example.tomkeyzhang.retrofit2_demo.util.RestClientV1;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
//@RunWith(JUnit4.class)
public class StringReturnTypeTest {

    MockWebServer mockWebServer;
    RestClientV1 restClientV1;

    @Test
    public void test() {
        System.out.println(restClientV1.getOrderDetail(1));
    }

    @Before
    public void create() {
    mockWebServer = new MockWebServer();
    mockWebServer.setDispatcher(new MockDispatcher());
    OkHttpClient client = new OkHttpClient.Builder().build();
    restClientV1 = new Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(client)
            .addCallAdapterFactory(new StringCallAdapterFactory())
            .addConverterFactory(new StringConverterFactory())
            .build()
            .create(RestClientV1.class);
    }

    @After
    public void destroy() throws IOException {
        mockWebServer.shutdown();
    }


}
# Retrofit2之CallAdapter和Converter
## 一、这篇文章适合谁？
先看一个问题，Retrofit2中定义的接口可以直接返回一个ResponseBody的String吗？

```java
public interface RestClientV1 {
	@GET("order/detail")
	String getOrderDetail(@Query("orderId") long orderId);
｝
```

如果你不能肯定的回答可以，同时不能清楚的知道该怎么做，推荐阅读这篇文章。这是一篇retrofit2的进阶用法的文章，如果不熟悉retrofit2的基本用法，建议先去[官网](http://square.github.io/retrofit/)看一下教程，再过来看这篇文章。如果你正在考虑如何使用retrofit来封装一个网络层，这篇文章应该非常适合你。

## 二、如何实现上面的功能
### 2.1 定义`StringCallAdapterFactory`和	`StringCallAdapter`

```java
public class StringCallAdapterFactory extends CallAdapter.Factory {
    @Nullable
    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if(returnType == String.class)
            return new StringCallAdapter();
    	 return null;
    }

    class StringCallAdapter implements CallAdapter<String,String>{
        @Override
        public Type responseType() {
            return String.class;
        }

        @Override
        public String adapt(Call<String> call) {
            try {
                return call.execute().body();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }
    }
}

```
`StringCallAdapterFactory`中`get(..)`方法的作用是返回处理指定`returnType`（接口定义方法的返回类型）的`CallAdapter`，而`CallAdapter`，顾名思义就是`Call`（`retrofit2.Call`）的一个适配器，作用就是将Call转化成自己想要的任意类型（这个逻辑具体是在`adapt(..)`方法里面处理）。特别地，`responseType()`方法的作用是告诉`Converter`，我需要一个String类型的响应数据。
<br>

### 2.2 定义`StringConverterFactory`和`StringConverter`

```java
public class StringConverterFactory extends Converter.Factory {
    @Nullable
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        if (type == String.class) {
            return new StringConverter();
        }
        return null;
    }

    class StringConverter implements Converter<ResponseBody, String> {

        @Override
        public String convert(ResponseBody value) throws IOException {
            return value.string();
        }
    }
}
```
`Converter`的作用是把http响应数据`ResponseBody`转化为需要的类型，这个类型正是前面`responseType()`方法定义的（这里是`String.class`）。在上面的`responseBodyConverter(..)`方法中，我们只对我们关心的`type`(`String.class`)进行了处理，返回我们自定义的`StringConverter`，这个转换器也很简单，直接调用`ResponseBody.string()`方法返回字符串。

### 2.3 测试

```java
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
@Test
public void test() {
   System.out.println(restClientV1.getOrderDetail(1));
}   
```
> 运行结果： hi man,this is order detail(orderId=1)

[测试类源码](https://github.com/TomkeyZhang/Retrofit2-CallAdapter-Converter-Demo/blob/master/app/src/test/java/com/example/tomkeyzhang/retrofit2_demo/StringReturnTypeTest.java)
## 三、原理解析
分析框架原理的时候，咱们得首先找到切入点，或者是疑惑点。基于上面的例子，有3个疑惑点：

1. `RestClientV1.getOrderDetail`方法返回类型String是如何起作用的？
2. `CallAdapterFactory`是如何起作用的？
3. `ConverterFactory`是如何起作用的？


### 3.1 从`returnType`(String.class)到`CallAdapter<T, R>`(StringCallAdapter)
在源码中我们先找到获取`CallAdapter<T, R>`的地方

```java
private CallAdapter<T, R> createCallAdapter() {
      Type returnType = method.getGenericReturnType();
      //省略
      Annotation[] annotations = method.getAnnotations();
      try {
        //<-关键代码
        return (CallAdapter<T, R>) retrofit.callAdapter(returnType, annotations);
      } catch (RuntimeException e) { // Wide exception range because factories are user code.
        throw methodError(e, "Unable to create call adapter for %s", returnType);
      }
    }
```
> 代码中的method对象就是`RestClientV1.getOrderDetail(..)`方法

上面关键代码处以`returnType`(String.class)为参数调用了`retrofit.callAdapter(..)`方法获取一个适配器，接着看

```java
public CallAdapter<?, ?> callAdapter(Type returnType, Annotation[] annotations) {
    return nextCallAdapter(null, returnType, annotations);
}
  
public CallAdapter<?, ?> nextCallAdapter(@Nullable CallAdapter.Factory skipPast, Type returnType,
      Annotation[] annotations) {
    int start = adapterFactories.indexOf(skipPast) + 1;
    for (int i = start, count = adapterFactories.size(); i < count; i++) {
    	CallAdapter<?, ?> adapter = adapterFactories.get(i).get(returnType, annotations, this);
      	if (adapter != null) {
        	return adapter;
      }
    }
}
```
核心是这一行`adapterFactories.get(i).get(returnType, annotations, this)`，就是循环调用`CallAdapter.Factory`的get方法来获取一个可用的适配器，一旦找到就返回。注意这里的`returnType`就是`String.class`，再回过头来看我们之前的2.1的代码，当执行到`StringCallAdapterFactory`，会返回`StringCallAdapter`。


### 3.2 从`responseType`(String.class)到`Converter<F, T>`(StringConverter)

同样的，在源码中我们先找到获取`Converter<F, T>`的地方

```java
private Converter<ResponseBody, T> createResponseConverter() {
      Annotation[] annotations = method.getAnnotations();
      try {
        return retrofit.responseBodyConverter(responseType, annotations);
      } catch (RuntimeException e) { // Wide exception range because factories are user code.
        throw methodError(e, "Unable to create converter for %s", responseType);
      }
}
    
public <T> Converter<ResponseBody, T> responseBodyConverter(Type type, Annotation[] annotations) {
       return nextResponseBodyConverter(null, type, annotations);
}

```
上面的`retrofit.responseBodyConverter(..)`中的`responseType`参数正是`StringCallAdapter`中返回的`String.class`，再看核心的获取`Converter`的方法，跟获取`CallAdapter`的方法基本一致，结合2.2的代码，我们发现，当传入参数`type`为`String.class`的时候，会返回`StringConverter`

```java
public <T> Converter<ResponseBody, T> nextResponseBodyConverter(
      @Nullable Converter.Factory skipPast, Type type, Annotation[] annotations) {
    int start = converterFactories.indexOf(skipPast) + 1;
    for (int i = start, count = converterFactories.size(); i < count; i++) {
      Converter<ResponseBody, ?> converter =
          converterFactories.get(i).responseBodyConverter(type, annotations, this);
      if (converter != null) {
        //noinspection unchecked
        return (Converter<ResponseBody, T>) converter;
      }
    }
}
```

### 3.3 `CallAdapter<T, R>`(StringCallAdapter)和`Converter<F, T>`(StringConverter) 如何起作用？
上面拿到的StringCallAdapter和StringConverter会被存放在一个`ServiceMethod`对象中

```java
final class ServiceMethod<R, T> {
	final CallAdapter<R, T> callAdapter;
	private final Converter<ResponseBody, R> responseConverter;
}
```
再看看这两个对象分别是在哪个地方起作用的

* `CallAdapter<T, R>`的作用处

```java
public <T> T create(final Class<T> service) {
    //省略部分代码
    return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[] { service },
        new InvocationHandler() {
          @Override public Object invoke(Object proxy, Method method, @Nullable Object[] args)
              throws Throwable {
            //省略部分代码
            ServiceMethod<Object, Object> serviceMethod =
                (ServiceMethod<Object, Object>) loadServiceMethod(method);
            OkHttpCall<Object> okHttpCall = new OkHttpCall<>(serviceMethod, args);
            return serviceMethod.callAdapter.adapt(okHttpCall);//<- 关键代码在这里
          }
        });
  }
```
上面的代码使用动态代理返回了`service`接口的一个实现类，调用`restClientV1.getOrderDetail(1)`方法时，它的返回值就是`invoke(..)`方法的返回值，这个返回值就是调用`callAdapter.adapt(..)`再看2.1中咱们自定义适配器的代码

```java
@Override
public String adapt(Call<String> call) {
    try {
        return call.execute().body();
    } catch (IOException e) {
        e.printStackTrace();
    }
    return "";
}
```
我们直接将`body`以`String`的形式返回了。这个设计非常的巧妙和灵活，我们可以将一个http请求包装成任何对象返回。

* `Converter<F, T>`的作用处

前面我们看到实际执行http请求的是`OkHttpCall`，在请求执行完成后有一段解析`Response`的代码

```java
Response<T> parseResponse(okhttp3.Response rawResponse) throws IOException {
    ResponseBody rawBody = rawResponse.body();
	//省略代码
    ExceptionCatchingRequestBody catchingBody = new ExceptionCatchingRequestBody(rawBody);
    try {
      T body = serviceMethod.toResponse(catchingBody);//<- 关键代码在这里
      return Response.success(body, rawResponse);
    } catch (RuntimeException e) {
      // If the underlying source threw an exception, propagate that rather than indicating it was
      // a runtime exception.
      catchingBody.throwIfCaught();
      throw e;
    }
  }
```
这个方法的作用是将`okhttp3.Response`转化为`retrofit2.Response`。
> 因为retrofit2最终也是使用okhttp来发起http请求的。
再看上面关键代码的实现

```java
/** Builds a method return value from an HTTP response body. */
R toResponse(ResponseBody body) throws IOException {
   return responseConverter.convert(body);
}
```
其实很简单，直接调用了我们自定义的`StringConverter`来获取一个自定义对象，再看2.2中咱们自定义的转换器的代码

```java
@Override
public String convert(ResponseBody value) throws IOException {
    return value.string();
}
```
这个也非常简单，就不多解释了。

**最后咱们再用图形的方式来捋一下整个调用流程：**
![retrofit2-calladapter-convert](https://raw.githubusercontent.com/TomkeyZhang/test1/master/retrofit2-calladapter-convert.png)


## 四、进阶使用
前面那个例子没有什么实际的意义，下面咱们使用retrofit的这个设计做一个有意义的App网络层封装，在这之前我们先看看在网络层封装时基本都会遇到的几个问题：

1. 网络请求返回时，需要防止组件(Activity／Fragment)已被销毁时导致的崩溃
2. 监听请求状态，灵活实现loading、success和error
3. 方便串行发起多个网络请求
4. 便于实现请求前后的业务拦截，比如登录token失效自动跳转登录页面
5. Presenter/ViewModel层应该不耦合于具体的网络框架，一旦换网络框架对这一层没有影响

为了解决上面的问题，咱们设计自己的`Call`接口，在看`Call`接口的代码前，有几点需要说明下：

* 一般我们需要跟服务端约定一个统一的数据返回格式，在这个示例中，约定如下：

```java
public class ApiResponse<T> {
	/**
     * api业务响应状态
     */
    private String status;
    /**
     * api业务数据
     */
    private T content;
    /**
     * api业务响应失败错误码
     */
    private String errorCode;
    /**
     * 错误字符串信息
     */
    private String errorMsg;
}
```
> 正常情况下，服务端会返回请求业务成功(ok)或者失败(fail)；异常情况下，客户端增加一个error的status，用于表示非200的请求以及调用抛出异常的情况

* 为了适配不同的Loading样式，我们设计了`ProgressOperation`接口

```java
public interface ProgressOperation {
    /**加载数据失败*/
    void showFailed();
	/**加载数据成功*/
    void showContent();
	/**开始加载数据*/
    void showProgress();
}
```
> 这样当我们需要使用不同的UI展示形式（如：ProgressDialog或者Progressbar）来实现Loading的时候，只要分别做一个实现类即可

* 示例中使用了Android官方的lifecycle组件，用来管理组件的生命周期，没用过的同学可以去官网简单查看一下[教程](https://developer.android.com/topic/libraries/architecture/lifecycle.html)，不需要了解太深，只要知道大概即可

**下面是自定义的`Call`接口代码：**

```java
public interface Call<T> {
    /**
     * 设置http200 ok的回调
     */
    Call<T> ok(@NonNull Observer<T> observer);

    /**
     * 设置http200 fail的回调
     */
    Call<T> fail(@NonNull Observer<ApiResponse<T>> observer);

    /**
     * 设置error的回调
     */
    Call<T> error(@NonNull Observer<ApiResponse<T>> observer);

    /**
     * 设置进度监听
     */
    Call<T> progress(@NonNull ProgressOperation progressOperation);

    /**
     * 执行异步请求，绑定组件生命周期(获取全部状态结果)
     */
    void enqueue(@NonNull LifecycleOwner owner, @NonNull Observer<ApiResponse<T>> observer);

    /**
     * 执行异步请求，绑定组件生命周期（获取部分状态结果）
     */
    void enqueue(@NonNull LifecycleOwner owner);

    /**
     * 执行异步请求，但不需要绑定组件生命周期(获取部分状态结果)
     */
    void enqueue();

    /**
     * 执行异步请求，但不需要绑定组件生命周期(获取全部状态结果)
     */
    void enqueue(@NonNull Observer<ApiResponse<T>> observer);

    /**
     * 发起同步网络请求
     */
    ApiResponse<T> execute();

    /**
     * 取消请求
     * 1、对于单个http请求，取消时如果还没有开始执行，则不执行；如果在执行中，则会确保执行结束不会回调，不确保一定能被取消
     * 2、对于多个连续http请求，除了1的特性外，取消后剩下的未开始执行请求也不会被执行
     */
    void cancel();

    /**
     * 是否被取消
     *
     * @return
     */
    boolean isCancelled();
}


```
> 上面的代码注释已经比较详细，大家可以仔细看下
<br>

* 我们以Android官方的LiveData为基础做了单个请求[Call的实现类](https://github.com/TomkeyZhang/Retrofit2-CallAdapter-Converter-Demo/blob/master/app/src/main/java/com/example/tomkeyzhang/retrofit2_demo/call/impl/DefaultCall.java)，实现这块就不再讲解，有兴趣大家可以自行查看源码。下面咱们看一下如何使用这个`Call`

```java
restClientV1.ok("1").progress(progress).ok(content -> {
     System.out.println(content.getName());
}).enqueue(lifecycleOwner);
```
> 其中`lifecycleOwner`可以直接使用supportv26包中的Activity或Fragment，传入这个对象后，如果组件已处于`destroy`状态，则回调不会被执行
<br>

* 对于需要执行多个任务的情况，可以这样用

```java
Task task1 = ((lifeState, apiResponse) -> restClientV1.okArray("1").execute());
Task task2 = ((lifeState, apiResponse) -> restClientV1.ok("1").execute());
Call<Content> call = MergeCall.task(task1, task2);
call.enqueue(lifecycleOwner,apiResponse -> {
     if(apiResponse.isOk()){
           //更新UI
     }else{
           //显示错误信息
     }
});
```
> 上面我们使用了自己定义的Task接口来描述一个任务，执行多个任务的时候，只有上个任务成功，才会执行下一个任务，否则会直接执行回调
<br>

* 对于业务拦截器，我们可以这样定义

```java
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

```
然后

* 对于单个网络请求，在构造`Retrofit`对象时调用这个方法`addCallAdapterFactory(new CustomCallAdapterFactory(CheckTokenInterceptor.INSTANCE))`；
* 对于多个串行网络请求，在生成`MergeCall`的时候传入拦截器

```java
public static Call task(Executor executor, List<Interceptor> interceptors, Task... tasks) {
        return new MergeCall(Arrays.asList(tasks), interceptors, executor);
}
public static Call task(Task... tasks) {
        return task(AsyncTask.THREAD_POOL_EXECUTOR, tasks);
}

//<- 关键在这里
public static Call task(Executor executor, Task... tasks) {
        return MergeCall.task(executor, Arrays.asList(CheckTokenInterceptor.INSTANCE), tasks);
}
```

**小结**

从上面对于单个和多个串行请求的设计和用法中可以看到，我们完全解决了前面提到的5个问题。而且在这种设计下，我们使用自己的`Call`接口做为网络层和其它层交互的纽带，其它层(Presenter/ViewModel、Activity/Fragment)完全不知道我们使用的是什么网络框架，那么如果哪天有一个更好用的网络框架，我们替换起来也是非常方便。最后再放一下本文demo的[源码链接](https://github.com/TomkeyZhang/Retrofit2-CallAdapter-Converter-Demo)。


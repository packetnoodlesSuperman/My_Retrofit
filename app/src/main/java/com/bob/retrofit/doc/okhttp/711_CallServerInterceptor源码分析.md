这是拦截链的最后一个拦截器，它将向网络写请求信息，还读响应信息，同时还有做一些错误处理

```
@Override
public Response intercept(Chain chain) throws IOException {
    RealInterceptorChain realChain = (RealInterceptorChain) chain;
    HttpCodec httpCodec = realChain.httpStream();
    StreamAllocation streamAllocation = realChain.streamAllocation();
    
    //获取开始请求时间，在写缓存时 开始时间就是在这里获取的
    long sentRequestMillis = System.currentTimeMills();
    
    //从request中获取request header，然后写入到httpCodec中，httpCodec是用来编码解码请求
    httpCodec.writeRequestHeader(request);
    Response.Builder responseBuilder = null;
    if (HttpMethod.permitsRequestBody(request.method()) && request.body() != null) {
        //
    }
}
```
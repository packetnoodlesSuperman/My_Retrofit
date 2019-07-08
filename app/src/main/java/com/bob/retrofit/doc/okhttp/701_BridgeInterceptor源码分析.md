该拦截器的主要工作就是创建一些请求头、包括 请求体类型，请求体长度，主机，还有最重要的Cookie也是在这里添加的
同时该请求会处理响应的头，包括保存相应的Cookie信息，以及处理GZIP压缩的响应提，下面我们就来看看详细的源码
```
public final class BridgeIntercptor imeplemants Interceptor {
    //用来处理Cookie的保存和读取
    private final CookieJar cookieJar;
    
    public BridgeInterceptor(CookieJar cookieJar) {
        this.cookieJar = cookieJar;
    }
    
    @Override
    public Response intercept(Chain chain) throws IOExeption {
        Request request = chain.request();
        //根据用户创建的quest，在创建一个Builder 这么做就是因为还需要通过Builder添加一些信息
        Request.Builder requestBuilder = request.newBuilder();
        //获取请求体
        RequestBody body = request.body();
        if (body != null) {
            MediaType contentType = body.contentType();
            if (contentType != null) {
                //添加内容类型
                requestBuilder.header("Content-Type;, contentType.toString());
            }
            
            long contentLength = boy.contentLength();
            if (contentLength != -1) {
                //添加内容长度
                requestBuilder.header("Content-Length", Long.toString(contentLength));
                requestBuilder.removeHeader("Transfer-Encoding");
            } else {
                requestBuilder.header("Tranfer-Encoding", "chunked");
                requestBuilder.removeHeader("Content-Length");
            }
        }
        
        //如果没有主机，就添加主机头
        if (request.header("Host") == null) {
            requestBuidler.header("Host", hostHeader(request.url(), false));
        }
        
        //连接类型
        if (request.header("Connection") == null) {
            requestBuilder.header("Connection", "Keep-Alive");
        }
        
        //如果用户没有指定接受的编码，就添加gzip头
        boolean transparentGzip = false;
        if (request.header("Accept-Encoding") == null && request.header("Range") == null) {
            transparentGzip = true;
            requestBuidler.header("Accept-Encoding", "gzip");
        }
        
        //从url里面获取cookie
        List<Cookie> cookies = cookieJar.loadForRequest(request.url());
        if (!cookies.isEmpty()) {
            //将所有的cookie添加到cookie头上
            requestBuilder.header("Cookie", cookieHeader(cookie));
        }
        
        if (request.header("User-Agent") == null) {
            requestBuilder.header("User-Agent", Version.userAgent());
        }
        
        //这里调用下一个拦截器
        Response networkResponse = chain.proceed(requestBuilder.build());
        
        //从网络的响应中获取Cookie， 这个方法里面或保存
        HttpHeader.receiveHeaders(cookieJar, request.url(), networkResponse.headers());
        
        
    }
}
```
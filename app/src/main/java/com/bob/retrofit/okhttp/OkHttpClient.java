package com.bob.retrofit.okhttp;

/**
 * String url = "http://wwww.baidu.com";
 * OkHttpClient okHttpClient = new OkHttpClient();
 * final Request request = new Request.Builder()
 *      .url(url)
 *      .get()//默认就是GET请求，可以不写
 *      .build();
 * Call call = okHttpClient.newCall(request);
 * call.enqueue(new Callback() {
 *      @Override
 *      public void onFailure(Call call, IOException e) {
 *          Log.d(TAG, "onFailure: ");
 *      }
 *      @Override
 *      public void onResponse(Call call, Response response) throws IOException {
 *          Log.d(TAG, "onResponse: " + response.body().string());
 *      }
 * });
 */
public class OkHttpClient implements Cloneable, Call.Factory, WebSocket.Factory {

    Dispatcher dispatcher;

    @Override
    public Call newCall(Request request) {
        return RealCall.newRealCall(this, request, false /* for web socket */);
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }

    @Override
    public WebSocket newWebSocket(Request request, WebSocketListener listener) {
        return null;
    }
}

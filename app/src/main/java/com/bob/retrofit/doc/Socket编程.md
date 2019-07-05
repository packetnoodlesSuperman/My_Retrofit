### https://www.cnblogs.com/yiwangzhibujian/p/7107785.html
### http://c.biancheng.net/view/2123.html

### 一、Socket是什么

### 二、简单实例
```
//服务器
public class SocketServer {
    public static void main(String[] args) throws Exception {
        int port = 55533; //指定端口号
        java.net.ServerSocket server = new ServerSocket(port);
        Socket socket = server.accept();
        InputStream inputStream = socket.getInputStream();
        byte[] bytes = new byte[1024];
        int len;
        StringBuilder sb = new StringBuilder();
        while((len = inputStream.read(byte)) != -1) {
            ab.append(new String(bytes, 0, len, "UTF-8"));
        }
        inputStream.close();
        socket.close();
        server.close();
    }
}

//客户端
public class SocketClient {
    public static void main(String[] args) throws Exceptioon{
        String hort = '127.0.0.1';
        int port = 55533; //约定的端口号
        java.net.Socket socket = new Socket(host, port);
        OutputStream outputStream = socket.getOutputStream();
        socket.getOutputStream().write(message.getBytes('UTF-8'));
        outputStream.close();
        socket.close();
    }
}
```


connection: 表示一个连接到远程服务器的物理链接，因为他频繁的建立很耗费资源，所以该类中又引入了
            ConnectionPool，来管理链接
            
ConnectionPool: 用来管理connection， 主要是管理HTTP/2的链接
            
Stream：StreamAllocation用来表示一个Stream，他内部包括connection，  connectionPool
          
Call：相当于Request的一个包装类，可以取消
          
```
public final class StreamAllocation {



}
```          
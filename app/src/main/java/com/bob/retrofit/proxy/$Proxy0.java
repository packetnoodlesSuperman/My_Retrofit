package com.bob.retrofit.proxy;
import java.lang.reflect.Method;
import com.bob.retrofit.proxy.Proxy;
import com.bob.retrofit.proxy.InvocationHandler;
public final class $Proxy0 extends Proxy implements com.bob.retrofit.proxy.Person {
   public $Proxy0(InvocationHandler var1) {
       super(var1);
   }
   public final void eat() {
       try {
           Method md = com.bob.retrofit.proxy.Person.class.getMethod("eat", new Class[]{});
           this.h.invoke(this, md, (Object[])null);
       } catch (Exception e) {
           e.printStackTrace();
       } catch (Throwable throwable) {
           throwable.printStackTrace();
       }
   }
}
一、Type的其他四个子类
* ParameterizedType (参数化类型)
* TypeVariable (类型变量 描述类型 是各种类型变量的公共高级接口)
* GenericArrayType (泛型数组)
* WildcardType (wildcard 通配符的意思)

--------------------- ---------------------
无论<>中有几层<>嵌套，这个方法仅仅脱去最外层的<>，之后剩下的内容就作为这个方法的返回值，所以其返回值类型不一定。
例如：
1. List<ArrayList> a1;//这里返回的是，ArrayList，Class类型
2. List<ArrayList<String>> a2;//这里返回的是ArrayList<String>，ParameterizedType类型
3. List<T> a3;//返回的是T，TypeVariable类型
4. List<? extends Number> a4; //返回的是WildcardType类型
5. List<ArrayList<String>[]> a5;//GenericArrayType
--------------------- ---------------------

* 什么是参数化类型？Type type = Field.getGenericType(); 这个type就是参数化类型
> 使用泛型中会提到这个概念。最直观的例子，List<String> 这就是个参数化类型，而 List 就是个原生类型
* 什么是类型变量
> Method中的方法 TypeVariable<Method>[] getTypeParameters();
> Class中的方法 public synchronized TypeVariable<Class<T>>[] getTypeParameters()
> Constructor中的方法  public TypeVariable<Constructor<T>>[] getTypeParameters() {
> GenericDeclaration接口的包装 实现GenericDeclaration接口的类包括Class(类), Method(方法), Constructor(构造器)
* Type与Class的区别
> Class是Type的一种，而像数组、枚举等“类型”是相对于Class来说
> Type接口是java编程语言中所有类型的公共高级接口，它们包括原始类型、参数化类型、数组类型、类型变量和基本类型
> Type的直接子类只有一个，也就是Class，代表着类型中的原始类型以及基本类型

二、Type
```
public interface Type {

    default String getTypeName() {
        return toString();
    }
}
```

三、ParameterizedType (实现类ParameterizedTypeImpl)
```
public interface ParameterizedType extends Type {

    //该方法返回参数化类型<>中的实际参数类型 (看下面的例子)
    Type[] getActualTypeArguments();

    //该方法的作用是返回当前的ParameterizedType的类型
    Type getRawType();

    //返回ParameterizedType类型所在的类的Type。这主要是对嵌套定义的内部类而言的
    //如Map.Entry<String, Object>这个参数化类型返回的事Map(因为Map.Entry这个类型所在的类是Map)的类型
    Type getOwnerType();
}

//demo
public class ParameterizedBean {
    List<String> list_1;
    List list_2;
    Map<String, Long> map_1;
    Map map_2;
    Map.Entry<Long, Short> entry;

    //测试代码
    public static void main(String[] args) {
        Field[] fields = ParameterizedBean.class.getDeclaredFields();
        for(Field field : fields) {
            System.out.print(f.getName()+": "+(f.getGenericType() instanceof ParameterizedType)+"  -->")
            if(!f.getGenericType() instanceof ParameterizedType) {
                return
            }
            ParameterizedType pType =(ParameterizedType) f.getGenericType();
            Type[] types =pType.getActualTypeArguments();
            for(Type type : types) {
                System.out.print("   类型："+t.getTypeName());
            }

            //getRawType 方法
            System.out.print("RawType："+pType.getRawType().getTypeName();

            //getOwnerType 方法
            Type t = pType.getOwnerType();
            if(t == null){
                System.out.print("OwnerType:Null     ");
            }else{
                System.out.print("OwnerType："+t.getTypeName());
            }
        }
    }

    //打印结果
    list_1: true --> 类型：java.lang.String                        RawType：java.util.List      OwnerType:Null
    list_2: false
    map_1: true  --> 类型：java.lang.String 类型：java.lang.Long   RawType：java.util.Map       OwnerType:Null
    map_2: false
    entry: true  --> 类型：java.lang.Long   类型：java.lang.Short  RawType：java.util.Map$Entry OwnerType:java.util.Map
}
```

四、TypeVariable 类型变量类型（或者叫“泛型变量”更准确些）
//类型变量声明（定义）的时候不能有下限（既不能有super），否则编译报错
```
public interface TypeVariable<D extends GenericDeclaration> extends Type {

     //获得该“范型变量”的上限（上边界），若无显式定义（extends），默认为Object。
     //类型变量的上限可能不止一个，因为可以用&符号限定多个
     //(这其中有且只能有一个为类或抽象类，且必须放在extends后的第一个，即若有多个上边界，则第一个&后必为接口）
     Type[] getBounds();

     //获得声明（定义）这个“范型变量”的类型及名称
     D getGenericDeclaration();

     //获得这个“范型变量”在声明（定义）时候的名称
     String getName();
}

//目前实现GenericDeclaration接口的类包括Class(类), Method(方法), Constructor(构造器)
public interface GenericDeclaration extends AnnotatedElement {
    //获取当前“实体”上声明的“泛型变量"
    public TypeVariable<?>[] getTypeParameters();
}

//这个接口(AnnotatedElement)的对象代表了在当前JVM中的一个"被注解元素"(可以是Class，Method，Field，Constructor，Package等)
public interface AnnotatedElement {
    //如果指定类型的注解出现在当前元素上，则返回true，否则将返回false
    default boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }
    //如果在当前元素上存在参数所指定类型（annotationClass）的注解，则返回对应的注解，否则将返回null
    <T extends Annotation> T getAnnotation(Class<T> annotationClass);
    //返回在这个元素上的所有注解。如果该元素没有注释，则返回值是长度为0的数组
    Annotation[] getAnnotations();
    default <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
         return AnnotatedElements.getDirectOrIndirectAnnotationsByType(this, annotationClass);
    }
     default <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        Objects.requireNonNull(annotationClass);
        for (Annotation annotation : getDeclaredAnnotations()) {
            if (annotationClass.equals(annotation.annotationType())) {
                return annotationClass.cast(annotation);
            }
        }
        return null;
    }
    default <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return AnnotatedElements.getDirectOrIndirectAnnotationsByType(this, annotationClass);
    }
    Annotation[] getDeclaredAnnotations();
}


//demo
public class TypeVariableBean<K extends Integer, V> {
     K key;
     V value;
     public static void main(String[] args) throws Exception {
        Type[] types = Main.class.getTypeParameters();
        for(Type type : types){
            TypeVariable t = (TypeVariable)type;
            int size = t.getBounds().length;

            System.out.println(t.getGenericDeclaration());
            System.out.println(t.getBounds()[size - 1]);
        }
    }
}

//输出结果
class com.xxx.xxx.Main
interface java.lang.Integer
K
class com.xxx.xxx.Main
class java.lang.Object
V
```

五、GenericArrayType
//用来描述ParameterizedType、TypeVariable类型的数组；即List<T>[] 、T[]等；
```
public interface GenericArrayType extends Type {
    Type getGenericComponentType();
}

public class GenericArrayTypeBean<T> {
    private T[] t;
    private List<String>[] list;
    public static void main(String[] args) {
        Field field_list = GenericArrayTypeBean.class.getDeclareField("list");
        Type type_list = field_list.getGenericType();
    }
}
```

六、WildcardType
```
public interface WildcardType extends Type {
   Type[] getUpperBounds();
   Type[] getLowerBounds();
}
```
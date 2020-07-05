## PerformanceTest

From source code:android/animation/PropertyValuesViewHolder


```
native static private long nGetIntMethod(Class targetClass, String methodName);
native static private long nGetFloatMethod(Class targetClass, String methodName);
native static private void nCallIntMethod(Object target, long methodID, int arg);
native static private void nCallFloatMethod(Object target, long methodID, float arg);
```

I am curious about why they use JNI to invoke the method. From the comment, I know a little about the reason.

```
// Cache JNI functions to avoid looking them up twice
private static final HashMap<Class, HashMap<String, Long>> sJNISetterPropertyMap =
        new HashMap<Class, HashMap<String, Long>>();
```


To avoid look them up twice. Sounds reasonable.

But I still curious about the performance between The java reflection and Jni. So I start a test project to test the performance.

The jni code snippet.

```
external fun nGetIntMethod(targetClass: Class<*>, methodName: String): Long
external fun nSetIntMethod(targetClass: Class<*>, methodName: String): Long
external fun nCallSetIntMethod(target: Any, methodId:Long, arg:Int)
external fun nCallGetIntMethod(target: Any, methodId:Long): Int

//from cpp.
extern "C"
JNIEXPORT jlong JNICALL
Java_com_cz_animation_myapplication_MainActivity_nGetIntMethod(JNIEnv *env, jobject thiz,
                                                               jclass target_class,
                                                               jstring method_name) {
    //From jstring get char *
    const char* nativeString = env->GetStringUTFChars(method_name, JNI_FALSE);
    jmethodID methodId=env->GetMethodID(target_class,nativeString,"()I");
    //Release the string
    env->ReleaseStringUTFChars(method_name, nativeString);
    __android_log_print(ANDROID_LOG_DEBUG, "test", "method = %d",  methodId);
    return (jlong)methodId;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_cz_animation_myapplication_MainActivity_nSetIntMethod(JNIEnv *env, jobject thiz,
                                                               jclass target_class,
                                                               jstring method_name) {
    const char* nativeString = env->GetStringUTFChars(method_name, JNI_FALSE);
    jmethodID methodId=env->GetMethodID(target_class,nativeString,"(I)V");
    //Release the string
    env->ReleaseStringUTFChars(method_name, nativeString);
    return (jlong)methodId;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cz_animation_myapplication_MainActivity_nCallSetIntMethod(JNIEnv *env, jobject thiz,
                                                                   jobject target, jlong method_id,
                                                                   jint arg) {
    env->CallVoidMethod(target,(jmethodID)method_id,arg);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cz_animation_myapplication_MainActivity_nCallGetIntMethod(JNIEnv *env, jobject thiz,
                                                                   jobject target,
                                                                   jlong method_id) {
    return env->CallIntMethod(target,(jmethodID)method_id);
}
```

* First, test the JNI by calling the setter and getter method one hundred thousand times.

```
val getterMethodId = nGetIntMethod(Person::class.java, "getAge")
val setterMethodId = nSetIntMethod(Person::class.java, "setAge")
val st1 = measureTimeMillis {
    val person = Person(100, "SuperMan")
    for (i in 0 until 100000) {
        nCallSetIntMethod(person, setterMethodId, i)
        nCallGetIntMethod(person, getterMethodId)
    }
}
sample_text.append("time1:$st1\n")
```

* Second, use the Java Reflection invoking setter and getter method one hundred thousand times.

```
val setterMethod = Person::class.java.getMethod("setAge", Int::class.java)
val getterMethod = Person::class.java.getMethod("getAge")
val st2 = measureTimeMillis {
    val person = Person(100, "SuperMan")
    for (i in 0 until 1000000) {
        setterMethod.invoke(person,i)
        getterMethod.invoke(person)
    }
}
sample_text.append("time2:$st2\n")
```

* Finally, We are calling the method directly.

```
val st3 = measureTimeMillis {
    val person = Person(100, "SuperMan")
    for (i in 0 until 1000000) {
        person.age=i
        person.getAge();
    }
}
sample_text.append("time3:$st3\n")
```



Here are the result printed on the console.

```
//times:10000
//time1:43 time2:47
//time1:42 time2:46

//times:100000
//time1:429 time2:487
//time1:418 time2:469

//times:1000000
//time1:4221
//time2:4664
//time3:2
```

It's clear that the JNI is a little faster than the Java reflection. Maybe that is the performance consumption the comment mentioned to look the function up twice.

But the different between Java reflection and Method calling is huge.

That is why We should avoid using Java reflection if it is not necessary.

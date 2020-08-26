#include <jni.h>
#include <string>
#include <unistd.h>

#define  LOG_TAG    "NativeCourse"

#define  logI(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define  logE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

void println(JNIEnv *env,char* message) {
    jclass syscls = env->FindClass("java/lang/System");
    // Lookup the "out" field
    jfieldID fid = env->GetStaticFieldID(syscls, "out", "Ljava/io/PrintStream;");
    jobject out = env->GetStaticObjectField(syscls, fid);
    // Get PrintStream class
    jclass pscls = env->FindClass("java/io/PrintStream");
    // Lookup printLn(String)
    jmethodID mid = env->GetMethodID(pscls, "println", "(Ljava/lang/String;)V");
    // Invoke the method
    jstring str = env->NewStringUTF(message);
    env->CallVoidMethod(out, mid, str);
}


//------------------------------------------------------------------------------------------------------
//Course1:http://jnicookbook.owsiak.org/recipe-No-001/
//Running simple jni code and display message from native.
//------------------------------------------------------------------------------------------------------
extern "C"
JNIEXPORT void JNICALL
Java_com_cz_android_cpp_sample_course_Recipe1Fragment_displayMessage(JNIEnv *env, jobject clazz) {
    println(env,"Message from native!");
}

//------------------------------------------------------------------------------------------------------
//Course1:http://jnicookbook.owsiak.org/recipe-No-002/
//+-----------+-------------+------------------+
//| Java Type | Native Type |       Size       |
//+-----------+-------------+------------------+
//| boolean   | jboolean    | unsigned 8 bits  |
//| byte      | jbyte       | signed 8 bits    |
//| char      | jchar       | unsigned 16 bits |
//| short     | jshort      | signed 16 bits   |
//| int       | jint        | signed 32 bits   |
//| long      | jlong       | signed 64 bits   |
//| float     | jfloat      | 32 bits          |
//| double    | jdouble     | 64 bits          |
//| void      | void        | not applicable   |
//+-----------+-------------+------------------+
//Passing value from java to C
//------------------------------------------------------------------------------------------------------
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_cz_android_cpp_sample_course_Recipe2Fragment_passBoolean(JNIEnv *env, jobject thiz,
                                                                  jboolean v) {
    return !v;
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_com_cz_android_cpp_sample_course_Recipe2Fragment_passFloat(JNIEnv *env, jobject thiz,
                                                                jfloat v) {
    return v+1;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_cz_android_cpp_sample_course_Recipe2Fragment_passInt(JNIEnv *env, jobject thiz, jint v) {
    return v+1;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_cz_android_cpp_sample_course_Recipe2Fragment_passLong(JNIEnv *env, jobject thiz, jlong v) {
    return v+1L;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_cz_android_cpp_sample_course_Recipe2Fragment_passString(JNIEnv *env, jobject thiz,
                                                                 jstring v) {
    char* str=(char*)env->GetStringUTFChars(v,0);
    std::string new_str(str);
    new_str+="L";
    env->ReleaseStringUTFChars(v,str);
    return env->NewStringUTF(new_str.c_str());
}

//------------------------------------------------------------------------------------------------------
//Course1:http://jnicookbook.owsiak.org/recipe-No-012/
//In this sample, we will pass all primitive types from Java to C.
//This time, we will pass them inside arrays.
//Whenever you pass array of primitives from Java to C,
//you have to retrieve it’s content using C baed types.
//------------------------------------------------------------------------------------------------------

extern "C"
JNIEXPORT jbooleanArray JNICALL
Java_com_cz_android_cpp_sample_course_Recipe3Fragment_passBooleanArray(JNIEnv *env, jobject thiz,
                                                                       jbooleanArray v) {
    int len=env->GetArrayLength(v);
    jboolean* arr1=env->GetBooleanArrayElements(v,0);
    jbooleanArray new_arr=env->NewBooleanArray(len+1);
    jboolean* arr2=env->GetBooleanArrayElements(new_arr,0);
    for(int i=0;i!=len;++i){
        arr2[i]=arr1[i];
    }
    arr2[len]=true;
    env->ReleaseBooleanArrayElements(v,arr1,0);
    env->ReleaseBooleanArrayElements(new_arr,arr2,0);
    return new_arr;
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_cz_android_cpp_sample_course_Recipe3Fragment_passFloatArray(JNIEnv *env, jobject thiz,
                                                                     jfloatArray v) {
    int len=env->GetArrayLength(v);
    jfloat* arr1=env->GetFloatArrayElements(v,0);
    jfloatArray new_arr=env->NewFloatArray(len+1);
    jfloat* arr2=env->GetFloatArrayElements(new_arr,0);
    for(int i=0;i!=len;++i){
        arr2[i]=arr1[i];
    }
    arr2[len]=10;
    env->ReleaseFloatArrayElements(v,arr1,0);
    env->ReleaseFloatArrayElements(new_arr,arr2,0);
    return new_arr;
}

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_cz_android_cpp_sample_course_Recipe3Fragment_passIntArray(JNIEnv *env, jobject thiz,
                                                                   jintArray v) {
    int len=env->GetArrayLength(v);
    jint* arr1=env->GetIntArrayElements(v,0);
    jintArray new_arr=env->NewIntArray(len+1);
    jint* arr2=env->GetIntArrayElements(new_arr,0);
    for(int i=0;i<len;i++){
        arr2[i]=arr1[i];
    }
    arr2[len]=10;
    env->ReleaseIntArrayElements(v,arr1,0);
    env->ReleaseIntArrayElements(new_arr,arr2,0);
    return new_arr;
}

extern "C"
JNIEXPORT jlongArray JNICALL
Java_com_cz_android_cpp_sample_course_Recipe3Fragment_passLongArray(JNIEnv *env, jobject thiz,
                                                                    jlongArray v) {
    int len=env->GetArrayLength(v);
    jlong* arr1=env->GetLongArrayElements(v,0);
    jlongArray new_arr=env->NewLongArray(len+1);
    jlong* arr2=env->GetLongArrayElements(new_arr,0);
    for(int i=0;i<len;i++){
        arr2[i]=arr1[i];
    }
    arr2[len]=10;
    env->ReleaseLongArrayElements(v,arr1,0);
    env->ReleaseLongArrayElements(new_arr,arr2,0);
    return new_arr;
}

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_cz_android_cpp_sample_course_Recipe3Fragment_passStringArray(JNIEnv *env, jobject thiz,
                                                                      jobjectArray v) {
    int len=env->GetArrayLength(v);
    jclass clazz=env->FindClass("java/lang/String");
    jobjectArray new_arr=env->NewObjectArray(len+1,clazz,NULL);
    for(int i=0;i<len;i++){
        jobject obj=env->GetObjectArrayElement(v,i);
        env->SetObjectArrayElement(new_arr,i,obj);
    }
    char* c_str="New";
    jstring str=env->NewStringUTF(c_str);
    env->SetObjectArrayElement(new_arr,len,str);
    return new_arr;
}

//------------------------------------------------------------------------------------------------------
//Course19:http://jnicookbook.owsiak.org/recipe-No-019/
//Throwing exception from C code
//------------------------------------------------------------------------------------------------------
extern "C"
JNIEXPORT void JNICALL
Java_com_cz_android_cpp_sample_course_Recipe4Fragment_throwException(JNIEnv *env, jobject thiz) {
    env->ThrowNew(env->FindClass("java/lang/Exception"),"Error!");
}

//------------------------------------------------------------------------------------------------------
//Course20:http://jnicookbook.owsiak.org/recipe-No-020/
//In this sample I will show you how to access fields of object passed as argument to native method.

//+---+---------+
//| Z | boolean |
//| B | byte    |
//| C | char    |
//| S | short   |
//| I | int     |
//| J | long    |
//| F | float   |
//| D | double  |
//+-------------+

//------------------------------------------------------------------------------------------------------
extern "C"
JNIEXPORT void JNICALL
Java_com_cz_android_cpp_sample_course_Recipe5Fragment_changeObjectInNative(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jobject obj) {
    jclass clazz=env->GetObjectClass(obj);
    jfieldID iVal=env->GetFieldID(clazz,"iVal", "I");
    jfieldID bVal=env->GetFieldID(clazz,"bVal", "Z");
    jfieldID cVal=env->GetFieldID(clazz,"cVal", "C");
    jfieldID dVal=env->GetFieldID(clazz,"dVal", "D");
    jfieldID oVal=env->GetFieldID(clazz,"oVal", "Lcom/cz/android/cpp/sample/course/model/OtherClass;");
    jfieldID sVal=env->GetFieldID(clazz,"sVal", "Ljava/lang/String;");

    jint iValValue=env->GetIntField(obj,iVal);
    env->SetIntField(obj,iVal,iValValue+1);

    jboolean bValValue=env->GetBooleanField(obj,bVal);
    env->SetBooleanField(obj,bVal,!bValValue);

    jchar cValValue=env->GetCharField(obj,cVal);
    env->SetCharField(obj,cVal,cValValue+1);

    jdouble dValValue=env->GetDoubleField(obj,dVal);
    env->SetDoubleField(obj,dVal,dValValue+1);

    jobject oValue=env->GetObjectField(obj,oVal);

    jclass otherClass=env->FindClass("com/cz/android/cpp/sample/course/model/OtherClass");
    jfieldID valFieldId=env->GetFieldID(otherClass,"val","Ljava/lang/String;");
    jstring val=(jstring)env->GetObjectField(oValue,valFieldId);

    const char * str=env->GetStringUTFChars(val,0);
    const char * new_str=(std::string(str)+" E").c_str();

    jstring new_val=env->NewStringUTF(new_str);
    env->SetObjectField(oValue,valFieldId,new_val);
    env->ReleaseStringUTFChars(val,str);

    jstring sValue=(jstring)env->GetObjectField(obj,sVal);
    const char* s_value=env->GetStringUTFChars(sValue,0);
    const char * s_new_str=(std::string(s_value)+" E").c_str();
    jstring obj_new_str=env->NewStringUTF(s_new_str);
    env->SetObjectField(obj,sVal,obj_new_str);
    env->ReleaseStringUTFChars(sValue,s_value);
}

//------------------------------------------------------------------------------------------------------
//Invoke java method from native multiple-thread.
//https://baptiste-wicht.com/posts/2012/03/cp11-concurrency-tutorial-part-2-protect-shared-data.html
//------------------------------------------------------------------------------------------------------
#include <thread>
#include <mutex>

std::mutex mutex;
void increaseCounter(JavaVM* jvm,jobject thiz){
    while(true){
        mutex.lock();
        JNIEnv* env;
        if (jvm->GetEnv((void**)&env, JNI_VERSION_1_6) == JNI_OK){
            jclass clazz=env->GetObjectClass(thiz);
            jmethodID methodId=env->GetMethodID(clazz,"increaseCounter","()V");
            env->CallVoidMethod(thiz,methodId);
        }
        mutex.unlock();
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_cz_android_cpp_sample_course_Recipe6Fragment_startCount(JNIEnv *env, jobject thiz) {
    JavaVM* jvm;
    env->GetJavaVM(&jvm);
    std::thread t1(increaseCounter,jvm,thiz);
    std::thread t2(increaseCounter,jvm,thiz);
    std::thread t3(increaseCounter,jvm,thiz);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cz_android_cpp_sample_course_Recipe6Fragment_stopCount(JNIEnv *env, jobject thiz) {

}
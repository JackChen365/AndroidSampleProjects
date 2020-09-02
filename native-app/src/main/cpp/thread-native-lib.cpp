//
// Created by cz on 2016/6/29.
//
#include <jni.h>
#include <time.h>
#include <signal.h>
#include <android/log.h>
#include <cstring>
#include <thread>
#include <chrono>

using namespace std;

#define TAG "NativeThread"

#define  logI(...)  __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define  logE(...)  __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

//------------------------------------------------------------------------------------------------------
//https://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/invocation.html
//There are few things you have to consider here. First of all, JVM can have only one, active, thread. It means that each thread running in parallel must acquire access to JVM by attaching to it. You can find description here. Especially, make sure to pay attention to following section:
//The JNI interface pointer (JNIEnv) is valid only in the current thread. Should another thread need to access the Java VM, it must first call AttachCurrentThread() to attach itself to the VM and obtain a JNI interface pointer. Once attached to the VM, a native thread works just like an ordinary Java thread running inside a native method. The native thread remains attached to the VM until it calls DetachCurrentThread() to detach itself.
//------------------------------------------------------------------------------------------------------

typedef class ThreadContext{
public:
    jclass activityCls;
    jobject activityObj;
    jmethodID updateCountMethodId;
    jmethodID updateTimeMethodId;
    JavaVM *vm;
    int count;
    ThreadContext(JavaVM *vm):vm(vm),count(0){}
};

ThreadContext *context = nullptr;
std::atomic_bool running { true };

void timerFunction(JNIEnv *e){
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "enter the thread function.");
    JavaVM *vm=context->vm;
    JNIEnv *env;
    while(running){
        context->count++;
        //get a env to do something
        jint code=vm->GetEnv((void**)&env,JNI_VERSION_1_6);
        if(JNI_OK!=code){
            code = vm->AttachCurrentThread(&env, NULL);
            if (JNI_OK != code) {
                logI("Failed to AttachCurrentThread, ErrorCode = %d", code);
            }
        }
        time_t timeT=time(NULL);
        env->CallVoidMethod(context->activityObj,context->updateCountMethodId,context->count);
        env->CallVoidMethod(context->activityObj,context->updateTimeMethodId,localtime(&timeT)->tm_hour,localtime(&timeT)->tm_min,localtime(&timeT)->tm_sec);
        logI("thread:%s, count:%d",std::this_thread::get_id(),context->count);
        std::this_thread::sleep_for(std::chrono::milliseconds(1000));
    }
}


JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "JNI_OnLoad and initialization the time value");
    context=new ThreadContext(vm);
    JNIEnv* env;
    //must be return jni version
    jint rtn=JNI_VERSION_1_6;
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) rtn= JNI_ERR;
    return  rtn;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cz_android_cpp_sample_thread_ThreadSampleActivity_getHour(JNIEnv *env, jobject jobject1) {
    time_t timeT=time(NULL);
    return localtime(&timeT)->tm_hour;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cz_android_cpp_sample_thread_ThreadSampleActivity_getMiunte(JNIEnv *env, jobject jobject1) {
    time_t timeT=time(NULL);
    return localtime(&timeT)->tm_min;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cz_android_cpp_sample_thread_ThreadSampleActivity_getSecond(JNIEnv *env, jobject jobject1) {
    time_t timeT=time(NULL);
    return localtime(&timeT)->tm_sec;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_cz_android_cpp_sample_thread_ThreadSampleActivity_stop(JNIEnv *env, jobject jobject1) {
    //release the global ref
    env->DeleteGlobalRef(context->activityObj);
    env->DeleteGlobalRef(context->activityCls);
    //must to NULL,if not ,the JavaVm will stop
    running = false;
    context->activityCls=NULL;
    context->activityObj=NULL;
    context->updateCountMethodId=NULL;
    context->updateTimeMethodId=NULL;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cz_android_cpp_sample_thread_ThreadSampleActivity_start(JNIEnv *env, jobject instance) {
    jclass clz = env->GetObjectClass(instance);
    context->activityCls= (jclass) env->NewGlobalRef(clz);
    context->activityObj=env->NewGlobalRef(instance);
    context->updateCountMethodId=env->GetMethodID(context->activityCls,"updateCounter","(I)V");
    context->updateTimeMethodId=env->GetMethodID(context->activityCls,"updateTime","(III)V");
    logI("start thread!");
    running = true;
    std::thread t1=std::thread(timerFunction,env);
    t1.detach();
}

//------------------------------------------------------------------------------------------------------
//C++ consumer and producer
//------------------------------------------------------------------------------------------------------
#include <mutex>
#include <condition_variable>
mutex mutex_obj;
unique_lock<mutex> unique_lock(mutex_obj);
condition_variable condition;
bool readyToSet=false;
int counter;


void consume(){
    mutex_obj.lock();
//    condition.wait(unique_lock,[]{return readyToSet;});
    mutex_obj.unlock();
}

void produce(){
    mutex_obj.lock();


    mutex_obj.unlock();
}
#include <jni.h>
#include <string>
#include <android/log.h>


extern "C"
JNIEXPORT jlong JNICALL
Java_com_cz_android_cpp_sample_basic_NativeMethodInvokeSampleActivity_nGetIntMethod(JNIEnv *env, jobject thiz,
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
Java_com_cz_android_cpp_sample_basic_NativeMethodInvokeSampleActivity_nSetIntMethod(JNIEnv *env, jobject thiz,
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
Java_com_cz_android_cpp_sample_basic_NativeMethodInvokeSampleActivity_nCallSetIntMethod(JNIEnv *env, jobject thiz,
                                                                   jobject target, jlong method_id,
                                                                   jint arg) {
    env->CallVoidMethod(target,(jmethodID)method_id,arg);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cz_android_cpp_sample_basic_NativeMethodInvokeSampleActivity_nCallGetIntMethod(JNIEnv *env, jobject thiz,
                                                                   jobject target,
                                                                   jlong method_id) {
    return env->CallIntMethod(target,(jmethodID)method_id);
}


#include <jni.h>
#include <string>
#include <unistd.h>

extern "C" JNIEXPORT jstring JNICALL
Java_com_cz_android_cpp_sample_basic_HelloNativeSampleActivity_stringFromJNI(JNIEnv* env,jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}extern "C"

JNIEXPORT jstring JNICALL
Java_com_cz_android_cpp_sample_basic_HelloNativeSampleActivity_changeTextFromJNI(JNIEnv *env,jobject thiz) {
    //Test if jni method will also make the java layer sleep. The answer is yes...
    sleep(2);
    std::string hello = "Text changed from C++";
    return env->NewStringUTF(hello.c_str());
}
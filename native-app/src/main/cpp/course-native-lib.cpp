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
//Passing int value from java to C
//------------------------------------------------------------------------------------------------------
#include <jni.h>
#include <string>
#include <thread>
#include <future>
#include <iostream>
#include <vector>
#include <dirent.h>
#include <android/log.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_cz_android_cpp_sample_basic_NativeFileSearchActivity_searchFile(JNIEnv *env, jobject thiz,
                                                                         jstring file_path) {


    const char *cstr = env->GetStringUTFChars(file_path, JNI_FALSE);
    std::string str = std::string(cstr);
    __android_log_print(ANDROID_LOG_INFO, "NativeFileSearchActivity_searchFile", "file = %s",  cstr);
    const std::vector<std::string> dirSkipList;
    env->ReleaseStringUTFChars(file_path, cstr);
    // Create a vector of string
    std::vector<std::string> listOfFiles;
}

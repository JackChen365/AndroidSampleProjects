//
// Created by cz on 2016/7/1.
//
#include <jni.h>
#include <sys/inotify.h>
#include <unistd.h>
#include <dirent.h>
#include <cstring>
#include <thread>
#include <android/log.h>

#define TAG "NativeFile"

#define  logI(...)  __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define  logE(...)  __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)


using  namespace std;

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

struct WatchStatus{
    JavaVM *vm;
    bool watched;
    int fd;
    int wd;
};
const int EVENT_SIZE  = sizeof(struct inotify_event);
const int MAX_SIZE=1024*EVENT_SIZE;
WatchStatus status;

void createThreadToWatch(JNIEnv *env);
void removeWatch(JNIEnv *env);

extern "C"
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    logI("JNI_OnLoad and initialization");
    memset(&status, 0, sizeof(status));
    status.vm=vm;
    status.watched=false;
    //must be return jni version
    jint rtn=JNI_VERSION_1_6;
    JNIEnv* env;
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) rtn= JNI_ERR;
    return  rtn;
}



void createThreadToWatch(JNIEnv *env){
    std::thread th=std::thread([](){
        char buffer[MAX_SIZE];
        while(read(status.fd, buffer, MAX_SIZE)) {
            struct inotify_event *event = (struct inotify_event*)buffer;
            if (event->len) {
                if ( event->mask & IN_CREATE ) {
                    if ( event->mask & IN_ISDIR ) logI("The directory %s was created.\n", event->name);
                    else logI("The file %s was created.\n", event->name);
                } else if ( event->mask & IN_DELETE ) {
                    if ( event->mask & IN_ISDIR ) logI("The directory %s was deleted.\n", event->name);
                    else logI("The file %s was deleted.\n", event->name);
                } else if ( event->mask & IN_MODIFY ) {
                    if ( event->mask & IN_ISDIR ) logI("The directory %s was modified.\n", event->name);
                    else logI("The file %s was modified.\n", event->name );
                }
            }else {
                logI("specified file!");
                //when only a file(not directory) is specified by add watch function, event->len's value may be zero, we can handle it here
            }
        }
        logI("com.cz.android.cpp.sample.thread is over!");
    });
    th.detach();
}


void removeWatch(JNIEnv *env) {
    if(status.watched) {
        status.watched=false;
        logI("remove last watch!");
        inotify_rm_watch(status.fd,status.wd);
    }
}


extern "C"
JNIEXPORT void JNICALL
Java_com_cz_android_cpp_sample_file_FileWatchSampleActivity_startWatchFile(JNIEnv *env,jobject jobject,jstring jpath) {
    const char *path=env->GetStringUTFChars(jpath,0);
    DIR *dir;
    if(NULL!=(dir=opendir(path))){
        removeWatch(env);
        status.fd=inotify_init();
        //watch modify create and delete,and the watch is blocking com.cz.android.cpp.sample.thread
        //two method implement the watch,No1:is blocking No2.is while(true)
        if(status.wd=inotify_add_watch(status.fd,path,IN_MODIFY | IN_CREATE | IN_DELETE)){
            status.watched=true;
            logI("watch :%s",path);
            createThreadToWatch(env);
        } else {
            //add watch fail
            logI("watch failed!");
        }
        //close the dir
        closedir(dir);
    } else {
        //update java ui message,path not existed!;
        logI("path not existed!");
    }
    env->ReleaseStringUTFChars(jpath, path);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cz_android_cpp_sample_file_FileWatchSampleActivity_removeWatchFile(JNIEnv *env,jobject thiz) {
    removeWatch(env);
}


//------------------------------------------------------------------------------------------------------
//List all the file in a specific directory
//------------------------------------------------------------------------------------------------------
#include <sys/stat.h>

void list_directory_internal(const char * path, int level,bool recursive){
    struct dirent *filename;
    DIR *dir;
    dir = opendir(path);
    if(dir == NULL) {
        logE("open dir %s error!\n",path);
    } else {
        while((filename = readdir(dir)) != NULL) {
            if(!strcmp(filename->d_name,".")||!strcmp(filename->d_name,".."))continue;
            string pathString(path);
            pathString.append("/");
            pathString.append(filename->d_name);
            const char * newPath=pathString.c_str();
            //logE("file:%s name:%s newPath:%s",path,filename->d_name,newPath);
            struct stat s;
            lstat(newPath,&s);
            if(S_ISDIR(s.st_mode)) {
                if(recursive){
                    list_directory_internal(newPath,level+1, recursive);
                }
            }else {
//                logI("Level:%d File: %s\n",level,filename->d_name);
            }
        }
        closedir(dir);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cz_android_cpp_sample_file_FileListSampleActivity_listDirectory(JNIEnv *env, jobject thiz,jstring jpath) {
    const char *path=env->GetStringUTFChars(jpath,0);
    list_directory_internal(path,0, false);
    env->ReleaseStringUTFChars(jpath,path);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cz_android_cpp_sample_file_FileListSampleActivity_listDirectoryRecursively(JNIEnv *env,
                                                                                    jobject thiz,
                                                                                    jstring jpath) {
    const char *path=env->GetStringUTFChars(jpath,0);
    list_directory_internal(path,0,true);
    env->ReleaseStringUTFChars(jpath,path);
}
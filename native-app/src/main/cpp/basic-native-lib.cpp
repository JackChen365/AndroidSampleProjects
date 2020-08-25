#include <jni.h>
#include <string>
#include <unistd.h>
#include <vector>
#include <android/log.h>

#define TAG "NativeBasic"

#define  logI(...)  __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define  logE(...)  __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

//--------------------------------------------------------------
//The native method list are for HelloNativeSampleActivity
//--------------------------------------------------------------
extern "C" JNIEXPORT jstring JNICALL
Java_com_cz_android_cpp_sample_basic_HelloNativeSampleActivity_stringFromJNI1(JNIEnv* env, jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}extern "C"

JNIEXPORT jstring JNICALL
Java_com_cz_android_cpp_sample_basic_HelloNativeSampleActivity_stringFromJNI2(JNIEnv *env, jobject thiz) {
    //Test if jni method will also make the java layer sleep. The answer is yes...
    sleep(2);
    std::string hello = "String from native and delayed two second return.";
    return env->NewStringUTF(hello.c_str());
}

//--------------------------------------------------------------
//The native method list are for NativeMethodInvokeSampleActivity
//--------------------------------------------------------------

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
    logI("method = %d",methodId);
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



//--------------------------------------------------------------
//The native method list are for NativeObjectActivity
//--------------------------------------------------------------
#include "basic/Person.h"
#include "basic/math-test.h"
extern "C"
JNIEXPORT jint JNICALL
Java_com_cz_android_cpp_sample_basic_NativeObjectActivity_nativeAdd(JNIEnv *env, jobject thiz,
                                                                    jint x, jint y) {
    return add1(x,y);
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_cz_android_cpp_sample_basic_NativeObjectActivity_getPersonSignature(JNIEnv *env, jobject thiz) {
    std::string name="Jack chen";
    Person* person=new Person(name,12);

    std::string new_str=(person->getName())+=" age:"+std::to_string(person->getAge());
    char* arr=new char[new_str.length()];
    std::strcpy(arr,new_str.c_str());
    jstring rtnValue=env->NewStringUTF(arr);
    delete(person);
    return rtnValue;
}


//--------------------------------------------------------------
//The native method list are for NativeStudentListActivity
//--------------------------------------------------------------
class Student{
public:
    char *name;
    int age;
    int sex;

    Student(char *name,int age,int sex):name(name),age(age),sex(sex){};
    ~Student(){ delete name;}
};

vector<shared_ptr<Student>> students;

void printStudent(){
    logI("print all students");
    for(auto iter=students.begin();iter!=students.end();++iter){
        Student& stu=**iter;
        logI("name:%s",stu.name);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cz_android_cpp_sample_basic_NativeStudentManager_addStudent(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jobject str) {
    jclass clazz=env->GetObjectClass(str);
    int age=env->GetIntField(str,env->GetFieldID(clazz,"age","I"));
    int sex=env->GetIntField(str,env->GetFieldID(clazz,"sex","I"));
    jstring name=(jstring)env->GetObjectField(str,env->GetFieldID(clazz,"name","Ljava/lang/String;"));
    char *studentName= const_cast<char*>(env->GetStringUTFChars(name,0));
    logI("Add a new student:%s",studentName);
    students.push_back(std::make_shared<Student>(studentName,age,sex));
    printStudent();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cz_android_cpp_sample_basic_NativeStudentManager_removeStudent(JNIEnv *env,
                                                                               jobject thiz,
                                                                               jstring stu_name) {
    const char *name = env->GetStringUTFChars(stu_name, 0);
    logI("remove student:%s",name);
    auto iter=std::find_if(students.begin(),students.end(),
            [name](auto ptr_stu) { return 0==std::strcmp(name,(*ptr_stu).name);});
    if(iter != students.end()){
        students.erase(iter);
        logI("Erase student:%s",(*iter)->name);
    } else {
        logI("Not found student!");
    }
    printStudent();
    env->ReleaseStringUTFChars(stu_name, name);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cz_android_cpp_sample_basic_NativeStudentManager_updateStudent(JNIEnv *env,
                                                                               jobject thiz,
                                                                               jstring name_,
                                                                               jint age) {
    const char *name = env->GetStringUTFChars( name_, 0);
    logI("update student:%s",name);
    auto iter=find_if(students.begin(),students.end(),
                      [name](auto ptr_stu) { return 0==std::strcmp(name,(*ptr_stu).name);});
    if(iter!=students.end()){
        (*iter)->age=age;
    }else {
        logI("not found student!");
    }
    env->ReleaseStringUTFChars( name_, name);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_cz_android_cpp_sample_basic_NativeStudentManager_getStudent(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jstring name_) {
    const char *name = env->GetStringUTFChars(name_, 0);
    auto iter=find_if(students.begin(),students.end(),
                      [name](auto ptr_stu) { return 0==std::strcmp(name,(*ptr_stu).name);});
    jclass cls = env->FindClass("com/cz/android/cpp/sample/basic/Student");  //创建一个class的引用
    jobject obj=env->AllocObject(cls);
    if(iter!=students.end()){
        env->SetObjectField(obj,env->GetFieldID(cls,"name","Ljava/lang/String;"),env->NewStringUTF((*iter)->name));
        env->SetIntField(obj,env->GetFieldID(cls,"age","I"),(*iter)->age);
        env->SetIntField(obj,env->GetFieldID(cls,"sex","I"),(*iter)->sex);
    }else {
        logI( "not found student!");
    }
    env->ReleaseStringUTFChars(name_, name);
    return obj;
}

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_cz_android_cpp_sample_basic_NativeStudentManager_getStudents(JNIEnv *env,
                                                                             jobject thiz) {
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "Return all the students");
    jclass cls = env->FindClass("com/cz/android/cpp/sample/basic/Student");  //创建一个class的引用
    const int len=students.size();
    jobjectArray objectArray=env->NewObjectArray(len,cls,0);
    for(int i=0;i!=len;++i){
        auto shared_ptr=students.at(i);
        Student* stu=shared_ptr.get();
        jobject obj=env->AllocObject(cls);
        env->SetObjectField(obj,env->GetFieldID(cls,"name","Ljava/lang/String;"),env->NewStringUTF(stu->name));
        env->SetIntField(obj,env->GetFieldID(cls,"age","I"),stu->age);
        env->SetIntField(obj,env->GetFieldID(cls,"sex","I"),stu->sex);
        env->SetObjectArrayElement(objectArray,i,obj);
    }
    return objectArray;
}

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_cz_android_cpp_sample_basic_NativeStudentManager_clearStudents(JNIEnv *env,jobject thiz) {
//    students.clear();
}

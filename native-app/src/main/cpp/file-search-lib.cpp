#include <jni.h>
#include <string>
#include <thread>
#include <future>
#include <filesystem>
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

/*
 * Get the list of all files in given directory and its sub directories.
 *
 * Arguments
 *     dirPath : Path of directory to be traversed
 *     dirSkipList : List of folder names to be skipped
 *
 * Returns:
 *     vector containing paths of all the files in given directory and its sub directories
 *
 */
std::vector<std::string> getAllFilesInDir(const std::string &dirPath,const std::vector<std::string> dirSkipList = { }){
    // Create a vector of string
    namespace filesys=std::filesystem;
    std::vector<std::string> listOfFiles;
    try {
        // Check if given path exists and points to a directory
        if (std::filesystem::exists(dirPath) && std::filesystem::is_directory(dirPath))
        {
            // Create a Recursive Directory Iterator object and points to the starting of directory
            std::filesystem::recursive_directory_iterator iter(dirPath);
            // Create a Recursive Directory Iterator object pointing to end.
            std::filesystem::recursive_directory_iterator end;
            // Iterate till end
            while (iter != end)
            {
                // Check if current entry is a directory and if exists in skip list
                if (std::filesystem::is_directory(iter->path()) &&
                    (std::find(dirSkipList.begin(), dirSkipList.end(), iter->path().filename()) != dirSkipList.end()))
                {
                    // Skip the iteration of current directory pointed by iterator
#ifdef USING_BOOST
                    // Boost Fileystsem  API to skip current directory iteration
                    iter.no_push();
#else
                    // c++17 Filesystem API to skip current directory iteration
                    iter.disable_recursion_pending();
#endif
                }
                else
                {
                    // Add the name in vector
                    listOfFiles.push_back(iter->path().string());
                }
                std::error_code ec;
                // Increment the iterator to point to next entry in recursive iteration
                iter.increment(ec);
                if (ec) {
                    std::cerr << "Error While Accessing : " << iter->path().string() << " :: " << ec.message() << '\n';
                }
            }
        }
    }
    catch (std::system_error & e)
    {
        std::cerr << "Exception :: " << e.what();
    }
    return listOfFiles;
}
//
// Created by cz on 2016/7/1.
//
#include <jni.h>
#include <iostream>
#include <fstream>
//--------------------------------------------------------------
//c++ read file
//--------------------------------------------------------------
void readFileLine(std::string &file_name){
    std::ifstream f(file_name);
    if(f.fail()){
        std::cout << "can not open the file"<<std::endl;
        exit(1);
    }
    //read line
    std::string line;
    while(getline(f,line)){
        std::cout<<line<<std::endl;
    }
    f.close();
}

void readFileBuffer(std::string &file_name){
    std::ifstream file(file_name);
    if(file.fail()){
        std::cout << "can not open the file"<<std::endl;
        exit(1);
    }
    //read line
    char* buffer=new char[10];
    while(!file.eof()){
        file.read(buffer,10);
        int read=file.gcount();
        //std::cout<<read<<std::endl;
        std::cout<<std::string(buffer,read)<<std::endl;
    }
    file.close();
}

//
// Created by cz on 2020/8/27.
//
#include <jni.h>
#include <iostream>
#include <string>
//--------------------------------------------------------------
//The test the reference
//--------------------------------------------------------------
void refTest1(int v){
    std::cout<<"test2 "<<&v<<std::endl;
}

void refTest2(int& v){
    std::cout<<"test3 "<<&v<<std::endl;
    auto b1=std::ref(v);
    std::cout<<"test4 "<<&b1<<std::endl;
}

int refTest(){
    int v=3;
    std::cout<<"test1 "<<&v<<std::endl;
    refTest1(v);
    refTest2(v);
//    0x7ffee010e928
//    0x7ffee010e920
//    0x7ffee010e8fc
//    0x7ffee010e928
}
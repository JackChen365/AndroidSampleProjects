##Readme

> In this demo, I want to know two things.
The first one was: How can we use a custom C plus plus Object in native code.
The second one: How to use a custom header file in native code.

The following samples will show you how to do it.

1. define a c plus plus object.

```
//
// Created by Jack Chen on 8/19/2020.
//
#ifndef PERSON_H_INCLUDED
#define PERSON_H_INCLUDED

#include <string>
#include <iostream>
using namespace std;

//Write a program to read a user's input and store it in the class object below.

class Person
{
public:
    Person();
    Person(string pname, int page);
    string getName() const;
    void setName( string name );
    void setAge( int age );
    int getAge() const;

private:
    string name;
    int age; //If 0 is unknown.
};

#endif // PERSON_H_INCLUDED
```

The cpp file

```
#include "Person.h"
using namespace std;

Person::Person(){
    name = "";
    age = 0;
}

Person::Person (string pname, int page){
    name = pname;
    if( page >= 0 && page < 120 ) {
        age = page;
    }
    else {
        throw std::invalid_argument( "Invalid Age" );
    }
}

string Person::getName() const{
    return name;
}

int Person::getAge() const {
    return age;
}

void Person::setName(string name){
    this->name = name;
}

void Person::setAge( int age ){
    this->age = age;
}
```

2. Attach the source to the native library, Before we build the source code.

```
//File:CMakeLists.txt
add_library(basic-native-lib
        SHARED
        basic/Person.cpp
        basic/math-test.cpp
        basic-native-lib.cpp)
```
Done.

That's everything we should do.
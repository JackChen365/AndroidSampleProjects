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

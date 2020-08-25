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
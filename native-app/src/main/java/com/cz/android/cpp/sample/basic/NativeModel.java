package com.cz.android.cpp.sample.basic;

/**
 * @author Created by cz
 * @date 2020/5/29 12:15 PM
 * @email chenzhen@okay.cn
 */
public class NativeModel {
    private int age;
    private String name;

    public NativeModel(int age, String name) {
        this.age = age;
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

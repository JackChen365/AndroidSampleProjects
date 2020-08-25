package com.cz.android.cpp.sample.basic

import android.content.Context
import com.cz.android.sample.library.data.DataManager
import com.cz.android.sample.library.data.DataProvider

/**
 * @author Created by cz
 * @date 2020/8/25 1:17 PM
 * @email bingo110@126.com
 */
object NativeStudentManager {
    init {
        System.loadLibrary("basic-native-lib")
    }
    private external fun addStudent(str: Student?)
    private external fun removeStudent(name: String?)
    private external fun updateStudent(name: String?, age: Int)
    private external fun getStudent(name: String?): Student?
    external fun getStudents(): Array<Student?>?
    external fun clearStudents(): Array<Student?>?

    fun addStudent(context:Context) {
        val dataProvider = DataManager.getDataProvider(context)
        val student = Student(dataProvider.word,
            DataProvider.RANDOM.nextInt(100),
            DataProvider.RANDOM.nextInt(2)
        )
        addStudent(student)
    }

    fun removeStudent() {
        val students = getStudents()
        if(null!=students&&students.isNotEmpty()){
            val last = students.last()
            if(null!=last){
                removeStudent(last.name)
            }
        }
    }

    fun updateStudent() {
        val students = getStudents()
        if(null!=students&&students.isNotEmpty()){
            val last = students.last()
            if(null!=last){
                updateStudent(last.name, DataProvider.RANDOM.nextInt(100))
            }
        }
    }
}
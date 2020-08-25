package com.cz.android.cpp.sample.basic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cz.android.cpp.sample.R
import com.cz.android.sample.api.Exclude
import kotlinx.android.synthetic.main.fragment_native_student2.*

/**
 * @author Created by cz
 * @date 2020/8/24 4:40 PM
 * @email bingo110@126.com
 * Demonstrate how we register event observer and receiving events from The LiveObservable.
 */
@Exclude
class NativeStudentFragment : Fragment() {
    companion object{
        init {
            System.loadLibrary("basic-native-lib")
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_native_student2,container,false);
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val context=context?:return
        addButton.setOnClickListener {
            NativeStudentManager.addStudent(context)
        }
        removeButton.setOnClickListener {
            NativeStudentManager.removeStudent();
        }
        updateButton.setOnClickListener {
            NativeStudentManager.updateStudent()
        }
        printAllButton.setOnClickListener {
//            val students = NativeStudentManager.getStudents()
//            if(null!=students){
//                println("All students:")
//                for (student in students) {
//                    if(null!=student){
//                        println("name:" + student.name.toString() + " age:" + student.age + " sex:" + student.sex)
//                    }
//                }
//            }
            NativeStudentManager.clearStudents()
        }
    }

    override fun onDestroy() {
        NativeStudentManager.clearStudents()
        super.onDestroy()
    }
}

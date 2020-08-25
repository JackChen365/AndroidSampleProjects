package com.cz.android.cpp.sample.basic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.cpp.sample.R
import com.cz.android.sample.library.component.message.SampleMessage
import kotlinx.android.synthetic.main.activity_native_student_list.*

@SampleMessage
class NativeStudentListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_student_list)
        val liveObserverFragment = NativeStudentFragment()
        if(null==savedInstanceState){
            supportFragmentManager.beginTransaction().add(R.id.content1,liveObserverFragment).commit()
        }
        addButton.setOnClickListener {
            val findFragment = supportFragmentManager.findFragmentById(R.id.content1)
            if(null==findFragment){
                supportFragmentManager.beginTransaction().add(R.id.content1,liveObserverFragment).commit()
            }
        }

        removeButton.setOnClickListener {
            val findFragment = supportFragmentManager.findFragmentById(R.id.content1)
            if(null!=findFragment){
                supportFragmentManager.beginTransaction().remove(liveObserverFragment).commit()
            }
        }

        showButton.setOnClickListener {
            val findFragment = supportFragmentManager.findFragmentById(R.id.content1)
            if(null!=findFragment){
                supportFragmentManager.beginTransaction().show(liveObserverFragment).commit()
            }
        }

        hideButton.setOnClickListener {
            val findFragment = supportFragmentManager.findFragmentById(R.id.content1)
            if(null!=findFragment){
                supportFragmentManager.beginTransaction().hide(liveObserverFragment).commit()
            }
        }

    }
}
package com.cz.android.cpp.sample.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cz.android.cpp.sample.R
import com.cz.android.cpp.sample.course.model.CustomClass
import com.cz.android.cpp.sample.course.model.OtherClass
import com.cz.android.sample.library.component.code.SampleSourceCode
import com.cz.android.sample.library.component.message.SampleMessage
import kotlinx.android.synthetic.main.fragment_native_recipe4.*

@SampleMessage
@SampleSourceCode()
class Recipe5Fragment : Fragment() {
    companion object {
        init {
            System.loadLibrary("native-course-lib")
        }
    }
    private external fun changeObjectInNative(obj: CustomClass);

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_native_recipe4, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val obj=CustomClass()
        obj.bVal=false
        obj.cVal='0'
        obj.dVal=0.1
        obj.iVal=1
        obj.oVal= OtherClass("Jack")
        obj.sVal="Value"
        outputButton.setOnClickListener {
            changeObjectInNative(obj)
            println("----------------------------")
            println("bool:${obj.bVal}")
            println("char:${obj.cVal}")
            println("double:${obj.dVal}")
            println("int:${obj.iVal}")
            println("object:${obj.oVal}")
            println("string:${obj.sVal}")
        }
    }
}
package com.cz.android.cpp.sample.course

import android.os.Bundle
import android.util.Log
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
import kotlinx.android.synthetic.main.fragment_native_recipe6.*

@SampleMessage
@SampleSourceCode
class Recipe6Fragment : Fragment() {
    companion object {
        private const val TAG="Recipe6Fragment"
        init {
            System.loadLibrary("native-course-lib")
        }
    }

    private external fun startCount()
    private external fun stopCount()

    private var counter=0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_native_recipe6, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        startButton.setOnClickListener {
            startCount()
        }
        stopButton.setOnClickListener {
            stopCount()
        }
    }

    fun increaseCounter(){
        counter++
        Log.i(TAG,"increaseCounter:${Thread.currentThread().name} counter:"+counter)
    }
}
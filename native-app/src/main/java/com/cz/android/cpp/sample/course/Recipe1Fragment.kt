package com.cz.android.cpp.sample.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cz.android.cpp.sample.R
import com.cz.android.sample.library.component.code.SampleSourceCode
import com.cz.android.sample.library.component.message.SampleMessage
import kotlinx.android.synthetic.main.fragment_native_recipe1.*

@SampleMessage
@SampleSourceCode
class Recipe1Fragment : Fragment() {
    companion object {
        init {
            System.loadLibrary("native-course")
        }
    }

    private external fun displayMessage()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(
            R.layout.fragment_native_recipe1,
            container,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        outputButton.setOnClickListener {
            displayMessage()
        }
    }
}
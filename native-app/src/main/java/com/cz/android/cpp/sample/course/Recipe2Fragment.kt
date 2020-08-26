package com.cz.android.cpp.sample.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cz.android.cpp.sample.R
import com.cz.android.sample.library.component.code.SampleSourceCode
import com.cz.android.sample.library.component.message.SampleMessage
import kotlinx.android.synthetic.main.fragment_native_recipe2.*

@SampleMessage
@SampleSourceCode
class Recipe2Fragment : Fragment() {
    companion object {
        init {
            System.loadLibrary("native-course-lib")
        }
    }
    private external fun passInt(v:Int):Int
    private external fun passBoolean(v:Boolean):Boolean
    private external fun passLong(v:Long):Long
    private external fun passFloat(v:Float):Float
    private external fun passString(v:String):String

    private var intValue=1
    private var booleanValue=false
    private var longValue=1L
    private var floatValue=1.0f
    private var stringValue="Message"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_native_recipe2, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        outputButton.setOnClickListener {
            intValue=passInt(intValue)
            booleanValue=passBoolean(booleanValue)
            longValue=passLong(longValue)
            floatValue=passFloat(floatValue)
            stringValue=passString(stringValue)
            println("passInt:$intValue")
            println("passBoolean:$booleanValue")
            println("passLong:$longValue")
            println("passFloat:$floatValue")
            println("passString:$stringValue")
        }
    }
}
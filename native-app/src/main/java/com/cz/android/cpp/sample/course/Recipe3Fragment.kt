package com.cz.android.cpp.sample.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cz.android.cpp.sample.R
import com.cz.android.sample.library.component.code.SampleSourceCode
import com.cz.android.sample.library.component.message.SampleMessage
import kotlinx.android.synthetic.main.fragment_native_recipe3.*
import java.util.*

@SampleMessage
@SampleSourceCode()
class Recipe3Fragment : Fragment() {
    companion object {
        init {
            System.loadLibrary("native-course-lib")
        }
    }
    private external fun passIntArray(v:IntArray):IntArray
    private external fun passBooleanArray(v:BooleanArray):BooleanArray
    private external fun passLongArray(v:LongArray):LongArray
    private external fun passFloatArray(v:FloatArray):FloatArray
    private external fun passStringArray(v:Array<String>):Array<String>

    private var intValue= intArrayOf(1)
    private var booleanValue= booleanArrayOf(false)
    private var longValue= longArrayOf(1)
    private var floatValue= floatArrayOf(1f)
    private var stringValue= arrayOf("Message")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_native_recipe3, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        outputButton.setOnClickListener {
            intValue=passIntArray(intValue)
            booleanValue=passBooleanArray(booleanValue)
            longValue=passLongArray(longValue)
            floatValue=passFloatArray(floatValue)
            stringValue=passStringArray(stringValue)
            println("-------------------------")
            println("passInt:${intValue.contentToString()}")
            println("passBoolean:${booleanValue.contentToString()}")
            println("passLong:${longValue.contentToString()}")
            println("passFloat:${floatValue.contentToString()}")
            println("passString:${stringValue.contentToString()}")
        }
    }
}
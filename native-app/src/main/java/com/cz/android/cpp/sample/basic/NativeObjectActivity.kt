package com.cz.android.cpp.sample.basic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.cpp.sample.R
import kotlinx.android.synthetic.main.activity_native_object.*

class NativeObjectActivity : AppCompatActivity() {
    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("basic-native-lib")
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_object)
        testButton1.setOnClickListener {
            sample_text.text = "Sum:"+nativeAdd(1,2)+"\n"
        }
        testButton2.setOnClickListener {
            val value = getPersonSignature()
            sample_text.append("Person:$value\n")
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    private external fun nativeAdd(x:Int, y:Int): Int

    private external fun getPersonSignature(): String

}
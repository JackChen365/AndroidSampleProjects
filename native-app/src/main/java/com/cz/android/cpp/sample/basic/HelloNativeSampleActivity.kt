package com.cz.android.cpp.sample.basic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.cpp.sample.R
import kotlinx.android.synthetic.main.activity_hello_native_sample.*


class HelloNativeSampleActivity : AppCompatActivity() {
    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello_native_sample)
        // Example of a call to a native method
        sample_text.text = stringFromJNI()

        changeButton.setOnClickListener {
            sample_text.text = changeTextFromJNI()
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    private external fun stringFromJNI(): String

    private external fun changeTextFromJNI(): String
}
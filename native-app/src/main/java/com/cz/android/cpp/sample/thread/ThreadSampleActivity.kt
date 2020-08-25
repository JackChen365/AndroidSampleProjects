package com.cz.android.cpp.sample.thread

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.cpp.sample.R
import kotlinx.android.synthetic.main.activity_thread_sample.*

class ThreadSampleActivity : AppCompatActivity() {
    companion object{
        init {
            System.loadLibrary("thread-native-lib")
        }
    }

    /**
     * Start a counter from native method.
     */
    private external fun start()

    /**
     * Stop the counter.
     */
    private external fun stop()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thread_sample)

        startButton.setOnClickListener {
            start()
        }

        stopButton.setOnClickListener {
            stop()
        }
    }

    /**
     * Invoke from native method. And it is in native thread. Be sure about this.
     */
    fun updateCounter(count:Int) {
        runOnUiThread { countText.text = String.format("count:%d", count) }
    }

    /**
     * Invoke from native method. And it is in native thread. Be sure about this.
     */
    fun updateTime(hour:Int,minute:Int,second:Int) {
        runOnUiThread{
            timeText.text = String.format("the time from jni:%d:%d:%d", hour, minute, second)
        }
    }
}
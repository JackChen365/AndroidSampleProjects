package com.cz.android.cpp.sample.thread

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.cz.android.cpp.sample.R
import com.cz.android.sample.library.component.message.SampleMessage

@SampleMessage
class ThreadConsumerAndProducerActivity : AppCompatActivity() {
    companion object{
        init {
            System.loadLibrary("thread-native-lib")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thread_consumer_and_producer)
    }

    private fun outputMessage(message:String){
        println(message)
    }
}
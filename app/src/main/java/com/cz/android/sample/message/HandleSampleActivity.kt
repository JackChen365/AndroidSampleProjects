package com.cz.android.sample.message

import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.message.SimpleHandler
import com.cz.android.message.SimpleLooper
import com.cz.android.message.SimpleMessage
import com.cz.android.sample.R
import com.cz.android.sample.api.Register
import kotlinx.android.synthetic.main.activity_handle_sample.*
import kotlin.concurrent.thread

@Register(title = "Handler", desc = "演示 Handler 与 MessageQueue")
class HandleSampleActivity : AppCompatActivity() {
    companion object{
        private const val TAG="HandleSampleActivity"
    }

    private lateinit var simpleHandler: SimpleHandler
    private lateinit var handler: SimpleHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_handle_sample)
        thread {
            Looper.prepare()
            handler= SimpleHandler(SimpleLooper.myLooper(), SimpleHandler.Callback { message->
                Log.i(TAG,"callback:"+message.what+" arg1:"+message.arg1)
                messageText.post {
                    messageText.append("callback:"+message.what+" arg1:"+message.arg1+"\n")
                }
                true
            })
            Looper.loop()
//            SimpleLooper.prepare()
//            val myQueue = SimpleLooper.myQueue()
//            handler= SimpleHandler(SimpleLooper.myLooper(), SimpleHandler.Callback { message->
//                Log.i(TAG,"callback:"+message.what+" arg1:"+message.arg1)
//                messageText.post {
//                    messageText.append("callback:"+message.what+" arg1:"+message.arg1+"\n")
//                }
//                true
//            })
//            SimpleLooper.loop()
        }
        postMessage.setOnClickListener {
//            val message = SimpleMessage.obtain()
//            message.what=1
//            message.arg1=SystemClock.elapsedRealtime().toInt()
//            simpleHandler.sendMessageDelayed(message,1000)

            val message = SimpleMessage.obtain()
            message.what=1
            message.arg1=SystemClock.elapsedRealtime().toInt()
            handler.sendMessageDelayed(message,1000)
        }
    }
}
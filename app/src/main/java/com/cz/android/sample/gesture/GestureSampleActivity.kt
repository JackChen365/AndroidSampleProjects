package com.cz.android.sample.gesture

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.sample.R
import com.cz.android.sample.api.Register
import kotlinx.android.synthetic.main.activity_gesture_sample.*

@Register(title = "GestureSample")
class GestureSampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gesture_sample)

        for(i in 0 until layout.childCount){
            val childView = layout.getChildAt(i)
            if(childView is TextView){
                childView.text="Index:$i"
                childView.setOnClickListener {
                    Toast.makeText(applicationContext,"View:$i",Toast.LENGTH_SHORT).show()
                }
                childView.setOnLongClickListener {
                    Toast.makeText(applicationContext,"View long click:$i",Toast.LENGTH_SHORT).show()
                    false
                }
            }
        }

        for(i in 0 until innerLayout.childCount){
            val childView=innerLayout.getChildAt(i) as TextView
            childView.text="Inner index:$i"
            childView.setOnClickListener {
                Toast.makeText(applicationContext,"Inner View:$i",Toast.LENGTH_SHORT).show()
            }
            childView.setOnLongClickListener {
                Toast.makeText(applicationContext,"Inner View long click:$i",Toast.LENGTH_SHORT).show()
                true
            }
        }
    }
}
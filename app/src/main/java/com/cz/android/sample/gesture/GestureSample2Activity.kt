package com.cz.android.sample.gesture

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.sample.R
import com.cz.android.sample.api.Register

@Register(title = "GestureSample2")
class GestureSample2Activity : AppCompatActivity() {
    companion object {
        private const val TAG = "GestureSample2Activity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gesture_sample2)
        val layout3 = findViewById<View>(R.id.layout3)
        layout3.setOnClickListener { //
            Toast.makeText(applicationContext, "Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        Log.i(TAG, "dispatchTouchEvent===========================================")
        val result = super.dispatchTouchEvent(ev)
        Log.i(TAG, "=============================================================")
        Log.i(TAG, "\n\n")
        return result
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.i(TAG, "onTouchEvent")
        return super.onTouchEvent(event)
    }
}
package com.cz.android.sample.animator

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.sample.R
import com.cz.android.sample.api.Register

@Register(title = "ObjectAnimator", desc = "演示基本的ObjectAnimator")
class CloneAnimatorSampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animation_cloning)
        val container = findViewById<LinearLayout>(R.id.container)
        val animView = AnimationView(this)
        container.addView(animView)
        val starter = findViewById<Button>(R.id.startButton)
        starter.setOnClickListener { animView.startAnimation() }
    }
}
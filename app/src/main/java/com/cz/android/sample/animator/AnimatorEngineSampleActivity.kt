package com.cz.android.sample.animator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.animator.version1.SimpleValueAnimator
import com.cz.android.sample.R
import com.cz.android.sample.api.Register
import kotlinx.android.synthetic.main.activity_value_animator_sample.*

@Register(title = "ValueAnimator", desc = "演示基本的ValueAnimator")
class AnimatorEngineSampleActivity : AppCompatActivity() {
    companion object{
        private const val TAG="ValueAnimator"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_value_animator_sample)

        startButton.setOnClickListener {
            val valueAnimator = SimpleValueAnimator()
            valueAnimator.setDuration(1000)
            valueAnimator.setStartDelay(1000);
            valueAnimator.start()
        }
    }
}
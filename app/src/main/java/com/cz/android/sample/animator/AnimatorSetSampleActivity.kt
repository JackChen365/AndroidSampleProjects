package com.cz.android.sample.animator

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.animator.version3.SimpleAnimatorSet
import com.cz.android.animator.version3.SimpleValueAnimator
import com.cz.android.sample.R
import com.cz.android.sample.api.Register
import kotlinx.android.synthetic.main.activity_animator_set_sample.*

@Register(title = "AnimatorSet", desc = "演示动画组.")
class AnimatorSetSampleActivity : AppCompatActivity() {
    companion object{
        private const val TAG="AnimatorSetSample"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animator_set_sample)

        startButton.setOnClickListener {
            val animator1= SimpleValueAnimator("Animator1")
            val animator2=SimpleValueAnimator("Animator2")
            val animator3=SimpleValueAnimator("Animator3")
            val animator4=SimpleValueAnimator("Animator4")

            animator1.addUpdateListener {
                Log.i(TAG,"addUpdateListener:animator1")
            }
            animator2.addUpdateListener {
                Log.i(TAG,"addUpdateListener:animator2")
            }
            animator3.addUpdateListener {
                Log.i(TAG,"addUpdateListener:animator3")
            }
            animator4.addUpdateListener {
                Log.i(TAG,"addUpdateListener:animator4")
            }
            val animatorSet= SimpleAnimatorSet()
            animatorSet.play(animator1).before(animator2).before(animator3).before(animator4)
            animatorSet.play(animator1).before(animator2)
            animatorSet.play(animator1).after(animator3)
            animatorSet.play(animator1).after(animator4)
            animatorSet.playSequentially(animator1,animator2)
            animatorSet.startDelay=1000
            animatorSet.start()

        }
    }
}
package com.cz.android.sample.drawable

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.drawable.ColorDrawable
import com.cz.android.drawable.RippleDrawable
import com.cz.android.sample.R
import com.cz.android.sample.api.Register
import kotlinx.android.synthetic.main.activity_ripple_drawable_sample.*


/**
 * Why Android RippleDrawable could draw outside the view
 * https://stackoverflow.com/questions/45808796/how-do-rippledrawable-draw-outside-view-bounds
 */
@Register(title = "RippleDrawable")
class RippleDrawableSampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ripple_drawable_sample)
        initializeRipple1()
        initializeRipple2()
        initializeRipple3()
        initializeRipple4()
    }

    private fun initializeRipple1() {
        val rippleDrawable = RippleDrawable(ColorStateList.valueOf(Color.RED),null,null);
        rippleDrawable.maxRadius=200
        textView1.setRippleBackground(rippleDrawable);
    }

    private fun initializeRipple2() {
        val stateList = arrayOf(
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf(android.R.attr.state_focused),
            intArrayOf(android.R.attr.state_activated),
            intArrayOf()
        )
        //深蓝
        //深蓝
        val normalColor = Color.parseColor("#303F9F")
        //玫瑰红
        //玫瑰红
        val pressedColor = Color.parseColor("#FF4081")
        val stateColorList = intArrayOf(
            pressedColor,
            pressedColor,
            pressedColor,
            normalColor
        )
        val colorStateList = ColorStateList(stateList, stateColorList)

        val rippleDrawable = RippleDrawable(colorStateList, null, null)
        textView2.setRippleBackground(rippleDrawable)
    }

    private fun initializeRipple3(){
        val stateList = arrayOf(
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf(android.R.attr.state_focused),
            intArrayOf(android.R.attr.state_activated),
            intArrayOf()
        )

        //深蓝
        val normalColor = Color.parseColor("#303F9F")
        //玫瑰红
        val pressedColor = Color.parseColor("#FF4081")
        val stateColorList = intArrayOf(
            pressedColor,
            pressedColor,
            pressedColor,
            normalColor
        )
        val colorStateList = ColorStateList(stateList, stateColorList)

        val colorDrawable = ColorDrawable(Color.YELLOW);

        val rippleDrawable = RippleDrawable(colorStateList, colorDrawable, null)
        textView3.setRippleBackground(rippleDrawable)
    }

    private fun initializeRipple4() {
        val stateList = arrayOf(
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf(android.R.attr.state_focused),
            intArrayOf(android.R.attr.state_activated),
            intArrayOf()
        )

        //深蓝
        val normalColor = Color.parseColor("#303F9F")
        //玫瑰红
        val pressedColor = Color.parseColor("#FF4081")
        val stateColorList = intArrayOf(
            pressedColor,
            pressedColor,
            pressedColor,
            normalColor)
        val colorStateList = ColorStateList(stateList, stateColorList)

        val colorDrawable = ColorDrawable(Color.YELLOW);
        val rippleDrawable = RippleDrawable(colorStateList, null, colorDrawable)
        textView4.setRippleBackground(rippleDrawable)
    }
}
package com.cz.android.sample.transition

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.layouttransition.LayoutTransition
import com.cz.android.sample.R
import com.cz.android.sample.api.Register
import java.util.*
import kotlinx.android.synthetic.main.activity_layout_transition_sample.*

@Register(title = "LayoutTransition", desc = "演示子控件变化动画")
class LayoutTransitionSampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_transition_sample)
        val layoutTransition = LayoutTransition()
//        layoutTransition.setAnimateParentHierarchy(false)
        layoutTransition.enableTransitionType(LayoutTransition.CHANGE_APPEARING)
        layoutTransition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        layout.setLayoutTransition(layoutTransition)

//        val layoutTransition = android.animation.LayoutTransition()
//        layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
//        layout.setLayoutTransition(layoutTransition)

        val children= ArrayDeque<View>()
        addButton.setOnClickListener {
            val button = Button(this)
            button.isAllCaps=false
            button.text = "Button:${layout.childCount+1}"
            children.offer(button)
            layout.addView(button, 0, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        for(i in 0 until layout.childCount){
            children.offer(layout.getChildAt(i))
        }
        removeButton.setOnClickListener {
            if(!children.isEmpty()){
                val view = children.pollLast()
                layout.removeView(view)
            }
        }
        resizeButton.setOnClickListener {
            if(!children.isEmpty()){
                val child = layout.getChildAt(0)
                val layoutParams = child.layoutParams
                if(layoutParams.width==ViewGroup.LayoutParams.WRAP_CONTENT){
                    layoutParams.width=ViewGroup.LayoutParams.MATCH_PARENT
                } else {
                    layoutParams.width=ViewGroup.LayoutParams.WRAP_CONTENT
                }
                child.requestLayout()
            }
        }

        visibilityButton.setOnClickListener {
            if(!children.isEmpty()) {
                val child = layout.getChildAt(0)
                child.visibility=if(child.visibility==View.VISIBLE) View.GONE else View.VISIBLE
            }
        }
    }
}
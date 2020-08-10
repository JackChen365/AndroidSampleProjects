package com.cz.android.sample.text.div

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import com.cz.android.text.layout.Layout
import com.cz.android.text.layout.div.TextDivision

/**
 * @author Created by cz
 * @date 2020/8/6 5:34 PM
 * @email bingo110@126.com
 */
class ImageDiv : AppCompatImageView {
    companion object{
        private const val TAG="ImageDiv"
    }
    private var measureComplete=false
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun consumeText(layout: Layout,offset: Int, line: Int, lineOffset: Int): Boolean {
        if(0 == line && 2 == offset){
            measureComplete=true
            return true
        } else {
            return false;
        }
    }

    override fun onMeasureText(layout: Layout, left:Int, w:Float, offset: Int, line: Int, lineOffset: Int) {
        val measuredWidth = layout.width-left

        Log.i(TAG,
            "onMeasureText:$left letterWidth:$w offset:$offset line:$line lineOffset:$lineOffset"
        )
    }

    override fun onDrawText(layout: Layout, offset: Int, line: Int, lineOffset: Int) {

    }



}
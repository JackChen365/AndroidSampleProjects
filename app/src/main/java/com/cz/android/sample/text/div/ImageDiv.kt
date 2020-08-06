package com.cz.android.sample.text.div

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.cz.android.text.layout.Layout
import com.cz.android.text.layout.div.TextDivision

/**
 * @author Created by cz
 * @date 2020/8/6 5:34 PM
 * @email bingo110@126.com
 */
class ImageDiv : AppCompatImageView, TextDivision {
    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context!!, attrs, defStyleAttr) {
    }

    override fun apply(
        layout: Layout,
        line: Int,
        offset: Int,
        lineOffset: Int
    ): Int {
        return 0
    }
}
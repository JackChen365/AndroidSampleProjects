package com.cz.android.sample.text

import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.graphics.EmbossMaskFilter
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spannable
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.sample.R
import com.cz.android.sample.api.Register
import com.cz.android.text.span.*
import com.cz.android.text.spannable.SpannableString
import kotlinx.android.synthetic.main.activity_text_view_sample2.*

@Register(title="TextView2")
class TextViewSample2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_view_sample2)
        //初始化span列表
        val spanList= mutableMapOf<String,Any>()
        spanList.put("BackgroundColorSpan", BackgroundColorSpan(Color.RED))
        spanList.put("ForegroundColorSpan", ForegroundColorSpan(Color.YELLOW))
        spanList.put("RelativeSizeSpan 相对大小（文本字体）", RelativeSizeSpan(2.5f))
        spanList.put("MaskFilterSpan 修饰效果",
            MaskFilterSpan(BlurMaskFilter(3f, BlurMaskFilter.Blur.OUTER))
        )
        spanList.put("浮雕(EmbossMaskFilter)",
            MaskFilterSpan(EmbossMaskFilter(floatArrayOf(1f, 1f, 3f), 1.5f, 8f, 3f))
        )
        spanList.put("StrikethroughSpan 删除线（中划线）", StrikethroughSpan())
        spanList.put("UnderlineSpan 下划线", UnderlineSpan())
        spanList.put("AbsoluteSizeSpan 绝对大小（文本字体）", AbsoluteSizeSpan(20, true))
        val drawableSpan = object : DynamicDrawableSpan(ALIGN_BASELINE) {
            override fun getDrawable(): Drawable {
                val d = resources.getDrawable(R.mipmap.ic_launcher)
                d.setBounds(0, 0, 50, 50)
                return d
            }
        }
        spanList.put("DynamicDrawableSpan",drawableSpan)
        val d1 = resources.getDrawable(R.mipmap.ic_launcher)
        d1.setBounds(0, 0, 50, 100)
        spanList.put("ImageSpan 图片1", ImageSpan(d1))
//        spanList.put("RelativeSizeSpan 相对大小（文本字体）", RelativeSizeSpan(2.5f))
        spanList.put("ScaleXSpan 基于x轴缩放", ScaleXSpan(3.8f))
        spanList.put("StyleSpan 字体样式：粗体、斜体等", StyleSpan(Typeface.BOLD_ITALIC))
//        spanList.put("SuperscriptSpan", SuperscriptSpan())

        val textLines=assets.open("lyric").bufferedReader().readLines().toMutableList()
        val source=textLines.subList(0,spanList.size).joinToString("\n")
        val spannableString = SpannableString(source)
        var start=2
        spanList.entries.forEachIndexed { index, (title,spanItem)->
            val index=source.indexOf('\n',start)
            var end=if(-1==index) source.length else index
            spannableString.setSpan(spanItem, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            start=end+1
        }
        text1.setText(spannableString)
    }

}

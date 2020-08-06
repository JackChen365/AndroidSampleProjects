package com.cz.android.sample.text.buffer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.nested.NestedScrollView
import com.cz.android.sample.R
import com.cz.android.sample.text.sample.SimpleTextView
import com.cz.android.text.layout.buffer.BufferedBoringStaticLayout
import kotlinx.android.synthetic.main.activity_buffer_text_view_sample.*

class BufferTextViewSample1Activity : AppCompatActivity() {
    companion object{
        const val TAG="BufferTextView"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buffer_text_view_sample)
        initTextView(text1)
        initChunkTextView(text2)

        outputButton.setOnClickListener {
            val layout = text2.getLayout()
            if(null!=layout && layout is BufferedBoringStaticLayout){
                layout.outputLine()
                text2.requestLayout()
                scrollView.arrowScroll(NestedScrollView.FOCUS_DOWN)
            }
        }
    }

    /**
     * 初始化text
     */
    private fun initTextView(textView: SimpleTextView){
        val text = assets.open("chapter1").bufferedReader().readText()
        textView.setText(text)
    }

    /**
     * 初始化text
     */
    private fun initChunkTextView(textView: BufferedTextView){
        val text = assets.open("chapter1").bufferedReader().readText()
        textView.setText(text)
    }

}

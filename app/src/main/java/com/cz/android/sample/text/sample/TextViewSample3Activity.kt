package com.cz.android.sample.text.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.nested.NestedScrollView
import com.cz.android.sample.R
import com.cz.android.sample.api.Register
import com.cz.android.text.layout.ChunkBoringStaticLayout
import kotlinx.android.synthetic.main.activity_text_view_sample3.*

@Register(title="TextView3")
class TextViewSample3Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_view_sample3)
        initTextView(text1)
        initChunkTextView(text2)

        outputButton.setOnClickListener {
            val layout = text2.getLayout()
            if(null!=layout && layout is ChunkBoringStaticLayout){
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
        val text = assets.open("text_chapter").bufferedReader().readText()
        textView.setText(text)
    }

    /**
     * 初始化text
     */
    private fun initChunkTextView(textView: BoringChunkTextView){
        val text = assets.open("text_chapter").bufferedReader().readText()
        textView.setText(text)
    }

}

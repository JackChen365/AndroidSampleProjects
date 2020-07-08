package com.cz.android.sample.text

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.sample.R
import com.cz.android.sample.api.Register
import kotlinx.android.synthetic.main.activity_text_view_sample1.*

@Register(title="TextView1")
class TextViewSample1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_view_sample1)
        //初始化第一个TextView
        initTextView(text1)
    }

    /**
     * 初始化text
     */
    private fun initTextView(textView: SimpleTextView){
        val text = assets.open("chapter1").bufferedReader().readText()
        textView.setText(text)
        //添加调试信息
        textView.setOnMessageChangeListener(object :SimpleTextView.OnTextClickListener{
            override fun onTextClicked(textLineInfo: SimpleTextView.TextLineInfo) {
                messageTextView.text = textLineInfo.toString()
            }
        })
    }

}

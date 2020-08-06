package com.cz.android.sample.text.sample

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import com.cz.android.sample.R
import com.cz.android.text.layout.Layout
import com.cz.android.text.layout.StaticLayout
import kotlin.system.measureTimeMillis


class SimpleTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ViewGroup(context, attrs, defStyleAttr) {

        companion object{
                private const val TAG="SimpleTextView"
        }
        /**
         * 绘制文本
         */
        private var text:CharSequence?=null
        /**
         * 文本排版layout对象
         */
        private var layout: Layout?=null
        /**
         * 画笔对象
         */
        private var textPaint=TextPaint(Paint.ANTI_ALIAS_FLAG)
        /**
         * 消息监听
         */
        private var listener: OnTextClickListener?=null
        /**
         * 计算char数组,可以重复使用
         */
        private var charArray:CharArray?=null


        init {
                //接收touch事件
                isClickable=true
                setWillNotDraw(false)
                textPaint.textSize = resources.getDimension(R.dimen.text_size)
                textPaint.color= Color.BLACK

        }
        /**
         * 设置文本
         */
        fun setText(text:CharSequence){
                this.text=text
                requestLayout()
        }

        fun getLineCount():Int{
                return layout?.lineCount?:0
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                if(null!=text&& 0 != measuredWidth &&(null==layout||text!=layout?.text)){
                        val measureTimeMillis = measureTimeMillis {
                                layout = StaticLayout(
                                        text,
                                        textPaint,
                                        measuredWidth - paddingLeft - paddingRight,
                                        0f
                                )
                        }
                        //添加调试信息
                        Log.e(TAG,"初始化Layout时长:$measureTimeMillis\n")

                }
                val layoutHeight=layout?.height?:0
                setMeasuredDimension(measuredWidth,paddingTop+layoutHeight+paddingBottom)
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        }

        override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)
                //调试绘制信息
                debugDraw(canvas)
                //绘制选中
                canvas.save()
                canvas.translate(paddingLeft*1f,paddingTop*1f)
                layout?.draw(canvas)
                canvas.restore()
        }


        /**
         * 调试绘制信息
         */
        private fun debugDraw(canvas: Canvas) {
                val paint=Paint(Paint.ANTI_ALIAS_FLAG)
                paint.strokeWidth=1f
                paint.color=Color.RED
                paint.style=Paint.Style.STROKE
                //绘制内边距
                canvas.drawRect(Rect(paddingLeft,paddingTop,width-paddingRight,height-paddingBottom),paint)
                //绘制每一行间隔
                val layout=layout
                if(null!=layout){
                        val lineCount = layout.lineCount
                        canvas.save()
                        canvas.translate(paddingLeft*1f,paddingTop*1f)
                        for(i in 0 until lineCount){
                                val lineBottom = layout.getLineBottom(i)
                                canvas.drawLine(0f,lineBottom*1f,
                                        (width-paddingLeft-paddingRight).toFloat(),lineBottom*1f,paint)
                        }
                        canvas.restore()
                }
        }

        fun setOnMessageChangeListener(listener: OnTextClickListener){
                this.listener=listener
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
                if(MotionEvent.ACTION_UP==event.actionMasked){
                        val layout=layout
                        if(null!=layout){
                                //获得所在行
                                val y = event.y.toInt()
                                val line=layout.getLineForVertical(y)
                                val lineStart = layout.getLineStart(line)
                                val lineEnd = layout.getLineEnd(line)
                                var charArray=charArray
                                if(null==charArray||charArray.size<(lineEnd-lineStart)){
                                        charArray= CharArray(lineEnd-lineStart)
                                }
                                TextUtils.getChars(text,lineStart,lineEnd,charArray,0)

                                val textLineInfo =
                                    TextLineInfo(
                                        line,
                                        String(charArray)
                                    )
                                textLineInfo.lineStart=lineStart
                                textLineInfo.lineEnd=lineEnd
                                textLineInfo.lineTop = layout.getLineTop(line)
                                textLineInfo.lineBottom = layout.getLineBottom(line)
                                textLineInfo.lineDescent = layout.getLineDescent(line)
                                listener?.onTextClicked(textLineInfo)

                        }
                }
                return super.onTouchEvent(event)
        }


        /**
         * 行信息
         */
        class TextLineInfo(val line:Int,val text:String){
                var lineTop = 0
                var lineBottom = 0
                var lineDescent = 0
                var lineStart = 0
                var lineEnd = 0

                override fun toString(): String {
                        return "line:$line text:$text\n" +
                                "lineStart:$lineStart lineEnd:$lineEnd lineTop:$lineTop lineBottom:$lineBottom lineDescent:$lineDescent"
                }
        }

        interface OnTextClickListener{
                fun onTextClicked(textLineInfo: TextLineInfo)
        }

}
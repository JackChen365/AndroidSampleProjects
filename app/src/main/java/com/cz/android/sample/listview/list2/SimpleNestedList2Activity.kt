package com.cz.android.sample.listview.list2

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.sample.R
import com.cz.android.sample.api.Register
import com.cz.android.sample.library.data.DataManager
import com.cz.android.sample.listview.list2.adapter.HorizontalSampleAdapter
import com.cz.android.sample.listview.list2.adapter.VerticalSampleAdapter
import com.cz.android.sample.listview.list2.decoration.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_simple_nested_list2.*

@Register(title = "SimpleListView2", desc = "演示不同方向列表操作")
class SimpleNestedList2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_nested_list2)

        val dataProvider = DataManager.getDataProvider(this)
        val wordList = dataProvider.getWordList(100)
        nestedListView.adapter= VerticalSampleAdapter(wordList)
        nestedListView.addItemDecoration(DividerItemDecoration(this))
        nestedListView.addItemDecoration(object : SimpleNestedListView.ItemDecoration() {
            override fun onDrawOver(c: Canvas, parent: SimpleNestedListView) {
                super.onDrawOver(c, parent)
                //Only for debugger.
                val paint=Paint(Paint.ANTI_ALIAS_FLAG)
                paint.setColor(Color.RED)
                paint.strokeWidth=resources.getDimension(R.dimen.sample_divide)

                c.drawLine(0f,0f,parent.width.toFloat(),0f,paint)
                c.drawLine(0f,parent.height.toFloat(),parent.width.toFloat(),parent.height.toFloat(),paint)
            }
        })

        radioLayout.setOnCheckedChangeListener { _, index, isChecked ->
            if(isChecked){
                if(0==index){
                    nestedListView.setOrientation(SimpleNestedListView.HORIZONTAL)
                    nestedListView.adapter= HorizontalSampleAdapter(wordList)
                } else if(1==index){
                    nestedListView.setOrientation(SimpleNestedListView.VERTICAL)
                    nestedListView.adapter= VerticalSampleAdapter(wordList)
                }
            }
        }

        scrollButton.setOnClickListener {
            nestedListView.scrollBy(0,500)
        }
    }
}
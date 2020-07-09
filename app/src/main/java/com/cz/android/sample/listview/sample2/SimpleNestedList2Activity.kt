package com.cz.android.sample.listview.sample2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.sample.R
import com.cz.android.sample.api.Register
import com.cz.android.sample.api.TestCase
import com.cz.android.sample.library.data.DataManager
import com.cz.android.sample.listview.sample2.adapter.HorizontalSampleAdapter
import com.cz.android.sample.listview.sample2.adapter.VerticalSampleAdapter
import com.cz.android.sample.listview.sample2.decoration.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_simple_nested_list2.*

@TestCase
@Register(title = "SimpleListView2", desc = "演示不同方向列表操作")
class SimpleNestedList2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_nested_list2)

        val dataProvider = DataManager.getDataProvider(this)
        val wordList = dataProvider.getWordList(100)
        nestedListView.adapter= VerticalSampleAdapter(wordList)
        nestedListView.addItemDecoration(DividerItemDecoration())

        radioLayout.setOnCheckedChangeListener { _, index, _ ->
            if(0==index){
                nestedListView.setOrientation(SimpleNestedListView.VERTICAL)
                nestedListView.adapter= VerticalSampleAdapter(wordList)
            } else if(1==index){
                nestedListView.setOrientation(SimpleNestedListView.HORIZONTAL)
                nestedListView.adapter= HorizontalSampleAdapter(wordList)
            }
        }
    }
}
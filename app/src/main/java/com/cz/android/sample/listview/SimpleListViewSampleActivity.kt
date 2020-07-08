package com.cz.android.sample.listview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.sample.R
import com.cz.android.sample.api.Register
import com.cz.android.sample.library.data.DataManager
import kotlinx.android.synthetic.main.activity_sample_list_view.*

@Register(title = "SimpleRecyclerView", desc = "演示最基本的控件列表数据复用")
class SimpleListViewSampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_list_view)

        val dataProvider = DataManager.getDataProvider(this)
        listView.setAdapter(SampleAdapter(dataProvider.getWordList(100)))
    }
}
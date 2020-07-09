package com.cz.android.sample.recycler

import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cz.android.sample.R
import com.cz.android.sample.api.Register
import com.cz.android.sample.library.adapter.SimpleArrayAdapter
import com.cz.android.sample.library.data.DataManager
import kotlinx.android.synthetic.main.activity_reccyler_view_recycer_test.*

@Register(title = "RecyclerViewRecyclerTest")
class RecyclerViewRecyclerTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reccyler_view_recycer_test)

        val dataProvider = DataManager.getDataProvider(this)
        recyclerView.layoutManager=SimpleLinearLayoutManager(this,SimpleLinearLayoutManager.HORIZONTAL,false)
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                super.onDrawOver(c, parent, state)
                Log.i("onDrawOver","childCount:"+parent.childCount)
            }
        })
        recyclerView.adapter=HorizontalSampleAdapter(dataProvider.getWordList(100))
    }
}
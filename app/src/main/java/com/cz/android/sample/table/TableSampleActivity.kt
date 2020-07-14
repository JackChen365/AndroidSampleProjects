package com.cz.android.sample.table

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.sample.R
import com.cz.android.sample.api.Register
import com.cz.android.sample.api.TestCase
import com.cz.android.sample.library.data.DataManager
import kotlinx.android.synthetic.main.activity_table_sample.*

@TestCase
@Register(title = "表格演示")
class TableSampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table_sample)

        var startIndex=0
        val rowCount=40
        val columnCount=1
        val dataProvider = DataManager.getDataProvider(this)
        val list = (0 until rowCount).map { dataProvider.getWordList(startIndex++, columnCount).toList() }.toList()
        val tableAdapter = SimpleTableLayoutAdapter(this, list)

//        zoomLayout.addItemDecoration(TableDecoration(this))
        zoomLayout.setAdapter(tableAdapter)
        scaleButton.setOnClickListener {
            zoomLayout.setViewScale(1.5f)
        }
    }
}
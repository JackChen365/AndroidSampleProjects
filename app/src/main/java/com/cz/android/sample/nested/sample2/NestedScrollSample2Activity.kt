package com.cz.android.sample.nested.sample2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.sample.R
import com.cz.android.sample.api.Register

@Register(title = "NestedScrollSample2")
class NestedScrollSample2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nested_scroll_sample2)
    }
}
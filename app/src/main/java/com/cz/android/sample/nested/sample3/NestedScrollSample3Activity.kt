package com.cz.android.sample.nested.sample3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cz.android.sample.R
import com.cz.android.sample.api.Register
import com.cz.android.sample.api.TestCase
import com.cz.android.sample.library.adapter.SimpleFragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_nested_scroll_sample3.*

@Register(title = "NestedScrollSample3")
class NestedScrollSample3Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nested_scroll_sample3)

        val fragmentList= mutableListOf<Fragment>()
        fragmentList.add(NestedListSampleFragment.newFragment())
        fragmentList.add(NestedListSampleFragment.newFragment())
        fragmentList.add(NestedListSampleFragment.newFragment())
        val adapter=SimpleFragmentPagerAdapter(supportFragmentManager,fragmentList,null)
        viewPager.adapter=adapter
    }
}
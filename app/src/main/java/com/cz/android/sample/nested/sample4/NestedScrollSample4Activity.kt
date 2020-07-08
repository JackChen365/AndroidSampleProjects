package com.cz.android.sample.nested.sample4

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cz.android.sample.R
import com.cz.android.sample.api.Register
import com.cz.android.sample.library.adapter.SimpleFragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_nested_scroll_sample4.*

@Register(title = "NestedScrollSample4")
class NestedScrollSample4Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nested_scroll_sample4)

        val fragmentList= mutableListOf<Fragment>()
        fragmentList.add(NestedListSampleFragment.newFragment())
        fragmentList.add(NestedListSampleFragment.newFragment())
        fragmentList.add(NestedListSampleFragment.newFragment())
        val adapter= SimpleFragmentPagerAdapter(supportFragmentManager,fragmentList,null)
        viewPager.adapter=adapter
    }
}
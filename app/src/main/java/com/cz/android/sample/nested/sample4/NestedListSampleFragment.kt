package com.cz.android.sample.nested.sample4

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cz.android.sample.R
import com.cz.android.sample.library.data.DataManager
import kotlinx.android.synthetic.main.fragment_nested_list_sample.*

/**
 * @author Created by cz
 * @date 2020/7/7 6:27 PM
 * @email bingo110@126.com
 */
class NestedListSampleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nested_list_sample, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val context = context
        val dataProvider = DataManager.getDataProvider(context)
        listView.setAdapter(
            SampleAdapter(
                dataProvider.getWordList(
                    100
                )
            )
        )
    }

    companion object {
        fun newFragment(): Fragment {
            return NestedListSampleFragment()
        }
    }
}
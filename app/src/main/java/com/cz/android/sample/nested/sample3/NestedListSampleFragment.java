package com.cz.android.sample.nested.sample3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cz.android.sample.R;

/**
 * @author Created by cz
 * @date 2020/7/7 6:27 PM
 * @email bingo110@126.com
 */
public class NestedListSampleFragment extends Fragment {
    public static Fragment newFragment(){
        return new NestedListSampleFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nested_list_sample,container,false);
    }
}

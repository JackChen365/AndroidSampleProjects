package com.cz.android.sample.nested.sample1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.cz.android.sample.R;
import com.cz.android.sample.api.Register;

@Register(title="NestedScrollSample1")
public class NestedScrollSample1Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nested_scroll_sample1);
    }
}

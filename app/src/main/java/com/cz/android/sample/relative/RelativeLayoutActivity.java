package com.cz.android.sample.relative;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.cz.android.sample.R;
import com.cz.android.sample.api.Register;

@Register(title="演示相对布局",desc="自定义相对布局相关操作")
public class RelativeLayoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reletive_layout);
    }
}

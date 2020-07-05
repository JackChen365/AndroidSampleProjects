package com.cz.android.sample.relative;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.cz.android.sample.R;
import com.cz.android.sample.api.Register;

@Register(title="演示约束布局",desc="自定义约束布局相关操作")
public class SimpleConstraintLayoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_constraint_layout);
    }
}

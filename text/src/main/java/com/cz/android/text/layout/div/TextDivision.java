package com.cz.android.text.layout.div;

import androidx.annotation.NonNull;

import com.cz.android.text.layout.Layout;

/**
 * @author Created by cz
 * @date 2020/8/5 10:37 AM
 * @email bingo110@126.com
 */
public interface TextDivision {

    boolean consumeText(@NonNull Layout layout,int offset,int line,int lineOffset);

    void onMeasureText(@NonNull Layout layout,int width,float letterWidth,int offset,int line,int lineOffset);

    void onDrawText(@NonNull Layout layout,int offset,int line,int lineOffset);
}

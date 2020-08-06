package com.cz.android.sample.tool;

import android.content.Context;

import java.util.Map;

/**
 * @author Created by cz
 * @date 2020/8/6 12:03 PM
 * @email bingo110@126.com
 */
public interface ComponentAnalyzer {

    void registerComponent(Context context);

    boolean applyInstance(Class<?> clazz);

    void analysis(Context context,Class<?> clazz,Map<String,Long> traceMap);
}

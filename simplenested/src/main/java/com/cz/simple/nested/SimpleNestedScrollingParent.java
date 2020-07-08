package com.cz.simple.nested;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

/**
 * @author Created by cz
 * @date 2020/7/7 11:19 AM
 * @email bingo110@126.com
 */
public interface SimpleNestedScrollingParent {
    int getNestedScrollAxes();

    boolean onStartNestedScroll(@NonNull View child, @NonNull View target, @ViewCompat.ScrollAxis int axes,
                                @ViewCompat.NestedScrollType int type);

    void onNestedScrollAccepted(@NonNull View child, @NonNull View target, @ViewCompat.ScrollAxis int axes,
                                @ViewCompat.NestedScrollType int type);

    void onStopNestedScroll(@NonNull View target, @ViewCompat.NestedScrollType int type);

    void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed,
                        int dxUnconsumed, int dyUnconsumed, @ViewCompat.NestedScrollType int type, @NonNull int[] consumed);


    void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed,
                           @ViewCompat.NestedScrollType int type);


    boolean onNestedFling(@NonNull View target, float velocityX, float velocityY, boolean consumed);


    boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY);
}

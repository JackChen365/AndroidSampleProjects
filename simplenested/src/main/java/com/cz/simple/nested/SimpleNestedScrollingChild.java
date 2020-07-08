package com.cz.simple.nested;

/**
 * @author Created by cz
 * @date 2020/7/7 10:26 AM
 * @email bingo110@126.com
 */
public interface SimpleNestedScrollingChild {

    void setNestedScrollingEnabled(boolean enable);

    boolean isNestedScrollingEnabled();

    boolean hasNestedScrollingParent(int type);

    boolean startNestedScroll(int axes, int type);

    void stopNestedScroll(int type);

    boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow, int type);

    boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow, int type,int[] consumed);

    boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed);

    boolean dispatchNestedPreFling(float velocityX, float velocityY);
}

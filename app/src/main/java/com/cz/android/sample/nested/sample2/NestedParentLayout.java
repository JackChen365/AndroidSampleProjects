package com.cz.android.sample.nested.sample2;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.cz.simple.nested.SimpleNestedScrollingParent;

/**
 * @author Created by cz
 * @date 2020/7/7 2:44 PM
 * @email bingo110@126.com
 */
public class NestedParentLayout extends ViewGroup implements SimpleNestedScrollingParent {
    private static final String TAG="NestedParentLayout";
    private int nestedOffsetTop=0;

    public NestedParentLayout(@NonNull Context context) {
        super(context);
    }

    public NestedParentLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedParentLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        measureChildren(widthMeasureSpec,heightMeasureSpec);
        int totalHeight=0;
        int childCount = getChildCount();
        for(int i=0;i<childCount;i++){
            View childView = getChildAt(i);
            int measuredHeight = childView.getMeasuredHeight();
            totalHeight+=measuredHeight;
        }
        int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth,totalHeight);
    }

    @Override
    protected void onLayout(boolean b, int left, int top, int right, int bottom) {
        int topOffset=0;
        int childCount = getChildCount();
        for(int i=0;i<childCount;i++){
            View childView = getChildAt(i);
            int measuredHeight = childView.getMeasuredHeight();
            childView.layout(left,topOffset,right,topOffset+measuredHeight);
            topOffset+=measuredHeight;
        }
        int oldOffset = nestedOffsetTop;
        nestedOffsetTop = 0;
        offsetTopAndBottom(oldOffset);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        return 0 != (ViewCompat.SCROLL_AXIS_VERTICAL&axes);
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
        if(0 > dyUnconsumed&&!target.canScrollVertically(dyConsumed)){
            consumed[1]=dyUnconsumed;
        }
    }

    @Override
    public void offsetTopAndBottom(int offset) {
        super.offsetTopAndBottom(offset);
        nestedOffsetTop+=offset;
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        Log.i(TAG,"onNestedPreScroll:"+dy);
        int top = getTop();
        int targetTop = getViewTop(this, target);
        if(0 < dy && -top < targetTop){
            //Over the top of the parent.
            if(dy-top > targetTop){
                offsetTopAndBottom(-targetTop-top);
            } else {
                offsetTopAndBottom(-dy);
            }
            consumed[1]=dy;
        } else if(0 > dy&&!target.canScrollVertically(dy)){
            //Up to the top of the parent.
            if(0 > top){
                if(0 < top-dy){
                    offsetTopAndBottom(-top);
                    consumed[1]=dy;
                } else {
                    offsetTopAndBottom(-dy);
                    consumed[1]=dy;
                }
            }
        }
    }

    private int getViewTop(ViewGroup parentLayout,View childView) {
        int top = childView.getTop();
        ViewParent parent = childView.getParent();
        while(parent!=parentLayout){
            View parentView = (View) parent;
            top+=parentView.getTop();
            parent=parent.getParent();
        }
        return top;
    }

    private int getViewBottom(ViewGroup parentLayout,View childView) {
        int bottom = childView.getBottom();
        ViewParent parent = childView.getParent();
        while(parent!=parentLayout){
            View parentView = (View) parent;
            bottom+=parentView.getBottom();
            parent=parent.getParent();
        }
        return bottom;
    }
}

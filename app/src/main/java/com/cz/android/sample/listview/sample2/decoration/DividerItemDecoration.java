package com.cz.android.sample.listview.sample2.decoration;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import com.cz.android.sample.listview.sample2.SimpleNestedListView;

/**
 * @author Created by cz
 * @date 2020/7/9 5:41 PM
 * @email bingo110@126.com
 */
public class DividerItemDecoration extends SimpleNestedListView.ItemDecoration {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);

    public DividerItemDecoration() {
        paint.setColor(Color.RED);
    }

    @Override
    public void onDraw(Canvas c, SimpleNestedListView parent) {
        super.onDraw(c, parent);
        int childCount = parent.getChildCount();
        int orientation = parent.getOrientation();
        for(int i=0;i<childCount;i++){
            View childView = parent.getChildAt(i);
            Rect outRect = parent.getDecoratedInsets(childView);
            if(SimpleNestedListView.HORIZONTAL==orientation){
                int paddingTop = parent.getPaddingTop();
                int paddingBottom = parent.getPaddingBottom();
                int height = parent.getHeight();
                int right = childView.getRight();
                int strokeWidth = outRect.right;
                paint.setStrokeWidth(strokeWidth);
                c.drawLine(right+strokeWidth/2,paddingTop,right+strokeWidth/2,height+paddingBottom/2,paint);
            } else if(SimpleNestedListView.VERTICAL==orientation){
                int paddingLeft = parent.getPaddingLeft();
                int paddingRight = parent.getPaddingRight();
                int width = parent.getWidth();
                int bottom = childView.getBottom();
                int strokeWidth = outRect.bottom;
                paint.setStrokeWidth(outRect.bottom);
                c.drawLine(paddingLeft,bottom+strokeWidth/2,width-paddingRight,bottom+strokeWidth/2,paint);
            }
        }
    }

    @Override
    public void onDrawOver(Canvas c, SimpleNestedListView parent) {
        super.onDrawOver(c, parent);
    }

    @Override
    public void getItemOffsets(Rect outRect, int itemPosition, SimpleNestedListView parent) {
        super.getItemOffsets(outRect,itemPosition, parent);
        int orientation = parent.getOrientation();
        if(SimpleNestedListView.HORIZONTAL==orientation){
            outRect.set(0,0,40,0);
        } else if(SimpleNestedListView.VERTICAL==orientation){
            outRect.set(0,0,0,40);
        }

    }
}

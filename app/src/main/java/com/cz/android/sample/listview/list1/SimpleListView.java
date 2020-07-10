package com.cz.android.sample.listview.list1;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EdgeEffect;
import android.widget.OverScroller;

import androidx.core.view.ViewCompat;

import java.util.LinkedList;

public class SimpleListView extends ViewGroup {
    private static final String TAG="SimpleRecyclerView";
    private static final int DIRECTION_START=-1;
    private static final int DIRECTION_END=1;
    private final RecyclerBin recyclerBin=new RecyclerBin();
    private final LayoutState layoutState=new LayoutState();
    private Adapter adapter;
    private boolean requestLayout=false;

    private final ViewFlinger viewFlinger;
    private boolean isBeingDragged = false;
    private int touchSlop;
    private float lastMotionX = 0f;
    private float lastMotionY = 0f;

    private VelocityTracker velocityTracker = null;
    private int minimumVelocity;
    private int maximumVelocity;

    private EdgeEffect mEdgeGlowTop;
    private EdgeEffect mEdgeGlowBottom;

    public SimpleListView(Context context) {
        this(context,null,0);
    }

    public SimpleListView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SimpleListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        touchSlop = configuration.getScaledTouchSlop();
        minimumVelocity = configuration.getScaledMinimumFlingVelocity();
        maximumVelocity = configuration.getScaledMaximumFlingVelocity();
        viewFlinger=new ViewFlinger(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        recyclerBin.setMeasureSpecs(widthMeasureSpec,heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        if(requestLayout&&null!=adapter){
            requestLayout=false;
            initializeLayoutState();
            fillAndRecyclerLayout();
        }
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    private void initializeLayoutState() {
        int measuredHeight = getMeasuredHeight();
        layoutState.available=measuredHeight;
        layoutState.itemDirection=DIRECTION_END;
        layoutState.position=0;
        layoutState.scrollingOffset =0;
    }

    private int fillAndRecyclerLayout() {
        int available = layoutState.available;
        while(0 < available&&hasMore()){
            int position = layoutState.position;
            int viewType = adapter.getViewType(position);
            View view = recyclerBin.getView(position,viewType);
            adapter.onBindView(view,position);
            //测量控件
            recyclerBin.measureChild(view);
            int measuredWidth = view.getMeasuredWidth();
            int measuredHeight = view.getMeasuredHeight();
            if(layoutState.itemDirection==DIRECTION_END){
                int top=layoutState.scrollingOffset;
                view.layout(0,top,measuredWidth,top+measuredHeight);
                addAdapterView(view,-1);
                recyclerList(layoutState);
                layoutState.scrollingOffset +=measuredHeight;
            } else if(layoutState.itemDirection==DIRECTION_START){
                int bottom=layoutState.scrollingOffset;
                view.layout(0,bottom-measuredHeight,measuredWidth,bottom);
                addAdapterView(view,0);
                recyclerList(layoutState);
                layoutState.scrollingOffset -=measuredHeight;
            }
            available-=measuredHeight;
            layoutState.position+=layoutState.itemDirection;
            layoutState.available-=measuredHeight;
        }
        return available-layoutState.available;
    }

    private void recyclerList(LayoutState layoutState) {
        if(layoutState.itemDirection==DIRECTION_END){
            recycleFromStart();
        } else if(layoutState.itemDirection==DIRECTION_START){
            recycleFromEnd();
        }
    }


    private void recycleFromStart() {
        int childCount = getChildCount();
        if(0 < childCount){
            int index=0;
            int height = getHeight();
            int scrollingOffset = layoutState.scrollingOffset;
            int top=scrollingOffset-height;
            for(int i=0;i<childCount;i++){
                View childView = getChildAt(i);
                int childTop = childView.getTop();
                int bottom = childView.getBottom();
                if(childTop < top && bottom > top){
                    index = i;
                    break;
                }
            }
            if(0 < index){
                recyclerChild(0,index);
            }
        }
    }

    private void recycleFromEnd() {
        int childCount = getChildCount();
        if(0 < childCount){
            int index=childCount;
            int height = getHeight();
            int scrollingOffset = layoutState.scrollingOffset;
            int bottom=scrollingOffset+height;
            for(int i=childCount-1;i>=0;i--){
                View childView = getChildAt(i);
                int childTop = childView.getTop();
                int childBottom = childView.getBottom();
                if(childTop < bottom && childBottom>bottom){
                    index=i+1;
                    break;
                }
            }
            if(index < childCount){
                recyclerChild(index,childCount);
            }
        }
    }

    private void recyclerChild(int start, int end) {
        int index=start;
        while(index<end){
            View childView = getChildAt(start);
            if(null!=childView){
                onRecycleChild(childView);
                removeAdapterView(childView);
                recyclerBin.addScarpView(childView);
            }
            index++;
        }
    }

    protected void onRecycleChild(View childView) {
        //Subclass could do something here.
    }

    public int getPosition(View child){
        LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
        return layoutParams.position;
    }


    private boolean hasMore() {
        int itemCount = adapter.getItemCount();
        return 0 <= layoutState.position && layoutState.position < itemCount;
    }

    public boolean canScrollHorizontally() {
        return true;
    }

    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public void scrollBy(int x, int y) {
        final boolean canScrollHorizontal = canScrollHorizontally();
        final boolean canScrollVertical = canScrollVertically();
        if (canScrollHorizontal || canScrollVertical) {
            scrollByInternal(canScrollHorizontal ? x : 0, canScrollVertical ? y : 0);
        }
    }

    protected void scrollByInternal(int dx, int dy) {
        int consumedX=0;
        if (dx != 0) {
            consumedX = scrollHorizontallyBy(dx);
        }
        int consumedY=0;
        if (dy != 0) {
            consumedY = scrollVerticallyBy(dy);
        }
        if(0 != consumedX||0 != consumedY){
            offsetChildren(-consumedX,-consumedY);
        }
    }

    private void offsetChildren(int consumedX, int consumedY) {
        int childCount = getChildCount();
        for(int i=0;i<childCount;i++){
            View childView = getChildAt(i);
            if(0!=consumedX){
                childView.offsetLeftAndRight(consumedX);
            }
            if(0!=consumedY){
                childView.offsetTopAndBottom(consumedY);
            }
        }
    }

    private int scrollHorizontallyBy(int dx) {
        return 0;
    }

    private int scrollVerticallyBy(int dy) {
        int absDy=Math.abs(dy);
        int scrollingOffset = updateLayoutState(dy);
        int consumed = scrollingOffset+ fillAndRecyclerLayout();
        int layoutDirection=(0 < dy) ? DIRECTION_END : DIRECTION_START;
        consumed = absDy > consumed ? overScroll(layoutDirection,consumed,dy) : dy;
        return consumed;
    }

    public int overScroll(int layoutDirection,int consumed,int dy){
        int absDy=Math.abs(dy);
        int overMaxDistance=0;
        if(DIRECTION_START==layoutDirection){
            View view=getChildAt(0);
            int start=view.getTop();
            if((overMaxDistance-start)<absDy){
                dy=start-overMaxDistance;
            }
        } else if(DIRECTION_END==layoutDirection){
            int childCount = getChildCount();
            View view=getChildAt(childCount-1);
            int height = getHeight();
            int end=view.getBottom();
            overMaxDistance=height-overMaxDistance;
            if((end-overMaxDistance)<absDy){
                dy=end-overMaxDistance;
            }
        }
        return dy;
    }

    private int updateLayoutState(int dy) {
        int offset=0;
        int childCount = getChildCount();
        if(0 == dy||0 == childCount) return offset;
        int itemDirection=(0 < dy) ? DIRECTION_END : DIRECTION_START;
        if(itemDirection == DIRECTION_START){
            View childView = getChildAt(0);
            LayoutParams layoutParams = (LayoutParams) childView.getLayoutParams();
            layoutState.position=layoutParams.position+itemDirection;
            layoutState.itemDirection=itemDirection;
            layoutState.scrollingOffset =childView.getTop();
            layoutState.available=childView.getTop()-dy;
            offset=-childView.getTop()+getPaddingTop();
        } else if(itemDirection == DIRECTION_END){
            View childView = getChildAt(childCount - 1);
            int height = getHeight();
            LayoutParams layoutParams = (LayoutParams) childView.getLayoutParams();
            layoutState.position=layoutParams.position+itemDirection;
            layoutState.itemDirection=itemDirection;
            layoutState.scrollingOffset =childView.getBottom();
            offset=childView.getBottom()-height;
            layoutState.available=(height-layoutState.scrollingOffset)+dy;
        }
        return offset;
    }


    public void setAdapter(Adapter adapter){
        this.adapter=adapter;
        requestLayout=true;
        requestLayout();
    }

    protected void addAdapterView(View childView,int index){
        SimpleListView.LayoutParams layoutParams = (SimpleListView.LayoutParams) childView.getLayoutParams();
        if(layoutParams.isCached){
            attachViewToParent(childView,index,layoutParams);
        } else {
            addView(childView,index,layoutParams);
        }
    }

    protected View newAdapterView(ViewGroup parent, int viewType) {
        if(null==adapter){
            throw new NullPointerException("The adapter is null!");
        }
        return adapter.onCreateView(parent,viewType);
    }


    /**
     * We will remove the view if it still attach in the group.
     * @param view
     */
    protected void removeAdapterView(View view){
        ViewParent parent = view.getParent();
        if(null!=parent){
            detachViewFromParent(view);
        }
    }

    private class RecyclerBin{
        SparseArray<LinkedList<View>> scrapViews= new SparseArray<>();
        private int widthMode, heightMode;
        private int width, height;

        void addScarpView(View view){
            removeAdapterView(view);
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            int viewType = layoutParams.viewType;
            LinkedList<View> cachedList = scrapViews.get(viewType);
            if(null==cachedList){
                cachedList=new LinkedList<>();
                scrapViews.put(viewType,cachedList);
            }
            cachedList.add(view);
        }

        void detachAndScrapAttachedViews(){
            while(0<getChildCount()){
                View childView = getChildAt(0);
                addScarpView(childView);
            }
        }

        void setMeasureSpecs(int wSpec, int hSpec) {
            width = MeasureSpec.getSize(wSpec);
            widthMode = MeasureSpec.getMode(wSpec);
            if (widthMode == MeasureSpec.UNSPECIFIED) {
                width = 0;
            }
            height = MeasureSpec.getSize(hSpec);
            heightMode = MeasureSpec.getMode(hSpec);
            if (heightMode == MeasureSpec.UNSPECIFIED) {
                height = 0;
            }
        }

        void clear(){
            scrapViews.clear();
        }

        int getScrapCount(){
            int count=0;
            for(int i=0;i<scrapViews.size();i++){
                LinkedList<View> views = scrapViews.valueAt(i);
                if(null!=views){
                    count+=views.size();
                }
            }
            return count;
        }

        void measureChild(View view){
            int parentWidthMeasureSpec= MeasureSpec.makeMeasureSpec(width, widthMode);
            int parentHeightMeasureSpec= MeasureSpec.makeMeasureSpec(height, heightMode);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            //Create a new measure spec for the child view.
            int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, 0, layoutParams.width);
            int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec, 0, layoutParams.height);
            view.measure(childWidthMeasureSpec,childHeightMeasureSpec);
        }

        View getView(int position,int viewType){
            View view;
            LinkedList<View> cachedList = scrapViews.get(viewType);
            if(null!=cachedList&&!cachedList.isEmpty()){
                view=cachedList.pollFirst();
                LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
                layoutParams.isCached=true;
            } else {
                view= newAdapterView(SimpleListView.this, viewType);
            }
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            layoutParams.position=position;
            layoutParams.viewType=viewType;
            return view;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (super.onInterceptTouchEvent(ev)) {
            return true;
        }
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            releaseDrag();
            return false;
        }
        if (action != MotionEvent.ACTION_DOWN&&isBeingDragged) {
            return true;
        }
        if(MotionEvent.ACTION_DOWN==action) {
            viewFlinger.abortAnimation();
            lastMotionX = ev.getX();
            lastMotionY = ev.getY();
        } else if(MotionEvent.ACTION_MOVE==action){
            float x = ev.getX();
            float y = ev.getY();
            float dx = x - lastMotionX;
            float dy = y - lastMotionY;
            if (Math.abs(dx) > touchSlop||Math.abs(dy) > touchSlop) {
                isBeingDragged = true;
                ViewParent parent = getParent();
                if(null!=parent){
                    parent.requestDisallowInterceptTouchEvent(true);
                }
            }
        } else if(MotionEvent.ACTION_UP==action||MotionEvent.ACTION_CANCEL==action) {
            releaseDrag();
        }
        return isBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);
        int action = ev.getActionMasked();
        if(MotionEvent.ACTION_DOWN==action){
            lastMotionX = ev.getX();
            lastMotionY = ev.getY();
            viewFlinger.abortAnimation();
            invalidate();
            ViewParent parent = getParent();
            if(null!=parent){
                parent.requestDisallowInterceptTouchEvent(true);
            }
        } else if(MotionEvent.ACTION_MOVE==action){
            float x = ev.getX();
            float y = ev.getY();
            float dx = x - lastMotionX;
            float dy = y - lastMotionY;
            if (!isBeingDragged&&(Math.abs(dx) > touchSlop||Math.abs(dy) > touchSlop)) {
                isBeingDragged = true;
                lastMotionX = x;
                lastMotionY = y;
                ViewParent parent = getParent();
                if(null!=parent){
                    parent.requestDisallowInterceptTouchEvent(true);
                }
            }
            if (isBeingDragged) {
                lastMotionX = x;
                lastMotionY = y;
                scrollBy(-Math.round(dx),-Math.round(dy));
                invalidate();
            }
        } else if(MotionEvent.ACTION_UP==action){
            if(null!=velocityTracker){
                velocityTracker.computeCurrentVelocity(1000,maximumVelocity);
                float xVelocity = velocityTracker.getXVelocity();
                float yVelocity = velocityTracker.getYVelocity();
                if(Math.abs(xVelocity)>minimumVelocity||Math.abs(yVelocity)>minimumVelocity){
                    viewFlinger.fling(-xVelocity,-yVelocity);
                }
            }
            releaseDrag();
        } else if(MotionEvent.ACTION_CANCEL==action){
            releaseDrag();
        }
        return true;
    }

    /**
     * Release the drag.
     */
    private void releaseDrag() {
        lastMotionX=0f;
        lastMotionY=0f;
        isBeingDragged = false;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }


    public class ViewFlinger implements Runnable{
        private final OverScroller overScroller;
        private int lastFlingX = 0;
        private int lastFlingY = 0;

        public ViewFlinger(Context context) {
            overScroller=new OverScroller(context);
        }

        @Override
        public void run() {
            if(!overScroller.isFinished()&&overScroller.computeScrollOffset()){
                int currX = overScroller.getCurrX();
                int currY = overScroller.getCurrY();
                int dx = currX - lastFlingX;
                int dy = currY - lastFlingY;
                lastFlingX = currX;
                lastFlingY = currY;
//                // We are done scrolling if scroller is finished, or for both the x and y dimension,
//                // we are done scrolling or we can't scroll further (we know we can't scroll further
//                // when we have unconsumed scroll distance).  It's possible that we don't need
//                // to also check for scroller.isFinished() at all, but no harm in doing so in case
//                // of old bugs in OverScroller.
//                boolean scrollerFinishedX = overScroller.getCurrX() == overScroller.getFinalX();
//                boolean scrollerFinishedY = overScroller.getCurrY() == overScroller.getFinalY();
//                final boolean doneScrolling = overScroller.isFinished()
//                        || ((scrollerFinishedX || dx != 0) && (scrollerFinishedY || dy != 0));
                scrollBy(dx,dy);
                invalidate();
                postOnAnimation();
            }
        }

        void startScroll(int startX,int startY,int dx,int dy) {
            lastFlingX = startX;
            lastFlingY = startY;
            overScroller.startScroll(startX, startY, dx, dy);
            if (Build.VERSION.SDK_INT < 23) {
                // b/64931938 before API 23, startScroll() does not reset getCurX()/getCurY()
                // to start values, which causes fillRemainingScrollValues() put in obsolete values
                // for LayoutManager.onLayoutChildren().
                overScroller.computeScrollOffset();
            }
            postOnAnimation();
        }

        /**
         * abort the animation
         */
        void abortAnimation(){
            if(!overScroller.isFinished()){
                overScroller.abortAnimation();
                postInvalidate();
            }
        }

        void fling(float velocityX,float velocityY) {
            lastFlingX = lastFlingY = 0;
            overScroller.fling(0, 0, (int)velocityX, (int)velocityY,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            postOnAnimation();
        }

        void postOnAnimation() {
            removeCallbacks(this);
            ViewCompat.postOnAnimation(SimpleListView.this, this);
        }
    }


    public class LayoutState{
        int itemDirection=DIRECTION_END;
        //检测加载，最后一个控件底部位置
        int scrollingOffset =0;
        //当前屏幕可排版区域
        int available=0;
        //当前检测控件位置
        int position;
    }

    public static abstract class Adapter{
        public abstract int getItemCount();

        public int getViewType(int position){
            return 0;
        }

        public abstract View onCreateView(ViewGroup parent,int position);

        public abstract void onBindView(View view,int position);
    }

    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * Create a layout params from a giving one.
     * @param p
     * @return
     */
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        Context context = getContext();
        return new LayoutParams(context,attrs);
    }

    public static class LayoutParams extends MarginLayoutParams {
        int position;
        int viewType;
        boolean isCached=false;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super((ViewGroup.LayoutParams) source);
        }
    }
}
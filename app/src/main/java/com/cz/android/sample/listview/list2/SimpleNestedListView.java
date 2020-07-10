package com.cz.android.sample.listview.list2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EdgeEffect;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.cz.simple.nested.SimpleNestedScrollingChild;
import com.cz.simple.nested.SimpleNestedScrollingChildHelper;

import java.util.ArrayList;
import java.util.LinkedList;

public class SimpleNestedListView extends ViewGroup implements SimpleNestedScrollingChild {
    private static final String TAG="SimpleRecyclerView";
    private static final int NO_POSITION=-1;
    private static final int DIRECTION_START=-1;
    private static final int DIRECTION_END=1;

    public static final int HORIZONTAL= LinearLayout.HORIZONTAL;
    public static final int VERTICAL= LinearLayout.VERTICAL;
    private final Rect tempRect = new Rect();

    private final ArrayList<ItemDecoration> itemDecorations = new ArrayList<>();
    private final SimpleNestedScrollingChildHelper nestedScrollingChildHelper;
    private final RecyclerBin recyclerBin=new RecyclerBin();
    private final LayoutState layoutState=new LayoutState();
    private final OrientationHelper.DynamicOrientationHelper orientationHelper=OrientationHelper.createDynamicHelper(this);;
    private Adapter adapter;
    private boolean dataStructureChange =false;

    private final ViewFlinger viewFlinger;
    private boolean isBeingDragged = false;
    private int touchSlop;
    private float lastMotionX = 0f;
    private float lastMotionY = 0f;

    private VelocityTracker velocityTracker = null;
    private int minimumVelocity;
    private int maximumVelocity;

    /**
     * Tmporarily calculate the scrolling distance. For NestedScrolling to know how far we moved.
     * @see #startCalculateScrolling()
     * @see #stopCalculateScrolling()
     */
    private int startCalculateScrolling=0;
    private int layoutScrollX=0;
    private int layoutScrollY=0;
    /**
     * Used during scrolling to retrieve the new offset within the window.
     */
    private final int[] scrollOffset = new int[2];
    private final int[] scrollConsumed = new int[2];
    private int nestedXOffset;
    private int nestedYOffset;

    private EdgeEffect leftGlow, topGlow, rightGlow, bottomGlow;

    public SimpleNestedListView(Context context) {
        this(context,null,0);
    }

    public SimpleNestedListView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SimpleNestedListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        nestedScrollingChildHelper=new SimpleNestedScrollingChildHelper(this);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        touchSlop = configuration.getScaledTouchSlop();
        minimumVelocity = configuration.getScaledMinimumFlingVelocity();
        maximumVelocity = configuration.getScaledMaximumFlingVelocity();
        viewFlinger=new ViewFlinger(context);

        setNestedScrollingEnabled(true);
    }

    public void setOrientation(int orientation){
        orientationHelper.setOrientation(orientation);
        dataStructureChange=true;
        initializeLayoutState();
        requestLayout();
    }

    public int getOrientation(){
        return orientationHelper.getOrientation();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        recyclerBin.setMeasureSpecs(widthMeasureSpec,heightMeasureSpec);

        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (leftGlow != null) leftGlow.setSize(heightSize, widthSize);
        if (topGlow != null) topGlow.setSize(widthSize, heightSize);
        if (rightGlow != null) rightGlow.setSize(heightSize, widthSize);
        if (bottomGlow != null) bottomGlow.setSize(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        if(dataStructureChange&&null!=adapter){
            dataStructureChange=false;
            initializeLayoutState();
            fillAndRecyclerLayout();
        }
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    private void initializeLayoutState() {
        int measurement = orientationHelper.getMeasurementAfterPadding();
        layoutState.available = measurement;
        layoutState.itemDirection=DIRECTION_END;
        layoutState.position=0;
        layoutState.layoutOffset = 0;
        layoutState.scrollingOffset = Integer.MIN_VALUE;
    }

    private int updateLayoutState(int requiredSpace,int itemDirection) {
        int scrollingOffset=0;
        int childCount = getChildCount();
        if(0 == requiredSpace||0 == childCount) return scrollingOffset;
        if(itemDirection == DIRECTION_START){
            View childView = getChildAt(0);
            LayoutParams layoutParams = (LayoutParams) childView.getLayoutParams();
            layoutState.position=layoutParams.position+itemDirection;
            layoutState.itemDirection=itemDirection;
            int childStart = orientationHelper.getDecoratedStart(childView);
            layoutState.layoutOffset =childStart;
            scrollingOffset=-childStart+orientationHelper.getStartAfterPadding();
        } else if(itemDirection == DIRECTION_END){
            View childView = getChildAt(childCount - 1);
            int endAfterPadding = orientationHelper.getEndAfterPadding();
            LayoutParams layoutParams = (LayoutParams) childView.getLayoutParams();
            layoutState.position=layoutParams.position+itemDirection;
            layoutState.itemDirection=itemDirection;
            int childEnd = orientationHelper.getDecoratedEnd(childView);
            layoutState.layoutOffset = childEnd;
            scrollingOffset=childEnd-endAfterPadding;
        }
        layoutState.available=requiredSpace-scrollingOffset;
        layoutState.scrollingOffset = scrollingOffset;
        Log.i(TAG,"mAvailable:"+layoutState.available+" mScrollingOffset:"+layoutState.scrollingOffset+" requiredSpace:"+requiredSpace);
        return scrollingOffset;
    }

    private void fillAndRecyclerLayout() {
        int available = layoutState.available;
        if(0 > layoutState.available){
            layoutState.scrollingOffset +=layoutState.available;
        }
        recyclerList(layoutState);
        Log.i(TAG,"fillAndRecyclerLayout:"+layoutState.available+" mScrollingOffset:"+layoutState.scrollingOffset);
        while(0 < available&&hasMore()){
            int position = layoutState.position;
            int viewType = adapter.getViewType(position);
            View view = recyclerBin.getView(position,viewType);
            adapter.onBindView(view,position);
            recyclerBin.measureChild(view);
            int consumed = layoutChildView(layoutState, view);
            if (layoutState.itemDirection == DIRECTION_START) {
                layoutState.layoutOffset -=consumed;
                addAdapterView(view,0);
            } else {
                layoutState.layoutOffset +=consumed;
                addAdapterView(view,-1);
            }
            available-=consumed;
            layoutState.position+=layoutState.itemDirection;
            layoutState.available-=consumed;
            //Check if we need to recycler view that out of the screen.
            layoutState.scrollingOffset +=consumed;
            if(0 > layoutState.available){
                layoutState.scrollingOffset +=layoutState.available;
            }
            Log.i(TAG,"fill:"+layoutState.available+" mScrollingOffset:"+layoutState.scrollingOffset);
            recyclerList(layoutState);
        }
    }

    /**
     * Layout the child view.
     * @param view
     * @param layoutState
     * @param view
     * @return
     */
    protected int layoutChildView(LayoutState layoutState,View view){
        int left=getPaddingLeft();
        int top;
        int right;
        int bottom;
        int consumed = orientationHelper.getDecorateMeasurement(view);
        if (orientationHelper.isVertical()) {
            right = left + orientationHelper.getDecorateMeasurementInOther(view);
            if (layoutState.itemDirection == DIRECTION_START) {
                bottom = layoutState.layoutOffset;
                top = layoutState.layoutOffset - consumed;
            } else {
                top = layoutState.layoutOffset;
                bottom = layoutState.layoutOffset + consumed;
            }
        } else {
            top = getPaddingTop();
            bottom = top + orientationHelper.getDecorateMeasurementInOther(view);
            if (layoutState.itemDirection == DIRECTION_START) {
                right = layoutState.layoutOffset;
                left = layoutState.layoutOffset - consumed;
            } else {
                left = layoutState.layoutOffset;
                right = layoutState.layoutOffset + consumed;
            }
        }
        layoutDecorated(view,left,top,right,bottom);
        return consumed;
    }

    private void recyclerList(LayoutState layoutState) {
        if(layoutState.itemDirection==DIRECTION_END){
            recycleFromStart(layoutState.scrollingOffset);
        } else if(layoutState.itemDirection==DIRECTION_START){
            recycleFromEnd(layoutState.scrollingOffset);
        }
    }

    private void recycleFromStart(int dt) {
        int childCount = getChildCount();
        if(0 < childCount){
            int limit=orientationHelper.getStartAfterPadding()+dt;
            Log.i(TAG,"recycleFromStart:"+" dt:"+dt+" limit:"+limit);
            for(int i=0;i<childCount;i++){
                View childView = getChildAt(i);
                int childEnd = orientationHelper.getDecoratedEnd(childView);
                if(childEnd > limit){
                    Log.i(TAG,"recycleFromStart:"+" dt:"+dt+" limit:"+limit+" i:"+i+" childEnd:"+childEnd+" childCount:"+childCount);
                    recyclerChild(0,i);
                    break;
                }
            }
        }
    }

    private void recycleFromEnd(int dt) {
        int childCount = getChildCount();
        if(0 < childCount){
            final int limit = orientationHelper.getEndAfterPadding() - dt;
            Log.i(TAG,"recycleFromEnd:"+dt+" limit:"+limit);
            for(int i=childCount-1;i>=0;i--){
                View childView = getChildAt(i);
                int childStart = orientationHelper.getDecoratedStart(childView);
                if(childStart < limit){
                    recyclerChild(childCount - 1, i);
                    Log.i(TAG,"recycleFromEnd:"+childStart+" limit:"+limit+" i:"+(i)+" childCount:"+childCount);
                    break;
                }
            }
        }
    }

    private void recyclerChild(int start, int end) {
        if (start == end) {
            return;
        }
        if (end > start) {
            for (int i = end - 1; i >= start; i--) {
                removeAndRecycleViewAt(i);
            }
        } else {
            for (int i = start; i > end; i--) {
                removeAndRecycleViewAt(i);
            }
        }
    }

    private void removeAndRecycleViewAt(int index) {
        final View view = getChildAt(index);
        recyclerBin.recycleView(view);
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
        return orientationHelper.isHorizontal();
    }

    public boolean canScrollVertically() {
        return orientationHelper.isVertical();
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
        int consumedX = scrollHorizontallyInternal(dx);
        int overScrollX = dx - consumedX;

        int consumedY = scrollVerticallyInternal(dy);
        int overScrollY = dy - consumedY;

        pullGlows(overScrollX, overScrollY);
    }

    private int scrollHorizontallyInternal(int dx) {
        if(0 == dx){
            return 0;
        }
        orientationHelper.setOrientation(OrientationHelper.HORIZONTAL);
        int consumedX=0;
        if (dx != 0) {
            consumedX = scrollHorizontallyBy(dx);
        }
        if(0 != consumedX){
            if(0 < startCalculateScrolling){
                layoutScrollX+=consumedX;
            }
            offsetChildrenLeftAndRight(-consumedX);
        }
        return consumedX;
    }

    private int scrollVerticallyInternal(int dy) {
        if(0 == dy){
            return 0;
        }
        orientationHelper.setOrientation(OrientationHelper.VERTICAL);
        int consumedY=0;
        if (dy != 0) {
            consumedY = scrollVerticallyBy(dy);
        }
        if(0 != consumedY){
            if(0 < startCalculateScrolling){
                layoutScrollY+=consumedY;
            }
            offsetChildrenTopAndBottom(-consumedY);
        }
        return consumedY;
    }

    private void offsetChildrenLeftAndRight(int dx) {
        int childCount = getChildCount();
        for(int i=0;i<childCount;i++){
            View childView = getChildAt(i);
            if(0!=dx){
                childView.offsetLeftAndRight(dx);
            }
        }
    }

    private void offsetChildrenTopAndBottom(int dy) {
        int childCount = getChildCount();
        for(int i=0;i<childCount;i++){
            View childView = getChildAt(i);
            if(0!=dy){
                childView.offsetTopAndBottom(dy);
            }
        }
    }

    private int scrollHorizontallyBy(int dx) {
        int absDx = Math.abs(dx);
        int layoutDirection=(0 < dx) ? DIRECTION_END : DIRECTION_START;
        int scrollingOffset = updateLayoutState(absDx,layoutDirection);
        fillAndRecyclerLayout();
        int consumed = absDx > scrollingOffset ? overScroll(layoutDirection,scrollingOffset,dx) : dx;
        return consumed;
    }

    private int scrollVerticallyBy(int dy) {
        int absDy = Math.abs(dy);
        int layoutDirection=(0 < dy) ? DIRECTION_END : DIRECTION_START;
        int scrollingOffset = updateLayoutState(absDy,layoutDirection);
        fillAndRecyclerLayout();
        int consumed = absDy > scrollingOffset ? overScroll(layoutDirection,scrollingOffset,dy) : dy;
        return consumed;
    }

    public int overScroll(int layoutDirection,int consumed,int delta){
        int absDiff=Math.abs(delta);
        int overMaxDistance=0;
        if(DIRECTION_START==layoutDirection){
            View view=getChildAt(0);
            int start=orientationHelper.getDecoratedStart(view);
            if((overMaxDistance-start)<absDiff){
                delta=start-overMaxDistance;
            }
        } else if(DIRECTION_END==layoutDirection){
            int childCount = getChildCount();
            View view=getChildAt(childCount-1);
            int end = orientationHelper.getEndAfterPadding();
            int childEnd=orientationHelper.getDecoratedEnd(view);
            overMaxDistance=end-overMaxDistance;
            if((childEnd-overMaxDistance)<absDiff){
                delta=childEnd-overMaxDistance;
            }
        }
        return delta;
    }

    public void setAdapter(Adapter adapter){
        this.adapter=adapter;
        this.dataStructureChange =true;
        this.recyclerBin.clear();
        removeAllViews();
        initializeLayoutState();
        requestLayout();
    }

    public Adapter getAdapter() {
        return adapter;
    }

    protected void addAdapterView(View childView, int index){
        SimpleNestedListView.LayoutParams layoutParams = (SimpleNestedListView.LayoutParams) childView.getLayoutParams();
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

        void recycleView(View view){
            removeAdapterView(view);
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            layoutParams.insetsDirty=true;
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
                recycleView(childView);
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

        void measureChild(View child){
            int parentWidthMeasureSpec= MeasureSpec.makeMeasureSpec(width, widthMode);
            int parentHeightMeasureSpec= MeasureSpec.makeMeasureSpec(height, heightMode);
            ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
            final Rect insets = getItemDecorInsetsForChild(child);
            int widthUsed = insets.left + insets.right;
            int heightUsed = insets.top + insets.bottom;
            //Create a new measure spec for the child view.
            int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, widthUsed, layoutParams.width);
            int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec, heightUsed, layoutParams.height);
            child.measure(childWidthMeasureSpec,childHeightMeasureSpec);
        }

        View getView(int position,int viewType){
            View view;
            LinkedList<View> cachedList = scrapViews.get(viewType);
            if(null!=cachedList&&!cachedList.isEmpty()){
                view=cachedList.pollFirst();
                LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
                layoutParams.isCached=true;
            } else {
                view= newAdapterView(SimpleNestedListView.this, viewType);
            }
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            layoutParams.insetsDirty=true;
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
            startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL,ViewCompat.TYPE_TOUCH);
        } else if(MotionEvent.ACTION_MOVE==action){
            float x = ev.getX();
            float y = ev.getY();
            float dx = x - lastMotionX;
            float dy = y - lastMotionY;
            if (canScrollHorizontally()&&Math.abs(dx)> touchSlop||canScrollVertically()&&Math.abs(dy) > touchSlop) {
                nestedXOffset = 0;
                nestedYOffset = 0;
                isBeingDragged = true;
                ViewParent parent = getParent();
                if(null!=parent){
                    parent.requestDisallowInterceptTouchEvent(true);
                }
            }
        } else if(MotionEvent.ACTION_UP==action||MotionEvent.ACTION_CANCEL==action) {
            stopNestedScroll(ViewCompat.TYPE_TOUCH);
            releaseDrag();
        }
        return isBeingDragged;
    }

    public int getLayoutScrollX() {
        return layoutScrollX;
    }

    public int getLayoutScrollY() {
        return layoutScrollY;
    }

    public void startCalculateScrolling() {
        if(0==startCalculateScrolling){
            layoutScrollX=0;
            layoutScrollY=0;
        }
        startCalculateScrolling++;
    }

    public void stopCalculateScrolling() {
        startCalculateScrolling--;
        if(0==startCalculateScrolling){
            layoutScrollX=0;
            layoutScrollY=0;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            nestedXOffset = 0;
            nestedYOffset = 0;
        }
        MotionEvent vtev = MotionEvent.obtain(ev);
        vtev.offsetLocation(canScrollHorizontally()?nestedXOffset:0, canScrollVertically()?nestedYOffset:0);
        if(MotionEvent.ACTION_DOWN==action){
            nestedYOffset = 0;
            lastMotionX = ev.getX();
            lastMotionY = ev.getY();
            viewFlinger.abortAnimation();
            ViewParent parent = getParent();
            if(null!=parent){
                parent.requestDisallowInterceptTouchEvent(true);
            }
        } else if(MotionEvent.ACTION_MOVE==action){
            float x = ev.getX();
            float y = ev.getY();
            int dx = (int) (lastMotionX - x + 0.5f);
            int dy = (int) (lastMotionY - y + 0.5f);

            if(dispatchNestedPreScroll(dx,dy, scrollConsumed, scrollOffset,ViewCompat.TYPE_TOUCH)){
                dx-= scrollConsumed[0];
                dy-= scrollConsumed[1];
                nestedXOffset += scrollOffset[0];
                nestedYOffset += scrollOffset[1];
            }

            if (!isBeingDragged&&(canScrollHorizontally()&&Math.abs(dx) > touchSlop||
                    canScrollVertically()&&Math.abs(dy) > touchSlop)) {
                isBeingDragged = true;
                lastMotionX = x;
                lastMotionY = y;
                if(canScrollHorizontally()){
                    if (dx > 0) {
                        dx -= touchSlop;
                    } else {
                        dx += touchSlop;
                    }
                } else if(canScrollVertically()){
                    if (dy > 0) {
                        dy -= touchSlop;
                    } else {
                        dy += touchSlop;
                    }
                }
                ViewParent parent = getParent();
                if(null!=parent){
                    parent.requestDisallowInterceptTouchEvent(true);
                }
            }
            if (isBeingDragged) {
                startCalculateScrolling();
                lastMotionX = x - scrollOffset[0];
                lastMotionY = y - scrollOffset[1];
                int oldX = layoutScrollX;
                int oldY = layoutScrollY;
                scrollBy(dx,dy);
                invalidate();

                final int scrolledDeltaX = layoutScrollX - oldX;
                final int scrolledDeltaY = layoutScrollY - oldY;
                final int unconsumedX = dx - scrolledDeltaX;
                final int unconsumedY = dy - scrolledDeltaY;
                scrollConsumed[0] = 0;
                scrollConsumed[1] = 0;
                dispatchNestedScroll(scrolledDeltaX, scrolledDeltaY, unconsumedX, unconsumedY, scrollOffset,
                        ViewCompat.TYPE_TOUCH, scrollConsumed);
                nestedXOffset += scrollOffset[0];
                nestedYOffset += scrollOffset[1];
                stopCalculateScrolling();
            }
        } else if(MotionEvent.ACTION_UP==action){
            if(null!=velocityTracker){
                velocityTracker.computeCurrentVelocity(1000,maximumVelocity);
                float xVelocity = !canScrollHorizontally()?0:velocityTracker.getXVelocity();
                float yVelocity = !canScrollVertically()?0:velocityTracker.getYVelocity();
                if(Math.abs(xVelocity)>minimumVelocity|| Math.abs(yVelocity)>minimumVelocity){
                    if (!dispatchNestedPreFling(-xVelocity, -yVelocity)) {
                        dispatchNestedFling(-xVelocity, -yVelocity, true);
                        viewFlinger.fling(-xVelocity,-yVelocity);
                    }
                }
            }
            stopNestedScroll(ViewCompat.TYPE_TOUCH);
            releaseDrag();
        } else if(MotionEvent.ACTION_CANCEL==action){
            stopNestedScroll(ViewCompat.TYPE_TOUCH);
            releaseDrag();
        }
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(vtev);
        vtev.recycle();
        return true;
    }

    /**
     * Release the drag.
     */
    private void releaseDrag() {
        lastMotionX=0f;
        lastMotionY=0f;
        isBeingDragged = false;
        releaseGlows();
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }


    public class ViewFlinger implements Runnable{
        private final OverScroller overScroller;

        public ViewFlinger(Context context) {
            overScroller=new OverScroller(context);
        }

        @Override
        public void run() {
            if (overScroller.isFinished()) {
                return;
            }
            overScroller.computeScrollOffset();
            int overScrollX = 0;
            int overScrollY = 0;
            int currX = overScroller.getCurrX();
            int currY = overScroller.getCurrY();
            int dxUnconsumed = (int) (currX-lastMotionX);
            int dyUnconsumed = (int) (currY-lastMotionY);
            lastMotionX = currX;
            lastMotionY = currY;
            // Nested Scrolling Pre Pass
            scrollConsumed[0] = 0;
            scrollConsumed[1] = 0;
            dispatchNestedPreScroll(dxUnconsumed, dyUnconsumed, scrollConsumed, null, ViewCompat.TYPE_NON_TOUCH);
            dxUnconsumed -= scrollConsumed[0];
            dyUnconsumed -= scrollConsumed[1];
            if (0 != dxUnconsumed || 0 != dyUnconsumed) {
                // Internal Scroll
                int dxConsumed=0;
                int dyConsumed=0;
                if(orientationHelper.isHorizontal()){
                    dxConsumed = scrollHorizontallyInternal(dxUnconsumed);
                    overScrollX = dxUnconsumed - dxConsumed;
                    dxUnconsumed -= dxConsumed;
                } else if(orientationHelper.isVertical()){
                    dyConsumed = scrollVerticallyInternal(dyUnconsumed);
                    overScrollY = dyUnconsumed - dyConsumed;
                    dyUnconsumed -= dyConsumed;
                }
                // Nested Scrolling Post Pass
                scrollConsumed[0] = 0;
                dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, scrollOffset, ViewCompat.TYPE_NON_TOUCH, scrollConsumed);
                dyUnconsumed -= scrollConsumed[0];
                dyUnconsumed -= scrollConsumed[0];
                invalidate();
            }
            if (0 != dxUnconsumed || 0 != dyUnconsumed) {
                overScroller.abortAnimation();
                stopNestedScroll(ViewCompat.TYPE_NON_TOUCH);
            }
            if (overScrollX != 0 || overScrollY != 0) {
                final int vel = (int) overScroller.getCurrVelocity();
                int velX = 0;
                if (overScrollX != currX) {
                    velX = overScrollX < 0 ? -vel : overScrollX > 0 ? vel : 0;
                }
                int velY = 0;
                if (overScrollY != currY) {
                    velY = overScrollY < 0 ? -vel : overScrollY > 0 ? vel : 0;
                }
                absorbGlows(velX, velY);
            }
            postOnAnimation();
        }

        void startScroll(int startX,int startY,int dx,int dy) {
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
            overScroller.fling(0, 0, (int)velocityX, (int)velocityY,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            runAnimatedScroll(true);
        }

        private void runAnimatedScroll(boolean participateInNestedScrolling) {
            if (participateInNestedScrolling) {
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH);
            } else {
                stopNestedScroll(ViewCompat.TYPE_NON_TOUCH);
            }
            overScroller.computeScrollOffset();
            lastMotionX = overScroller.getCurrX();
            lastMotionY = overScroller.getCurrY();
            postOnAnimation();
        }

        void postOnAnimation() {
            removeCallbacks(this);
            ViewCompat.postOnAnimation(SimpleNestedListView.this, this);
        }
    }


    public class LayoutState{
        int itemDirection=DIRECTION_END;
        int scrollingOffset =0;
        int layoutOffset =0;
        int available=0;
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
        Rect decorInsets=new Rect();
        boolean insetsDirty=true;

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

    //------------------------------------------------------------------
    //All about scroll.
    //------------------------------------------------------------------
    @Nullable
    View findOneVisibleChild(int fromIndex,int toIndex) {
        int next=toIndex > fromIndex?1:-1;
        int startPadding = orientationHelper.getStartPadding();
        int i = fromIndex;
        while (i != toIndex) {
            View child = getChildAt(i);
            int childStart = orientationHelper.getDecoratedStart(child);
            int childEnd = orientationHelper.getDecoratedEnd(child);
            if (childStart<=startPadding&&startPadding<=childEnd) {
                return child;
            }
            i += next;
        }
        return null;
    }

    public View findViewByPosition(int position) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            int childPosition = getPosition(child);
            if (childPosition==position) {
                return child;
            }
        }
        return null;
    }

    public int findFirstVisibleItemPosition() {
        int childCount = getChildCount();
        View child = findOneVisibleChild(0, childCount);
        return child == null ? NO_POSITION : getPosition(child);
    }

    public int findLastVisibleItemPosition() {
        int childCount = getChildCount();
        View child = findOneVisibleChild(childCount - 1, -1);
        return child == null ? NO_POSITION : getPosition(child);
    }

    @Override
    protected int computeHorizontalScrollRange() {
        return computeScrollRange();
    }

    @Override
    protected int computeHorizontalScrollOffset() {
        return computeScrollOffset();
    }

    @Override
    protected int computeHorizontalScrollExtent() {
        return computeScrollExtent();
    }

    @Override
    protected int computeVerticalScrollRange() {
        return computeScrollRange();
    }

    @Override
    protected int computeVerticalScrollOffset() {
        return computeScrollOffset();
    }

    @Override
    protected int computeVerticalScrollExtent() {
        return computeScrollExtent();
    }

    private int computeScrollOffset() {
        if (getChildCount() == 0) {
            return 0;
        }
        int firstVisibleItemPosition = findFirstVisibleItemPosition();
        int lastVisibleItemPosition = findLastVisibleItemPosition();
        View firstVisibleView = findViewByPosition(firstVisibleItemPosition);
        View lastVisibleView = findViewByPosition(lastVisibleItemPosition);
        return SimpleScrollbarHelper.computeScrollOffset(this,orientationHelper,firstVisibleView,lastVisibleView);
    }

    private int computeScrollExtent() {
        if (getChildCount() == 0) {
            return 0;
        }
        int firstVisibleItemPosition = findFirstVisibleItemPosition();
        int lastVisibleItemPosition = findLastVisibleItemPosition();
        View firstVisibleView = findViewByPosition(firstVisibleItemPosition);
        View lastVisibleView = findViewByPosition(lastVisibleItemPosition);
        return SimpleScrollbarHelper.computeScrollExtent(this,orientationHelper,firstVisibleView,lastVisibleView);
    }

    private int computeScrollRange() {
        if (getChildCount() == 0) {
            return 0;
        }
        int firstVisibleItemPosition = findFirstVisibleItemPosition();
        int lastVisibleItemPosition = findLastVisibleItemPosition();
        View firstVisibleView = findViewByPosition(firstVisibleItemPosition);
        View lastVisibleView = findViewByPosition(lastVisibleItemPosition);
        return SimpleScrollbarHelper.computeScrollRange(this,orientationHelper,firstVisibleView,lastVisibleView);
    }

    //------------------------------------------------------------------
    //All about save instance.
    //------------------------------------------------------------------

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    //------------------------------------------------------------------
    //All about edge effect.
    //------------------------------------------------------------------
    /**
     * Apply a pull to relevant overscroll glow effects
     */
    private void pullGlows(int overScrollX, int overScrollY) {
        if (overScrollX < 0) {
            if (leftGlow == null) {
                leftGlow = new EdgeEffect(getContext());
                leftGlow.setSize(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(),
                        getMeasuredWidth() - getPaddingLeft() - getPaddingRight());
            }
            leftGlow.onPull(-overScrollX / (float) getWidth());
        } else if (overScrollX > 0) {
            if (rightGlow == null) {
                rightGlow = new EdgeEffect(getContext());
                rightGlow.setSize(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(),
                        getMeasuredWidth() - getPaddingLeft() - getPaddingRight());
            }
            rightGlow.onPull(overScrollX / (float) getWidth());
        }

        if (overScrollY < 0) {
            if (topGlow == null) {
                topGlow = new EdgeEffect(getContext());
                topGlow.setSize(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                        getMeasuredHeight() - getPaddingTop() - getPaddingBottom());
            }
            topGlow.onPull(-overScrollY / (float) getHeight());
        } else if (overScrollY > 0) {
            if (bottomGlow == null) {
                bottomGlow = new EdgeEffect(getContext());
                bottomGlow.setSize(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                        getMeasuredHeight() - getPaddingTop() - getPaddingBottom());
            }
            bottomGlow.onPull(overScrollY / (float) getHeight());
        }

        if (overScrollX != 0 || overScrollY != 0) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void releaseGlows() {
        if (leftGlow != null) leftGlow.onRelease();
        if (topGlow != null) topGlow.onRelease();
        if (rightGlow != null) rightGlow.onRelease();
        if (bottomGlow != null) bottomGlow.onRelease();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    void absorbGlows(int velocityX, int velocityY) {
        if (velocityX < 0) {
            if (leftGlow == null) {
                leftGlow = new EdgeEffect(getContext());
                leftGlow.setSize(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(),
                        getMeasuredWidth() - getPaddingLeft() - getPaddingRight());
            }
            leftGlow.onAbsorb(-velocityX);
        } else if (velocityX > 0) {
            if (rightGlow == null) {
                rightGlow = new EdgeEffect(getContext());
                rightGlow.setSize(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(),
                        getMeasuredWidth() - getPaddingLeft() - getPaddingRight());
            }
            rightGlow.onAbsorb(velocityX);
        }

        if (velocityY < 0) {
            if (topGlow == null) {
                topGlow = new EdgeEffect(getContext());
                topGlow.setSize(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                        getMeasuredHeight() - getPaddingTop() - getPaddingBottom());
            }
            topGlow.onAbsorb(-velocityY);
        } else if (velocityY > 0) {
            if (bottomGlow == null) {
                bottomGlow = new EdgeEffect(getContext());
                bottomGlow.setSize(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                        getMeasuredHeight() - getPaddingTop() - getPaddingBottom());
            }
            bottomGlow.onAbsorb(velocityY);
        }

        if (velocityX != 0 || velocityY != 0) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public void draw(Canvas c) {
        super.draw(c);
        final int count = itemDecorations.size();
        for (int i = 0; i < count; i++) {
            itemDecorations.get(i).onDrawOver(c, this);
        }
        boolean needsInvalidate = false;
        if (leftGlow != null && !leftGlow.isFinished()) {
            final int restore = c.save();
            c.rotate(270);
            c.translate(-getHeight() + getPaddingTop(), 0);
            needsInvalidate = leftGlow != null && leftGlow.draw(c);
            c.restoreToCount(restore);
        }
        if (topGlow != null && !topGlow.isFinished()) {
            c.translate(getPaddingLeft(), getPaddingTop());
            needsInvalidate |= topGlow != null && topGlow.draw(c);
        }
        if (rightGlow != null && !rightGlow.isFinished()) {
            final int restore = c.save();
            final int width = getWidth();

            c.rotate(90);
            c.translate(-getPaddingTop(), -width);
            needsInvalidate |= rightGlow != null && rightGlow.draw(c);
            c.restoreToCount(restore);
        }
        if (bottomGlow != null && !bottomGlow.isFinished()) {
            final int restore = c.save();
            c.rotate(180);
            c.translate(-getWidth() + getPaddingLeft(), -getHeight() + getPaddingTop());
            needsInvalidate |= bottomGlow != null && bottomGlow.draw(c);
            c.restoreToCount(restore);
        }
        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    //------------------------------------------------------------------
    //All about nested scroll.
    //------------------------------------------------------------------

    void markItemDecorInsetsDirty() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            ((LayoutParams) child.getLayoutParams()).insetsDirty = true;
        }
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        nestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return nestedScrollingChildHelper.hasNestedScrollingParent(type);
    }

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return nestedScrollingChildHelper.startNestedScroll(axes,type);
    }

    @Override
    public void stopNestedScroll(int type) {
        nestedScrollingChildHelper.stopNestedScroll(type);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow, int type) {
        return nestedScrollingChildHelper.dispatchNestedPreScroll(dx,dy,consumed,offsetInWindow,type);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow, int type, int[] consumed) {
        return nestedScrollingChildHelper.dispatchNestedScroll(dxConsumed,dyConsumed,dxUnconsumed,dyUnconsumed,offsetInWindow,type,consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return nestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return nestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }


    //------------------------------------------------------------------
    //All about decorate the child view.
    //------------------------------------------------------------------
    /**
     * Add an {@link ItemDecoration} to this RecyclerView. Item decorations can
     * affect both measurement and drawing of individual item views.
     *
     * <p>Item decorations are ordered. Decorations placed earlier in the list will
     * be run/queried/drawn first for their effects on item views. Padding added to views
     * will be nested; a padding added by an earlier decoration will mean further
     * item decorations in the list will be asked to draw/pad within the previous decoration's
     * given area.</p>
     *
     * @param decor Decoration to add
     * @param index Position in the decoration chain to insert this decoration at. If this value
     *              is negative the decoration will be added at the end.
     */
    public void addItemDecoration(ItemDecoration decor, int index) {
        if (itemDecorations.isEmpty()) {
            setWillNotDraw(false);
        }
        if (index < 0) {
            itemDecorations.add(decor);
        } else {
            itemDecorations.add(index, decor);
        }
        markItemDecorInsetsDirty();
        requestLayout();
    }

    /**
     * Add an {@link ItemDecoration} to this RecyclerView. Item decorations can
     * affect both measurement and drawing of individual item views.
     *
     * <p>Item decorations are ordered. Decorations placed earlier in the list will
     * be run/queried/drawn first for their effects on item views. Padding added to views
     * will be nested; a padding added by an earlier decoration will mean further
     * item decorations in the list will be asked to draw/pad within the previous decoration's
     * given area.</p>
     *
     * @param decor Decoration to add
     */
    public void addItemDecoration(ItemDecoration decor) {
        addItemDecoration(decor, -1);
    }

    /**
     * Remove an {@link ItemDecoration} from this RecyclerView.
     *
     * <p>The given decoration will no longer impact the measurement and drawing of
     * item views.</p>
     *
     * @param decor Decoration to remove
     * @see #addItemDecoration(ItemDecoration)
     */
    public void removeItemDecoration(ItemDecoration decor) {
        itemDecorations.remove(decor);
        if (itemDecorations.isEmpty()) {
            setWillNotDraw(ViewCompat.getOverScrollMode(this) == ViewCompat.OVER_SCROLL_NEVER);
        }
        markItemDecorInsetsDirty();
        requestLayout();
    }

    /**
     * Returns the measured width of the given child, plus the additional size of
     * any insets applied by {@link ItemDecoration ItemDecorations}.
     *
     * @param child Child view to query
     * @return child's measured width plus <code>ItemDecoration</code> insets
     *
     * @see View#getMeasuredWidth()
     */
    public int getDecoratedMeasuredWidth(View child) {
        final Rect insets = ((LayoutParams) child.getLayoutParams()).decorInsets;
        return child.getMeasuredWidth() + insets.left + insets.right;
    }

    /**
     * Returns the measured height of the given child, plus the additional size of
     * any insets applied by {@link ItemDecoration ItemDecorations}.
     *
     * @param child Child view to query
     * @return child's measured height plus <code>ItemDecoration</code> insets
     *
     * @see View#getMeasuredHeight()
     */
    public int getDecoratedMeasuredHeight(View child) {
        final Rect insets = ((LayoutParams) child.getLayoutParams()).decorInsets;
        return child.getMeasuredHeight() + insets.top + insets.bottom;
    }

    public Rect getDecoratedInsets(View child) {
        return ((LayoutParams) child.getLayoutParams()).decorInsets;
    }

    /**
     * Lay out the given child view within the RecyclerView using coordinates that
     * include any current {@link ItemDecoration ItemDecorations}.
     *
     * <p>LayoutManagers should prefer working in sizes and coordinates that include
     * item decoration insets whenever possible. This allows the LayoutManager to effectively
     * ignore decoration insets within measurement and layout code. See the following
     * methods:</p>
     * <ul>
     *     <li>{@link #measureChild(View, int, int)}</li>
     *     <li>{@link #getDecoratedLeft(View)}</li>
     *     <li>{@link #getDecoratedTop(View)}</li>
     *     <li>{@link #getDecoratedRight(View)}</li>
     *     <li>{@link #getDecoratedBottom(View)}</li>
     *     <li>{@link #getDecoratedMeasuredWidth(View)}</li>
     *     <li>{@link #getDecoratedMeasuredHeight(View)}</li>
     * </ul>
     *
     * @param child Child to lay out
     * @param left Left edge, with item decoration insets included
     * @param top Top edge, with item decoration insets included
     * @param right Right edge, with item decoration insets included
     * @param bottom Bottom edge, with item decoration insets included
     *
     * @see View#layout(int, int, int, int)
     */
    public void layoutDecorated(View child, int left, int top, int right, int bottom) {
        final Rect insets = ((LayoutParams) child.getLayoutParams()).decorInsets;
        child.layout(left + insets.left, top + insets.top, right - insets.right,
                bottom - insets.bottom);
    }

    Rect getItemDecorInsetsForChild(View child) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (!lp.insetsDirty) {
            return lp.decorInsets;
        }

        final Rect insets = lp.decorInsets;
        insets.set(0, 0, 0, 0);
        final int decorCount = itemDecorations.size();
        for (int i = 0; i < decorCount; i++) {
            tempRect.set(0, 0, 0, 0);
            itemDecorations.get(i).getItemOffsets(tempRect, lp.position, this);
            insets.left += tempRect.left;
            insets.top += tempRect.top;
            insets.right += tempRect.right;
            insets.bottom += tempRect.bottom;
        }
        lp.insetsDirty = false;
        return insets;
    }

    /**
     * Returns the left edge of the given child view within its parent, offset by any applied
     * {@link ItemDecoration ItemDecorations}.
     *
     * @param child Child to query
     * @return Child left edge with offsets applied
     */
    public int getDecoratedLeft(View child) {
        final Rect insets = ((LayoutParams) child.getLayoutParams()).decorInsets;
        return child.getLeft() - insets.left;
    }

    /**
     * Returns the top edge of the given child view within its parent, offset by any applied
     * {@link ItemDecoration ItemDecorations}.
     *
     * @param child Child to query
     * @return Child top edge with offsets applied
     */
    public int getDecoratedTop(View child) {
        final Rect insets = ((LayoutParams) child.getLayoutParams()).decorInsets;
        return child.getTop() - insets.top;
    }

    /**
     * Returns the right edge of the given child view within its parent, offset by any applied
     * {@link ItemDecoration ItemDecorations}.
     *
     * @param child Child to query
     * @return Child right edge with offsets applied
     */
    public int getDecoratedRight(View child) {
        final Rect insets = ((LayoutParams) child.getLayoutParams()).decorInsets;
        return child.getRight() + insets.right;
    }

    /**
     * Returns the bottom edge of the given child view within its parent, offset by any applied
     * {@link ItemDecoration ItemDecorations}.
     *
     * @param child Child to query
     * @return Child bottom edge with offsets applied
     */
    public int getDecoratedBottom(View child) {
        final Rect insets = ((LayoutParams) child.getLayoutParams()).decorInsets;
        return child.getBottom() + insets.bottom;
    }

    /**
     * An ItemDecoration allows the application to add a special drawing and layout offset
     * to specific item views from the adapter's data set. This can be useful for drawing dividers
     * between items, highlights, visual grouping boundaries and more.
     *
     * <p>All ItemDecorations are drawn in the order they were added, before the item
     * views (in {@link ItemDecoration#onDraw(Canvas, SimpleNestedListView) onDraw()} and after the items
     * (in {@link ItemDecoration#onDrawOver(Canvas, SimpleNestedListView)}.</p>
     */
    public static abstract class ItemDecoration {
        /**
         * Draw any appropriate decorations into the Canvas supplied to the RecyclerView.
         * Any content drawn by this method will be drawn before the item views are drawn,
         * and will thus appear underneath the views.
         *
         * @param c Canvas to draw into
         * @param parent RecyclerView this ItemDecoration is drawing into
         */
        public void onDraw(@NonNull Canvas c,@NonNull SimpleNestedListView parent) {
        }

        /**
         * Draw any appropriate decorations into the Canvas supplied to the RecyclerView.
         * Any content drawn by this method will be drawn after the item views are drawn
         * and will thus appear over the views.
         *
         * @param c Canvas to draw into
         * @param parent RecyclerView this ItemDecoration is drawing into
         */
        public void onDrawOver(@NonNull Canvas c,@NonNull SimpleNestedListView parent) {
        }

        /**
         * Retrieve any offsets for the given item. Each field of <code>outRect</code> specifies
         * the number of pixels that the item view should be inset by, similar to padding or margin.
         * The default implementation sets the bounds of outRect to 0 and returns.
         *
         * <p>If this ItemDecoration does not affect the positioning of item views it should set
         * all four fields of <code>outRect</code> (left, top, right, bottom) to zero
         * before returning.</p>
         *
         * @param outRect Rect to receive the output.
         * @param itemPosition Adapter position of the item to offset
         * @param parent RecyclerView this ItemDecoration is decorating
         */
        public void getItemOffsets(@NonNull Rect outRect, int itemPosition,@NonNull SimpleNestedListView parent) {
            outRect.set(0, 0, 0, 0);
        }
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        final int count = itemDecorations.size();
        for (int i = 0; i < count; i++) {
            itemDecorations.get(i).onDraw(c, this);
        }
        int childCount = getChildCount();
        Log.i(TAG,"onDraw:"+childCount);
    }


}
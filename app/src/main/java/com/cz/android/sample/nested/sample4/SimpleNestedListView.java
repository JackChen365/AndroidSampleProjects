package com.cz.android.sample.nested.sample4;

import android.content.Context;
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
import android.widget.OverScroller;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.cz.simple.nested.SimpleNestedScrollingChild;
import com.cz.simple.nested.SimpleNestedScrollingChildHelper;

import java.util.LinkedList;

public class SimpleNestedListView extends ViewGroup implements SimpleNestedScrollingChild {
    private static final String TAG="SimpleRecyclerView";
    private static final int NO_POSITION=-1;
    private static final int DIRECTION_START=-1;
    private static final int DIRECTION_END=1;
    private final SimpleNestedScrollingChildHelper nestedScrollingChildHelper;
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

    /**
     * Used during scrolling to retrieve the new offset within the window.
     */
    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private int mNestedYOffset;

    private EdgeEffect mEdgeGlowTop;
    private EdgeEffect mEdgeGlowBottom;

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
        scrollHorizontallyInternal(dx);
        scrollVerticallyInternal(dy);
    }

    private int scrollHorizontallyInternal(int dx) {
        int consumedX=0;
        if (dx != 0) {
            consumedX = scrollHorizontallyBy(dx);
        }
        if(0 != consumedX){
            offsetChildrenLeftAndRight(-consumedX);
        }
        return consumedX;
    }

    private int scrollVerticallyInternal(int dy) {
        int consumedY=0;
        if (dy != 0) {
            consumedY = scrollVerticallyBy(dy);
        }
        if(0 != consumedY){
            offsetChildrenTopAndBottom(-consumedY);
        }
        return consumedY;
    }

    private void offsetChildrenLeftAndRight(int consumedX) {
        int childCount = getChildCount();
        for(int i=0;i<childCount;i++){
            View childView = getChildAt(i);
            if(0!=consumedX){
                childView.offsetLeftAndRight(consumedX);
            }
        }
    }

    private void offsetChildrenTopAndBottom(int consumedY) {
        int childCount = getChildCount();
        for(int i=0;i<childCount;i++){
            View childView = getChildAt(i);
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
                view= newAdapterView(SimpleNestedListView.this, viewType);
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
            startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL,ViewCompat.TYPE_TOUCH);
        } else if(MotionEvent.ACTION_MOVE==action){
            float x = ev.getX();
            float y = ev.getY();
            float dx = x - lastMotionX;
            float dy = y - lastMotionY;
            if (Math.abs(dx) > touchSlop||Math.abs(dy) > touchSlop) {
                mNestedYOffset = 0;
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
        Log.i(TAG,"onInterceptTouchEvent:"+isBeingDragged);
        return isBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            mNestedYOffset = 0;
        }
        MotionEvent vtev = MotionEvent.obtain(ev);
        vtev.offsetLocation(0, mNestedYOffset);
        if(MotionEvent.ACTION_DOWN==action){
            mNestedYOffset = 0;
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
            int dx = (int) (lastMotionX - x + 0.5f);
            int dy = (int) (lastMotionY - y + 0.5f);

            if(dispatchNestedPreScroll(dx,dy,mScrollConsumed,mScrollOffset,ViewCompat.TYPE_TOUCH)){
                dx-=mScrollConsumed[0];
                dy-=mScrollConsumed[1];
                mNestedYOffset += mScrollOffset[1];
            }

            if (!isBeingDragged&&(Math.abs(dx) > touchSlop||Math.abs(dy) > touchSlop)) {
                isBeingDragged = true;
                lastMotionX = x;
                lastMotionY = y;
                if (dy > 0) {
                    dy -= touchSlop;
                } else {
                    dy += touchSlop;
                }
                ViewParent parent = getParent();
                if(null!=parent){
                    parent.requestDisallowInterceptTouchEvent(true);
                }
            }
            if (isBeingDragged) {
                lastMotionX = x;
                lastMotionY = y - mScrollOffset[1];
                int oldY = getChildScrollY();
                scrollBy(0,dy);
                invalidate();

                final int scrolledDeltaY = getChildScrollY() - oldY;
                final int unconsumedY = dy - scrolledDeltaY;
                mScrollConsumed[1] = 0;
                dispatchNestedScroll(0, scrolledDeltaY, 0, unconsumedY, mScrollOffset,
                        ViewCompat.TYPE_TOUCH, mScrollConsumed);
                mNestedYOffset += mScrollOffset[1];
            }
        } else if(MotionEvent.ACTION_UP==action){
            if(null!=velocityTracker){
                velocityTracker.computeCurrentVelocity(1000,maximumVelocity);
                float xVelocity = velocityTracker.getXVelocity();
                float yVelocity = velocityTracker.getYVelocity();
                if(Math.abs(xVelocity)>minimumVelocity||Math.abs(yVelocity)>minimumVelocity){
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

    private int getChildScrollY() {
        int oldY = 0;
        int childCount = getChildCount();
        if(0 < childCount){
            View childView = getChildAt(0);
            oldY = childView.getTop();
        }
        return oldY;
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

        public ViewFlinger(Context context) {
            overScroller=new OverScroller(context);
        }

        @Override
        public void run() {
            if (overScroller.isFinished()) {
                return;
            }
            overScroller.computeScrollOffset();
            int currX = overScroller.getCurrX();
            int currY = overScroller.getCurrY();
            int unconsumed = (int) (currY-lastMotionY);
            lastMotionX = currX;
            lastMotionY = currY;
            // Nested Scrolling Pre Pass
            mScrollConsumed[1] = 0;
            dispatchNestedPreScroll(0, unconsumed, mScrollConsumed, null, ViewCompat.TYPE_NON_TOUCH);
            unconsumed -= mScrollConsumed[1];
            if (unconsumed != 0) {
                // Internal Scroll
                int consumed = scrollVerticallyInternal(unconsumed);
                unconsumed -= consumed;
                // Nested Scrolling Post Pass
                mScrollConsumed[1] = 0;
                dispatchNestedScroll(0, consumed, 0, unconsumed, mScrollOffset,
                        ViewCompat.TYPE_NON_TOUCH, mScrollConsumed);
                unconsumed -= mScrollConsumed[1];
                invalidate();
            }
            if (unconsumed != 0) {
                overScroller.abortAnimation();
                stopNestedScroll(ViewCompat.TYPE_NON_TOUCH);
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

    //------------------------------------------------------------------
    //All about nested.
    //------------------------------------------------------------------

    //------------------------------------------------------------------
    //All about scroll.
    //------------------------------------------------------------------
    @Nullable
    View findOneVisibleChild(int fromIndex,int toIndex) {
        int next=toIndex > fromIndex?1:-1;
        int top = getPaddingTop();
        int i = fromIndex;
        while (i != toIndex) {
            View child = getChildAt(i);
            int childStart = child.getTop();
            int childEnd = child.getBottom();
            if (childStart<=top&&top<=childEnd) {
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
    public boolean canScrollHorizontally(int direction) {
        return super.canScrollHorizontally(direction);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return super.canScrollVertically(direction);
    }

    @Override
    protected int computeHorizontalScrollRange() {
        return super.computeHorizontalScrollRange();
    }

    @Override
    protected int computeHorizontalScrollOffset() {
        return super.computeHorizontalScrollOffset();
    }

    @Override
    protected int computeHorizontalScrollExtent() {
        return super.computeHorizontalScrollExtent();
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
        return SimpleScrollbarHelper.computeScrollOffset(this,firstVisibleView,lastVisibleView);
    }

    private int computeScrollExtent() {
        if (getChildCount() == 0) {
            return 0;
        }
        int firstVisibleItemPosition = findFirstVisibleItemPosition();
        int lastVisibleItemPosition = findLastVisibleItemPosition();
        View firstVisibleView = findViewByPosition(firstVisibleItemPosition);
        View lastVisibleView = findViewByPosition(lastVisibleItemPosition);
        return SimpleScrollbarHelper.computeScrollOffset(this,firstVisibleView,lastVisibleView);
    }

    private int computeScrollRange() {
        if (getChildCount() == 0) {
            return 0;
        }
        int firstVisibleItemPosition = findFirstVisibleItemPosition();
        int lastVisibleItemPosition = findLastVisibleItemPosition();
        View firstVisibleView = findViewByPosition(firstVisibleItemPosition);
        View lastVisibleView = findViewByPosition(lastVisibleItemPosition);
        return SimpleScrollbarHelper.computeScrollRange(this,firstVisibleView,lastVisibleView);
    }

    //------------------------------------------------------------------
    //All about save instance.
    //------------------------------------------------------------------

    @Override
    public void setSaveEnabled(boolean enabled) {
        super.setSaveEnabled(enabled);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    //------------------------------------------------------------------
    //All about edge effect.
    //------------------------------------------------------------------

    private void ensureGlows() {
        if (getOverScrollMode() != View.OVER_SCROLL_NEVER) {
            if (mEdgeGlowTop == null) {
                Context context = getContext();
                mEdgeGlowTop = new EdgeEffect(context);
                mEdgeGlowBottom = new EdgeEffect(context);
            }
        } else {
            mEdgeGlowTop = null;
            mEdgeGlowBottom = null;
        }
    }

//    @Override
//    public void draw(Canvas canvas) {
//        super.draw(canvas);
//        if (mEdgeGlowTop != null) {
//            final int scrollY = getScrollY();
//            if (!mEdgeGlowTop.isFinished()) {
//                final int restoreCount = canvas.save();
//                int width = getWidth();
//                int height = getHeight();
//                int xTranslation = 0;
//                int yTranslation = Math.min(0, scrollY);
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || getClipToPadding()) {
//                    width -= getPaddingLeft() + getPaddingRight();
//                    xTranslation += getPaddingLeft();
//                }
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getClipToPadding()) {
//                    height -= getPaddingTop() + getPaddingBottom();
//                    yTranslation += getPaddingTop();
//                }
//                canvas.translate(xTranslation, yTranslation);
//                mEdgeGlowTop.setSize(width, height);
//                if (mEdgeGlowTop.draw(canvas)) {
//                    ViewCompat.postInvalidateOnAnimation(this);
//                }
//                canvas.restoreToCount(restoreCount);
//            }
//            if (!mEdgeGlowBottom.isFinished()) {
//                final int restoreCount = canvas.save();
//                int width = getWidth();
//                int height = getHeight();
//                int xTranslation = 0;
//                int yTranslation = Math.max(getScrollRange(), scrollY) + height;
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || getClipToPadding()) {
//                    width -= getPaddingLeft() + getPaddingRight();
//                    xTranslation += getPaddingLeft();
//                }
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getClipToPadding()) {
//                    height -= getPaddingTop() + getPaddingBottom();
//                    yTranslation -= getPaddingBottom();
//                }
//                canvas.translate(xTranslation - width, yTranslation);
//                canvas.rotate(180, width, 0);
//                mEdgeGlowBottom.setSize(width, height);
//                if (mEdgeGlowBottom.draw(canvas)) {
//                    ViewCompat.postInvalidateOnAnimation(this);
//                }
//                canvas.restoreToCount(restoreCount);
//            }
//        }
//    }


    //------------------------------------------------------------------
    //All about nested scroll.
    //------------------------------------------------------------------


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
}
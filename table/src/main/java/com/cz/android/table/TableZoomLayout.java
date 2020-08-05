package com.cz.android.table;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import java.util.List;

/**
 * @author Created by cz
 * @date 2020-03-12 20:41
 * @email bingo110@126.com
 *
 * Just a example for {@link ZoomLayout}
 */
public class TableZoomLayout extends RecyclerZoomLayout {
    private static final String TAG="TableZoomLayout";
    private static final int NO_POSITION=-1;
    private static final int DIRECTION_START = -1;
    private static final int DIRECTION_END = 1;
    private final OrientationHelper.DynamicOrientationHelper orientationHelper;
    private final LayoutState layoutState=new LayoutState();
    private Adapter adapter;

    public TableZoomLayout(Context context) {
        this(context,null, 0);
    }

    public TableZoomLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TableZoomLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        setZoomEnabled(false);
        this.orientationHelper = OrientationHelper.createDynamicHelper(this);
    }

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
        this.layoutState.structureChanged=true;
        removeAllViews();
        clearRecyclerPool();
        requestLayout();
    }

    public Adapter getAdapter(){
        return adapter;
    }

    @Override
    protected View newAdapterView(Context context, ViewGroup parent, int viewType) {
        if(null==adapter){
            throw new NullPointerException("The adapter is null!");
        }
        return adapter.getView(context,parent,viewType);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //For a table we are not gonna support measurement mode like: AT_MOST.
        //So you have to determine the exactly size.
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(null!=adapter&&layoutState.structureChanged){
            //Change the bool to avoid do all the operation many times.
            layoutState.structureChanged=false;
            initializeLayoutState();
            //Fill the window
            fillHierarchyLayout();
        }
    }

    private void initializeLayoutState() {
        layoutState.available = orientationHelper.getTotalSpace();
        layoutState.itemDirection = DIRECTION_END;
        layoutState.layoutTableRow = 0;
        layoutState.layoutTableColumn = 0;
        layoutState.layoutOffset = 0;
        layoutState.layoutOffsetInOther = 0;
        layoutState.scrollingOffset = Integer.MIN_VALUE;
    }

    /**
     * Totally fill the hierarchy layout.
     */
    private void fillHierarchyLayout() {
        //Fill the layout.
        detachAndScrapAttachedViews();
        int totalSpace = orientationHelper.getTotalSpace();
        int totalSpaceInOther = orientationHelper.getTotalSpaceInOther();
        int top=getPaddingTop();
        int row=0;
        while(top<totalSpace&&tableRowHasMore()){
            int column = 0;
            int tableCellSize = 0;
            int left=getPaddingLeft();
            layoutState.layoutTableColumn = column;
            while(left<totalSpaceInOther&&tableColumnHasMore()){
                //Initialize the table column.
                int viewType = adapter.getViewType(row, column);
                View childView = getView(row,column,viewType);
                adapter.onBindView(childView,row,column);
                measureChildView(childView);
                //Check out does the table column exists.
                int tableCellWidth = adapter.getTableCellWidth(childView, row, column);
                int tableCellHeight=adapter.getTableCellHeight(childView,row,column);
                //Re-measure this view to fit the table cell.
                measureChildView(childView,tableCellWidth,tableCellHeight);
                int decoratedMeasuredWidth = getDecoratedMeasuredWidth(childView);
                int decoratedMeasuredHeight = getDecoratedMeasuredHeight(childView);
                layoutDecorated(childView,left,top,left+decoratedMeasuredWidth,top+decoratedMeasuredHeight);
                if(tableCellSize < tableCellHeight){
                    tableCellSize = tableCellHeight;
                }
                addTableViewInternal(childView,layoutState.itemDirection);
                left+=tableCellWidth;
                layoutState.layoutTableColumn++;
                column++;
            }
            top+=tableCellSize;
            layoutState.layoutTableRow++;
            row++;
        }
    }

    private boolean tableRowHasMore() {
        int rowCount = adapter.getRowCount();
        return layoutState.layoutTableRow >= 0 && layoutState.layoutTableRow < rowCount;
    }

    private boolean tableColumnHasMore() {
        int columnCount = adapter.getColumnCount();
        return layoutState.layoutTableColumn >= 0 && layoutState.layoutTableColumn < columnCount;
    }

    private View getView(int row,int column,int viewType){
        View view = super.getView(viewType);
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.row=row;
        layoutParams.column=column;
        return view;
    }

    private void addTableViewInternal(View child, int itemDirection){
        if(DIRECTION_START==itemDirection){
            addAdapterView(child,0);
        } else {
            addAdapterView(child,-1);
        }
    }

    private void updateLayoutStateHorizontally(LayoutState layoutState, int layoutDirection, int requiredSpace){
        int scrollingOffset=0;
        int childCount = getChildCount();
        View startView = getChildAt(0);
        View endView = getChildAt(childCount-1);
        if(null!=startView&&null!=endView){
            int startTableRow = getTableRow(startView);
            int startTableColumn = getTableColumn(startView);
            int endTableRow = getTableRow(endView);
            int endTableColumn = getTableColumn(endView);
            layoutState.tableRect.set(startTableColumn, startTableRow, endTableColumn,endTableRow);
            if(layoutDirection== DIRECTION_START){
                layoutState.itemDirection = DIRECTION_START;
                layoutState.layoutTableRow = startTableRow;
                layoutState.layoutTableColumn = startTableColumn;

                int childStart = orientationHelper.getDecoratedStart(startView);
                layoutState.layoutOffset = childStart;
                layoutState.layoutOffsetInOther = orientationHelper.getDecoratedEndInOther(endView);
                scrollingOffset = -childStart + orientationHelper.getStartAfterPadding();
            } else {
                layoutState.itemDirection = DIRECTION_END;
                layoutState.layoutTableRow = endTableRow;
                layoutState.layoutTableColumn = endTableColumn;

                int endAfterPadding = orientationHelper.getEndAfterPadding();
                int childEnd = orientationHelper.getDecoratedEnd(endView);
                layoutState.layoutOffset = childEnd;
                layoutState.layoutOffsetInOther = orientationHelper.getDecoratedStartInOther(startView);
                if(childEnd>=endAfterPadding){
                    scrollingOffset=childEnd-endAfterPadding;
                }
            }
            layoutState.layoutTableColumn += layoutState.itemDirection;
        }
        layoutState.available= requiredSpace - scrollingOffset;
        layoutState.scrollingOffset = scrollingOffset;
        Log.i(TAG,"updateLayoutStateHorizontally:"+layoutState.available+" scrollingOffset:"+scrollingOffset+" layoutOffset:"+layoutState.layoutOffset+" layoutOffsetInOther:"+layoutState.layoutOffsetInOther);
    }

    @Override
    protected int scrollHorizontallyBy(int dx, boolean isScaleDragged) {
        if(0 == dx||null==adapter||0 == adapter.getColumnCount()){
            return 0;
        }
        orientationHelper.setOrientation(OrientationHelper.HORIZONTAL);
        int consumedX=0;
        if (dx != 0) {
            consumedX = scrollHorizontallyByInternal(dx);
        }
        if(0 != consumedX){
            offsetChildrenLeftAndRight(-consumedX);
        }
        return consumedX;
    }

    private int scrollHorizontallyByInternal(int dx) {
        int absDx = Math.abs(dx);
        int layoutDirection=(0 < dx) ? DIRECTION_END : DIRECTION_START;
        updateLayoutStateHorizontally(layoutState,layoutDirection,absDx);
        if(0 > layoutState.available){
            layoutState.scrollingOffset +=layoutState.available;
        }
        int consumed;
        if(DIRECTION_START==layoutDirection){
            consumed=fillAndRecyclerLayoutFromStartHorizontally();
        } else {
            consumed=fillAndRecyclerLayoutFromEndHorizontally();
        }
        consumed += layoutState.scrollingOffset;
        return absDx > consumed ? overScrollHorizontally(layoutDirection,consumed) : dx;
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public boolean isHorizontal() {
        return orientationHelper.isHorizontal();
    }

    private int fillAndRecyclerLayoutFromStartHorizontally() {
        int available = layoutState.available;
        recyclerTableView(layoutState);
        int left=layoutState.layoutOffset;
        int column = layoutState.layoutTableColumn;
        while(0<layoutState.available&&tableColumnHasMore()){
            int tableCellSize=0;
            int top=layoutState.layoutOffsetInOther;
            layoutState.layoutTableColumn = column;
            int row = layoutState.tableRect.bottom;
            int tableEndRow = layoutState.tableRect.top;
            while(row>=tableEndRow && tableRowHasMore()){
                //Initialize the table column.
                int viewType = adapter.getViewType(row, column);
                View childView = getView(row,column,viewType);
                adapter.onBindView(childView,row,column);
                measureChildView(childView);
                //Check out does the table column exists.
                int tableCellWidth = adapter.getTableCellWidth(childView, row, column);
                int tableCellHeight = adapter.getTableCellHeight(childView,row,column);
                //Re-measure this view to fit the table cell.
                measureChildView(childView,tableCellWidth,tableCellHeight);
                layoutChildren(childView,layoutState.itemDirection,left,top);
                if(tableCellSize < tableCellWidth){
                    tableCellSize = tableCellWidth;
                }
                addTableViewInternal(childView,layoutState.itemDirection);
                layoutState.layoutTableColumn++;
                top-=tableCellHeight;
                row--;
            }
            layoutState.layoutTableRow++;
            layoutState.available-=tableCellSize;
            //Check if we need to recycler view that out of the screen.
            layoutState.scrollingOffset +=tableCellSize;
            if(0 > layoutState.available){
                layoutState.scrollingOffset +=layoutState.available;
            }
            left+=tableCellSize;
            column++;
        }
        return available-layoutState.available;
    }

    private int fillAndRecyclerLayoutFromEndHorizontally() {
        int available = layoutState.available;
        recyclerTableView(layoutState);
        int left=layoutState.layoutOffset;
        int column = layoutState.layoutTableColumn;
        while(0<layoutState.available&&tableColumnHasMore()){
            int tableCellSize=0;
            int top=layoutState.layoutOffsetInOther;
            layoutState.layoutTableColumn = column;
            int row = layoutState.tableRect.top;
            int tableEndRow = layoutState.tableRect.bottom;
            while(row<=tableEndRow && tableRowHasMore()){
                //Initialize the table column.
                int viewType = adapter.getViewType(row, column);
                View childView = getView(row,column,viewType);
                adapter.onBindView(childView,row,column);
                measureChildView(childView);
                //Check out does the table column exists.
                int tableCellWidth = adapter.getTableCellWidth(childView, row, column);
                int tableCellHeight = adapter.getTableCellHeight(childView,row,column);
                //Re-measure this view to fit the table cell.
                measureChildView(childView,tableCellWidth,tableCellHeight);
                layoutChildren(childView,layoutState.itemDirection,left,top);
                if(tableCellSize < tableCellWidth){
                    tableCellSize = tableCellWidth;
                }
                addTableViewInternal(childView,layoutState.itemDirection);
                layoutState.layoutTableColumn++;
                top+=tableCellHeight;
                row++;
            }
            layoutState.layoutTableRow++;
            layoutState.available-=tableCellSize;
            //Check if we need to recycler view that out of the screen.
            layoutState.scrollingOffset +=tableCellSize;
            if(0 > layoutState.available){
                layoutState.scrollingOffset +=layoutState.available;
            }
            recyclerTableView(layoutState);
            left+=tableCellSize;
            column++;
        }
        return available-layoutState.available;
    }

    private void layoutChildren(View child, int itemDirection, int left, int top) {
        int decoratedMeasuredWidth = getDecoratedMeasuredWidth(child);
        int decoratedMeasuredHeight = getDecoratedMeasuredHeight(child);
        if(DIRECTION_START==itemDirection){
            layoutDecorated(child,left-decoratedMeasuredWidth,top-decoratedMeasuredHeight,left,top);
        } else {
            layoutDecorated(child,left,top,left+decoratedMeasuredWidth,top+decoratedMeasuredHeight);
        }
    }

    private int overScrollHorizontally(int layoutDirection, int consumed) {
        return layoutDirection * consumed;
    }

    /**
     * Recycler all the child views that out of the screen.
     */
    private void recyclerTableView(LayoutState layoutState) {
        if(layoutState.itemDirection==DIRECTION_END){
            recycleViewFromStart(layoutState.scrollingOffset);
        } else if(layoutState.itemDirection==DIRECTION_START){
            recycleViewFromEnd(layoutState.scrollingOffset);
        }
    }

    private void recycleViewFromStart(int dt) {
        int limit=orientationHelper.getStartAfterPadding()+dt;
        int index=0;
        while(index<getChildCount()){
            View childView = getChildAt(index);
            int childEnd = orientationHelper.getDecoratedEnd(childView);
            if(childEnd <= limit){
                onRemoveAndRecycleView(childView);
                continue;
            }
            index++;
        }
    }

    private void recycleViewFromEnd(int dt) {
        final int limit = orientationHelper.getEndAfterPadding() - dt;
        int index = getChildCount()-1;
        while(0 <= index){
            View childView = getChildAt(index);
            int childStart = orientationHelper.getDecoratedStart(childView);
            if(childStart >= limit){
                onRemoveAndRecycleView(childView);
            }
            index--;
        }
    }

    private void updateLayoutStateVertically(LayoutState layoutState, int layoutDirection, int requiredSpace){
        int scrollingOffset=0;
        int childCount = getChildCount();
        View startView = getChildAt(0);
        View endView = getChildAt(childCount-1);
        if(null!=startView&&null!=endView){
            int startTableRow = getTableRow(startView);
            int startTableColumn = getTableColumn(startView);
            int endTableRow = getTableRow(endView);
            int endTableColumn = getTableColumn(endView);
            layoutState.tableRect.set(startTableColumn, startTableRow, endTableColumn,endTableRow);
            if(layoutDirection== DIRECTION_START){
                layoutState.itemDirection = DIRECTION_START;
                layoutState.layoutTableRow = startTableRow;
                layoutState.layoutTableColumn = startTableColumn;

                int childStart = orientationHelper.getDecoratedStart(startView);
                layoutState.layoutOffset = childStart;
                layoutState.layoutOffsetInOther = orientationHelper.getDecoratedEndInOther(endView);
                scrollingOffset = -childStart + orientationHelper.getStartAfterPadding();
            } else {
                layoutState.itemDirection = DIRECTION_END;
                layoutState.layoutTableRow = endTableRow;
                layoutState.layoutTableColumn = startTableColumn;

                int endAfterPadding = orientationHelper.getEndAfterPadding();
                int childEnd = orientationHelper.getDecoratedEnd(endView);
                layoutState.layoutOffset = childEnd;
                layoutState.layoutOffsetInOther = orientationHelper.getDecoratedStartInOther(startView);
                if(childEnd>=endAfterPadding){
                    scrollingOffset=childEnd-endAfterPadding;
                }
            }
            layoutState.layoutTableRow += layoutState.itemDirection;
        }
        layoutState.available= requiredSpace - scrollingOffset;
        layoutState.scrollingOffset = scrollingOffset;
        Log.i(TAG,"updateLayoutStateVertically:"+layoutState.available+" scrollingOffset:"+scrollingOffset+" layoutOffset:"+layoutState.layoutOffset+" layoutOffsetInOther:"+layoutState.layoutOffsetInOther);
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public boolean isVertical() {
        return orientationHelper.isVertical();
    }

    @Override
    protected int scrollVerticallyBy(int dy, boolean isScaleDragged) {
        if(0 == dy||null==adapter||0 == adapter.getColumnCount()){
            return 0;
        }
        orientationHelper.setOrientation(OrientationHelper.VERTICAL);
        int firstVisibleTableRow = findFirstVisibleTableRow();
        int lastVisibleTableRow = findLastVisibleTableRow();
        boolean canScrollVertically = canScrollVertically(dy);
        int consumedY=0;
        if (dy != 0) {
            consumedY = scrollVerticallyByInternal(dy);
        }
        if(0 != consumedY){
            offsetChildrenTopAndBottom(-consumedY);
        }
        return consumedY;
    }

    private int scrollVerticallyByInternal(int dy) {
        int absDy = Math.abs(dy);
        int layoutDirection=(0 < dy) ? DIRECTION_END : DIRECTION_START;
        updateLayoutStateVertically(layoutState,layoutDirection,absDy);
        if(0 > layoutState.available){
            layoutState.scrollingOffset +=layoutState.available;
        }
        int consumed;
        if(DIRECTION_START==layoutDirection){
            consumed=fillAndRecyclerLayoutFromStartVertically();
        } else {
            consumed=fillAndRecyclerLayoutFromEndVertically();
        }
        consumed += layoutState.scrollingOffset;
        return absDy > consumed ? overScrollVertically(layoutDirection,consumed) : dy;
    }

    protected int overScrollVertically(int layoutDirection, int consumed) {
        return layoutDirection*consumed;
    }

    private int fillAndRecyclerLayoutFromStartVertically() {
        int available = layoutState.available;
        recyclerTableView(layoutState);
        int top=layoutState.layoutOffset;
        int row = layoutState.layoutTableRow;
        while(0<layoutState.available&&tableRowHasMore()){
            int tableCellSize=0;
            int left=layoutState.layoutOffsetInOther;
            int column = layoutState.tableRect.right;
            int tableEndColumn = layoutState.tableRect.left;
            while(column>=tableEndColumn && tableColumnHasMore()){
                //Initialize the table column.
                int viewType = adapter.getViewType(row, column);
                View childView = getView(row,column,viewType);
                adapter.onBindView(childView,row,column);
                measureChildView(childView);
                //Check out does the table column exists.
                int tableCellWidth = adapter.getTableCellWidth(childView, row, column);
                int tableCellHeight = adapter.getTableCellHeight(childView,row,column);
                //Re-measure this view to fit the table cell.
                measureChildView(childView,tableCellWidth,tableCellHeight);
                layoutChildren(childView,layoutState.itemDirection,left,top);
                if(tableCellSize < tableCellHeight){
                    tableCellSize = tableCellHeight;
                }
                addTableViewInternal(childView,layoutState.itemDirection);
                layoutState.layoutTableColumn++;
                left-=tableCellWidth;
                column--;
            }
            layoutState.layoutTableRow--;
            layoutState.available-=tableCellSize;
            //Check if we need to recycler view that out of the screen.
            layoutState.scrollingOffset +=tableCellSize;
            if(0 > layoutState.available){
                layoutState.scrollingOffset +=layoutState.available;
            }
            top-=tableCellSize;
            row++;
        }
        return available-layoutState.available;
    }
    private int fillAndRecyclerLayoutFromEndVertically() {
        int available = layoutState.available;
        recyclerTableView(layoutState);
        int top=layoutState.layoutOffset;
        int row = layoutState.layoutTableRow;
        while(0<layoutState.available&&tableRowHasMore()){
            int tableCellSize=0;
            int left=layoutState.layoutOffsetInOther;
            int column = layoutState.tableRect.left;
            int tableEndColumn = layoutState.tableRect.right;
            while(column<=tableEndColumn && tableColumnHasMore()){
                //Initialize the table column.
                int viewType = adapter.getViewType(row, column);
                View childView = getView(row,column,viewType);
                adapter.onBindView(childView,row,column);
                measureChildView(childView);
                //Check out does the table column exists.
                int tableCellWidth = adapter.getTableCellWidth(childView, row, column);
                int tableCellHeight = adapter.getTableCellHeight(childView,row,column);
                //Re-measure this view to fit the table cell.
                measureChildView(childView,tableCellWidth,tableCellHeight);
                layoutChildren(childView,layoutState.itemDirection,left,top);
                if(tableCellSize < tableCellHeight){
                    tableCellSize = tableCellHeight;
                }
                addTableViewInternal(childView,layoutState.itemDirection);
                layoutState.layoutTableColumn++;
                left+=tableCellWidth;
                column++;
            }
            layoutState.layoutTableRow++;
            layoutState.available-=tableCellSize;
            //Check if we need to recycler view that out of the screen.
            layoutState.scrollingOffset +=tableCellSize;
            if(0 > layoutState.available){
                layoutState.scrollingOffset +=layoutState.available;
            }
            recyclerTableView(layoutState);
            top+=tableCellSize;
            row++;
        }
        return available-layoutState.available;
    }

    @Override
    public void removeAndRecycleView(View childView) {
        TableZoomLayout.LayoutParams layoutParams = (TableZoomLayout.LayoutParams) childView.getLayoutParams();
        int tableRow = layoutParams.row;
        int tableColumn = layoutParams.column;
        super.removeAndRecycleView(childView);
    }


    protected void onRemoveAndRecycleView(View child) {
        LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
        Log.i(TAG,"onRemoveAndRecycleView:"+layoutParams.row+" column:"+layoutParams.column);
        removeAndRecycleView(child);
    }

    public int getTableRow(View child) {
        LayoutParams layoutParams= (LayoutParams) child.getLayoutParams();
        return layoutParams.row;
    }

    public int getTableColumn(View child) {
        LayoutParams layoutParams= (LayoutParams) child.getLayoutParams();
        return layoutParams.column;
    }


    /**
     * Check if this table cell is inside the merged table cell.
     * @param mergedTableCell
     * @param column
     * @param row
     * @return
     */
    private boolean inMergedTableCell(List<Rect> mergedTableCell,int row,int column){
        for(Rect rect:mergedTableCell){
            if(rect.contains(column,row)){
                return true;
            }
        }
        return false;
    }

    //------------------------------------------------------------------
    //All about scroll.
    //------------------------------------------------------------------

    public View findViewByTablePosition(int row, int column) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            if(layoutParams.row==row&&layoutParams.column==column){
                return child;
            }
        }
        return null;
    }

    public int findFirstVisibleTableRow() {
        View child = getChildAt(0);
        return child == null ? NO_POSITION : getTableRow(child);
    }

    public int findLastVisibleTableRow() {
        int childCount = getChildCount();
        View child = getChildAt(childCount-1);
        return child == null ? NO_POSITION : getTableRow(child);
    }

    public int findFirstVisibleTableColumn() {
        View child = getChildAt(0);
        return child == null ? NO_POSITION : getTableColumn(child);
    }

    public int findLastVisibleTableColumn() {
        int childCount = getChildCount();
        View child = getChildAt(childCount - 1);
        return child == null ? NO_POSITION : getTableColumn(child);
    }

    @Override
    protected int computeHorizontalScrollRange() {
        int computeScrollRange = computeScrollRange();
        Log.i(TAG,"computeHorizontalScrollRange:"+computeScrollRange);
        return computeScrollRange;
    }

    @Override
    protected int computeHorizontalScrollOffset() {
        int computeScrollOffset = computeScrollOffset();
        Log.i(TAG,"computeHorizontalScrollOffset:"+computeScrollOffset);
        return computeScrollOffset;
    }

    @Override
    protected int computeHorizontalScrollExtent() {
        int computeScrollExtent = computeScrollExtent();
        Log.i(TAG,"computeHorizontalScrollExtent:"+computeScrollExtent);
        return computeScrollExtent;
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
        int firstVisibleTableRow = findFirstVisibleTableRow();
        int firstVisibleTableColumn = findFirstVisibleTableColumn();
        int lastVisibleTableRow = findLastVisibleTableRow();
        int lastVisibleTableColumn = findLastVisibleTableColumn();
        View firstVisibleView = findViewByTablePosition(firstVisibleTableRow,firstVisibleTableColumn);
        View lastVisibleView = findViewByTablePosition(lastVisibleTableRow,lastVisibleTableColumn);
        if(isHorizontal()){
            return SimpleTableScrollbarHelper.computeScrollOffsetHorizontally(this,orientationHelper,firstVisibleView,lastVisibleView);
        } else {
            return SimpleTableScrollbarHelper.computeScrollOffsetVertically(this,orientationHelper,firstVisibleView,lastVisibleView);
        }
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        final int offset = computeHorizontalScrollOffset();
        final int range = computeHorizontalScrollRange() - computeHorizontalScrollExtent();
        if (range == 0) return false;
        if (direction < 0) {
            return offset > 0;
        } else {
            boolean canScrollHorizontally=offset < range - 1;
            return canScrollHorizontally;
        }
    }

    private int computeScrollExtent() {
        if (getChildCount() == 0) {
            return 0;
        }
        int firstVisibleTableRow = findFirstVisibleTableRow();
        int firstVisibleTableColumn = findFirstVisibleTableColumn();
        int lastVisibleTableRow = findLastVisibleTableRow();
        int lastVisibleTableColumn = findLastVisibleTableColumn();
        View firstVisibleView = findViewByTablePosition(firstVisibleTableRow,firstVisibleTableColumn);
        View lastVisibleView = findViewByTablePosition(lastVisibleTableRow,lastVisibleTableColumn);
        if(isHorizontal()){
            return SimpleTableScrollbarHelper.computeScrollExtentHorizontally(this,orientationHelper,firstVisibleView,lastVisibleView);
        } else {
            return SimpleTableScrollbarHelper.computeScrollExtentVertically(this,orientationHelper,firstVisibleView,lastVisibleView);
        }
    }

    private int computeScrollRange() {
        if (getChildCount() == 0) {
            return 0;
        }
        int firstVisibleTableRow = findFirstVisibleTableRow();
        int firstVisibleTableColumn = findFirstVisibleTableColumn();
        int lastVisibleTableRow = findLastVisibleTableRow();
        int lastVisibleTableColumn = findLastVisibleTableColumn();
        View firstVisibleView = findViewByTablePosition(firstVisibleTableRow,firstVisibleTableColumn);
        View lastVisibleView = findViewByTablePosition(lastVisibleTableRow,lastVisibleTableColumn);
        if(isHorizontal()){
            return SimpleTableScrollbarHelper.computeScrollRangeHorizontally(this,orientationHelper,firstVisibleView,lastVisibleView);
        } else {
            return SimpleTableScrollbarHelper.computeScrollRangeVertically(this,orientationHelper,firstVisibleView,lastVisibleView);
        }
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        Context context = getContext();
        return new LayoutParams(context,attrs);
    }

    /**
     * {@link MarginLayoutParams LayoutParams} subclass for children of
     * {@link RecyclerZoomLayout}. All the sub-class of this View are encouraged
     * to create their own subclass of this <code>LayoutParams</code> class
     * to store any additional required per-child view metadata about the layout.
     */
    public static class LayoutParams extends RecyclerZoomLayout.LayoutParams {
        public int row;
        public int rowSpan=1;
        public int column;
        public int columnSpan=1;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(RecyclerZoomLayout.LayoutParams source) {
            super((ViewGroup.LayoutParams) source);
        }

        @NonNull
        @Override
        public String toString() {
            return "row:"+row+" column:"+column;
        }
    }

    /**
     * The abstract data adapter.
     */
    public static abstract class Adapter{
        /**
         * Return the row count of the table.
         * @return
         */
        public abstract int getRowCount();
        /**
         * Return the column count of the table.
         * @return
         */
        public abstract int getColumnCount();
        /**
         * Return the table column width.
         * @return
         */
        public int getTableCellWidth(View tableColumnView, int row, int column){ return tableColumnView.getMeasuredWidth(); }

        /**
         * Return the table column width.
         * @return
         */
        public int getTableCellHeight(View tableColumnView, int row, int column){ return tableColumnView.getMeasuredHeight(); }

        /**
         * For a merge table cell. Here return the row span count.
         * Notice:
         *  we will support this feature soon.
         * @param row
         * @param column
         * @return
         */
        public int getRowSpan(int row,int column){
            //todo id-1
            return 1;
        }

        /**
         * For a merge table cell. Here return the column span count.
         * Notice:
         *  we will support this feature soon.
         * @param row
         * @param column
         * @return
         */
        public int getColumnSpan(int row,int column){
            //todo id-1
            return 1;
        }

        /**
         * Return the view type by the specific row and column of the table.
         * @param row
         * @param column
         * @return
         */
        public int getViewType(int row,int column){
            return 0;
        }
        /**
         * Return a view by the given view type.
         * @param viewType
         * @return
         */
        public abstract View getView(Context context,ViewGroup parent, int viewType);

        /**
         * Binding the view by the specific row and column of the table.
         * @param view
         */
        public abstract void onBindView(View view, int row, int column);

    }

    private class LayoutState {
        /**
         * If the data structure has changed. We will fillTableAndRecycle the content again.
         */
        boolean structureChanged = false;
        /**
         * Current table cell rectangle.
         * For Example: [0, 0, 2, 2] means the start column of the table was zero and the end column of the table was two,
         * The start row of the table was zero the and the end row of the table was two.
         */
        Rect tableRect=new Rect();

        int layoutTableRow = 0;

        int layoutTableColumn =0;
        /**
         * The available space
         */
        int available = 0;
        /**
         * The scroll offset value used to recycle all the children that out of the screen.
         */
        int scrollingOffset = 0;
        /**
         * The layout offset is for us to know where we should put the children.
         */
        int layoutOffset = 0;
        /**
         * The layout offset in other direction is for us to know where we should put the children.
         * For example: if we scroll horizontally, the offset will be the left of the table. The the value: LayoutOffsetInOther will be the top of the table.
         */
        int layoutOffsetInOther = 0;
        /**
         * The direction of the layout.
         */
        int itemDirection = DIRECTION_END;
    }
}

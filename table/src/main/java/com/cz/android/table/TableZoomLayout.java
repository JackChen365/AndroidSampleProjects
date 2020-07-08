package com.cz.android.table;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;


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
    private static final int DIRECTION_START = -1;
    private static final int DIRECTION_END = 1;
    private final ZoomOrientationHelper orientationHelper;
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
        this.orientationHelper = ZoomOrientationHelper.createOrientationHelper(this,ZoomOrientationHelper.VERTICAL);
    }


    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
        this.layoutState.structureChanged=true;
        removeAllViews();
        clearRecyclerPool();
        requestLayout();
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
            //Fill the window
            fillHierarchyLayout();
        }
    }

    /**
     * Totally fill the hierarchy layout.
     */
    private void fillHierarchyLayout() {
        //Fill the layout.
        detachAndScrapAttachedViews();
        int columnCount = adapter.getColumnCount();
        int rowCount = adapter.getRowCount();
        int totalSpace = orientationHelper.getTotalSpace();
        int totalSpaceInOther = orientationHelper.getTotalSpaceInOther();
        int top=getPaddingTop();
        int row=0;
        while(row<rowCount&&top<totalSpace){
            int column = 0;
            int tableCellSize=0;
            int left=getPaddingLeft();
            while(column<columnCount&&left<totalSpaceInOther){
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
                if(0 == tableCellSize){
                    tableCellSize = tableCellHeight;
                }
                addTableCellInternal(childView,row,column);
                left+=tableCellWidth;
                column++;
            }
            top+=tableCellSize;
            row++;
        }
    }

    /**
     * Fill the layout by the new rectangle value.
     * @param tableLeft
     * @param tableTop
     * @param tableRight
     * @param tableBottom
     */
    private void fillHierarchyLayoutFromStart(int tableLeft,int tableTop,int tableRight,int tableBottom,int left,int top,int right,int bottom){
        int columnCount = adapter.getColumnCount();
        int rowCount = adapter.getRowCount();
        for(int row=tableTop;row<rowCount&&(row<=tableBottom||top<bottom);row++){
            int tableCellSize=0;
            for(int column= tableLeft;column<columnCount&&(column<=tableRight||left<right);column++){
                //Initialize the table column.
                int viewType = adapter.getViewType(row, column);
                View childView = getView(row,column,viewType);
                adapter.onBindView(childView,row,column);
                measureChildView(childView);
                int tableCellWidth = adapter.getTableCellWidth(childView, row, column);
                int tableCellHeight=adapter.getTableCellHeight(childView,row,column);
                //Re-measure this view to fit the table cell.
                measureChildView(childView,tableCellWidth,tableCellHeight);
                addTableCellInternal(childView,row,column);
                left+=tableCellWidth;
            }
            top+=tableCellSize;
        }
    }

    /**
     * Fill the layout by the new rectangle value.
     * @param tableLeft
     * @param tableTop
     * @param tableRight
     * @param tableBottom
     */
    private void fillHierarchyLayoutFromEnd(int tableLeft,int tableTop,int tableRight,int tableBottom,int left,int top,int right,int bottom){
        int columnCount = adapter.getColumnCount();
        int rowCount = adapter.getRowCount();
        for(int row=tableTop;row<rowCount&&(row<=tableBottom||top<bottom);row++){
            int tableCellSize=0;
            for(int column= tableLeft;column<columnCount&&(column<=tableRight||left<right);column++){
                //Initialize the table column.
                int viewType = adapter.getViewType(row, column);
                View childView = getView(row,column,viewType);
                adapter.onBindView(childView,row,column);
                measureChildView(childView);
                int tableCellWidth = adapter.getTableCellWidth(childView, row, column);
                int tableCellHeight=adapter.getTableCellHeight(childView,row,column);
                //Re-measure this view to fit the table cell.
                measureChildView(childView,tableCellWidth,tableCellHeight);
                addTableCellInternal(childView,row,column);
                left+=tableCellWidth;
            }
            top+=tableCellSize;
        }
    }

    private View getView(int row,int column,int viewType){
        View view = super.getView(viewType);
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.row=row;
        layoutParams.column=column;
        return view;
    }


    private View addTableCellInternal(View child, int row, int column){
//        int paddingLeft = getPaddingLeft();
//        int paddingTop = getPaddingTop();
//        int decoratedMeasuredWidth = getDecoratedMeasuredWidth(child);
//        int decoratedMeasuredHeight = getDecoratedMeasuredHeight(child);
//
//        int left=paddingLeft+tableIndexer.getTableCellOffsetX(column);
//        int top=paddingTop+tableIndexer.getTableCellOffsetY(row);
//
//        layoutDecorated(child,left,top,left+decoratedMeasuredWidth,top+decoratedMeasuredHeight);
//        offsetChild(child,-scrollX,-scrollY);
//        addAdapterView(child,-1);
        return child;
    }

    private void updateLayoutStateHorizontally(int dx){

    }

    private void updateLayoutStateVertically(LayoutState layoutState,int layoutDirection,int dy){
        int scrollingOffset=0;
        if(layoutDirection== DIRECTION_END){
            int childCount = getChildCount();
            View view=getChildAt(childCount-1);
            if(null!=view){
                layoutState.itemDirection= DIRECTION_END;
                layoutState.tableRow = getTableRow(view) + layoutState.itemDirection;
                layoutState.tableColumn = getTableColumn(view);
                layoutState.scrollingOffset = orientationHelper.getDecoratedEnd(view);
            }
        } else {
            View view = getChildAt(0);
            if(null!=view){
                layoutState.itemDirection= DIRECTION_START;
                layoutState.tableRow = getTableRow(view) + layoutState.itemDirection;
                layoutState.tableColumn = getTableColumn(view);
                layoutState.scrollingOffset = orientationHelper.getDecoratedStart(view);
            }
        }
//        layoutState.available= requiredSpace - scrollingOffset;
        layoutState.scrollingOffset = scrollingOffset;
        Log.i(TAG,"scrollingOffset:"+scrollingOffset);
    }

    @Override
    protected int scrollHorizontallyBy(int dx, boolean isScaleDragged) {
//        if(null==adapter){
//            return 0;
//        }
//        int totalSpaceInOther = orientationHelper.getTotalSpaceInOther();
//        //If the screen range contains the table cell. like: h:1-3 v-1-2
//        int paddingLeft = getPaddingLeft();
//        int left = scrollX + dx;
//        int right = left + totalSpaceInOther;
//        int leftTableCellColumn = tableIndexer.findTableCellColumn(left);
//        int rightTableCellColumn = tableIndexer.findTableCellColumn(right);
//        int columnCount = adapter.getColumnCount();
//        //Check the boundary of the screen horizontally
//        int scrolled = dx;
//        if(0 > leftTableCellColumn){
//            //to left
//            while(leftTableCellColumn!=0){
//                leftTableCellColumn++;
//                rightTableCellColumn++;
//            }
//            if(left < paddingLeft){
//                scrolled=-scrollX;
//            }
//        } else if(rightTableCellColumn>=columnCount){
//            //to right
//            while(rightTableCellColumn!=columnCount-1){
//                rightTableCellColumn--;
//                leftTableCellColumn--;
//            }
//            int endTableColumnOffset = tableIndexer.getEndTableColumnOffset();
//            if (right > endTableColumnOffset) {
//                scrolled = endTableColumnOffset-(right-dx);
//            }
//        }
//        int layoutDirection = dx > 0 ? DIRECTION_END : DIRECTION_START;
//        if(isScaleDragged||DIRECTION_START==layoutDirection){
//            //Move backward.
//            if(rect.right>rightTableCellColumn){
//                int oldHierarchyDepthIndex = rect.right+1;
//                tableIndexer.removeTableColumn(oldHierarchyDepthIndex,oldHierarchyDepthIndex+1);
//                Log.i(TAG,"scrollHorizontallyBy backward:"+leftTableCellColumn+" scrolled:"+scrolled+" rect:"+rect+" indexer:"+tableIndexer);
//                rect.right=rightTableCellColumn;
//            }
//            if(rect.left<leftTableCellColumn){
//                //When scaling the content. The right side of the hierarchy depth will less than the current depth.
//                Log.i(TAG,"scrollHorizontallyBy1:"+leftTableCellColumn+" scrolled:"+scrolled+" left:"+rect.left+" indexer:"+tableIndexer);
//                tableIndexer.removeTableColumn(rect.left-1,leftTableCellColumn);
//                rect.left=leftTableCellColumn;
//            } else if(rect.left>leftTableCellColumn){
//                int oldHierarchyDepthIndex = rect.left;
//                rect.left=leftTableCellColumn;
//                //Move forward.
//                fillHierarchyLayoutFromStart(leftTableCellColumn,rect.top,oldHierarchyDepthIndex-1,rect.bottom);
//                Log.i(TAG,"scrollHorizontallyBy2:"+leftTableCellColumn+" scrolled:"+scrolled+" rect:"+rect+" indexer:"+tableIndexer);
//            }
//        }
//        if(isScaleDragged||DIRECTION_END==layoutDirection){
//            if(rect.left<leftTableCellColumn){
//                int oldHierarchyDepthIndex=rect.left;
//                tableIndexer.removeTableColumn(oldHierarchyDepthIndex,oldHierarchyDepthIndex+1);
//                Log.i(TAG,"scrollHorizontallyBy forward:"+leftTableCellColumn+" scrolled:"+scrolled+" rect:"+rect+" indexer:"+tableIndexer);
//                rect.left=leftTableCellColumn;
//            }
//            if(rect.right>rightTableCellColumn){
//                //When scaling the content. The right side of the hierarchy depth will less than the current depth.
//                rect.right=rightTableCellColumn;
//            } else if(rect.right<rightTableCellColumn){
//                //Moving forward.
//                int oldHierarchyDepthIndex = rect.right;
//                rect.right=rightTableCellColumn;
//                fillHierarchyLayoutFromEnd(oldHierarchyDepthIndex+1,rect.top,rightTableCellColumn,rect.bottom);
//                Log.i(TAG,"scrollHorizontallyBy3:"+leftTableCellColumn+" scrolled:"+scrolled+" rect:"+rect+" indexer:"+tableIndexer);
//            }
//        }
////        Log.i(TAG,"scrollHorizontallyBy:"+tableIndexer.getStartTableColumn()+" end:"+tableIndexer.getEndTableColumn()+" right:"+rightHierarchyDepthIndex);
//        //After calculate with the offset value. we update the offset value.
//        scrollX+=scrolled;
//        //Recycler view by layout state.
//        recycleByLayoutState();
//        //Offset all the child views.
//        return offsetChildrenHorizontal(-scrolled);
        return 0;
    }

    @Override
    protected int scrollVerticallyBy(int dy, boolean isScaleDragged) {
//        int childCount = getChildCount();
//        if(0 == dy || null==adapter||0 == childCount){
//            return 0;
//        }
//        updateLayoutStateHorizontally(dy);
//        int totalSpace = orientationHelper.getTotalSpace();
//        //If the screen range contains the table cell. like: h:1-3 v-1-2
//        int paddingTop = getPaddingTop();
//        int top = dy+scrollY;
//        int bottom = top + totalSpace;
//        int topTableCellRow = tableIndexer.findTableCellRow(top);
//        int bottomTableCellRow = tableIndexer.findTableCellRow(bottom);
//        int rowCount = adapter.getRowCount();
//        int scrolled = dy;
//        if(0 > topTableCellRow){
//            //to top
//            while(topTableCellRow!=0){
//                topTableCellRow++;
//                bottomTableCellRow++;
//            }
//            if(top < paddingTop){
//                scrolled=-scrollY;
//            }
//        } else if(bottomTableCellRow>=rowCount){
//            //to bottom
//            while(bottomTableCellRow!=rowCount-1){
//                topTableCellRow--;
//                bottomTableCellRow--;
//            }
//            int endTableRowOffset = tableIndexer.getEndTableRowOffset();
//            if (bottom > endTableRowOffset) {
//                scrolled = endTableRowOffset-(bottom-dy);
//            }
//        }
//        top = scrolled+scrollY;
//        bottom = top + totalSpace;
//        topTableCellRow = tableIndexer.findTableCellRow(top);
//        bottomTableCellRow = tableIndexer.findTableCellRow(bottom);
//        Log.i(TAG,"scrollVerticallyBy:"+topTableCellRow+" bottom"+bottomTableCellRow+" scrollY:"+scrollY+" dy:"+dy+" scrolled:"+scrolled+" rect:"+rect+" indexer:"+tableIndexer);
//        int layoutDirection = dy > 0 ? DIRECTION_END : DIRECTION_START;
//        if(isScaleDragged||DIRECTION_START==layoutDirection){
//            //Move backward.
//            if(rect.bottom>bottomTableCellRow){
//                int oldTableCellRow = rect.bottom+1;
//                tableIndexer.removeTableRow(oldTableCellRow,oldTableCellRow+1);
//                Log.i(TAG,"scrollVerticallyBy backward:"+topTableCellRow+" bottom"+bottomTableCellRow+" scrolled:"+scrolled+" rect:"+rect+" indexer:"+tableIndexer);
//                rect.bottom=bottomTableCellRow;
//                if(rect.top==topTableCellRow){
//                    rect.top++;
//                }
//            }
//            if(rect.top<topTableCellRow){
//                //When scaling the content. The right side of the hierarchy depth will less than the current depth.
//                Log.i(TAG,"scrollVerticallyBy1:"+topTableCellRow+" bottom"+bottomTableCellRow+" scrolled:"+scrolled+" left:"+rect.left+" indexer:"+tableIndexer);
//                tableIndexer.removeTableRow(rect.top-1,topTableCellRow);
//                rect.top=topTableCellRow;
//            } else if(rect.top>topTableCellRow){
//                int oldTableCellRow = rect.top;
//                //Move forward.
//                int startTableRowOffset = tableIndexer.getStartTableRowOffset();
//                fillHierarchyLayoutFromStart(rect.left,topTableCellRow,rect.right,oldTableCellRow-1,0,top,0,startTableRowOffset);
//                rect.top=tableIndexer.getStartTableRow();
//                Log.i(TAG,"scrollVerticallyBy2:"+topTableCellRow+" bottom"+bottomTableCellRow+" scrolled:"+scrolled+" rect:"+rect+" indexer:"+tableIndexer);
//            }
//        }
//        if(isScaleDragged||DIRECTION_END==layoutDirection){
//            if(rect.top<topTableCellRow){
//                int oldTableCellRow=rect.top;
//                tableIndexer.removeTableRow(0,oldTableCellRow+1);
//                Log.i(TAG,"scrollVerticallyBy forward:"+topTableCellRow+" bottom"+bottomTableCellRow+" scrolled:"+scrolled+" rect:"+rect+" indexer:"+tableIndexer);
//                rect.top=topTableCellRow;
//                if(rect.bottom==bottomTableCellRow){
//                    rect.bottom--;
//                }
//            }
//            if(rect.bottom>bottomTableCellRow){
//                //When scaling the content. The right side of the hierarchy depth will less than the current depth.
//                rect.bottom=bottomTableCellRow;
//            } else if(rect.bottom<bottomTableCellRow){
//                //Moving forward.
//                int oldTableCellRow = rect.bottom;
//                Log.i(TAG,"scrollVerticallyBy3: top:"+topTableCellRow+" bottom:"+bottomTableCellRow+" scrolled:"+scrolled+" rect:"+rect+" indexer:"+tableIndexer);
//                int endTableRowOffset = tableIndexer.getEndTableRowOffset();
//                fillHierarchyLayoutFromEnd(rect.left,oldTableCellRow+1,rect.right,bottomTableCellRow,0,endTableRowOffset,0,bottom);
//                rect.bottom=tableIndexer.getEndTableRow();
//                Log.i(TAG,"scrollVerticallyBy3--------:"+topTableCellRow+" bottom:"+bottomTableCellRow+" scrolled:"+scrolled+" rect:"+rect+" indexer:"+tableIndexer);
//            }
//        }
//        //After calculate with the offset value. we update the offset value.
//        scrollY+=scrolled;
//        //Recycler view by layout state.
//        recycleByLayoutState();
//        //Offset all the child views.
//        return offsetChildrenVertical(-scrolled);
        return 0;
    }

    /**
     * Recycler all the child views that out of the screen.
     */
    private void recycleByLayoutState() {
//        for(int i=0;i<getChildCount();){
//            View childView = getChildAt(i);
//            LayoutParams layoutParams = (LayoutParams) childView.getLayoutParams();
//            int tableRow = layoutParams.row;
//            int tableColumn = layoutParams.column;
//            //Check the rect including the empty rect. This is not like the class:Rect#contains(x,y)
//            if(rect.left <= rect.right && rect.top <= rect.bottom
//                    && tableColumn >= rect.left && tableColumn <= rect.right && tableRow >= rect.top && tableRow <= rect.bottom){
//                //Still in this screen.
//                i++;
//            } else {
//                //This view is out of screen.
//                removeAndRecycleView(childView);
//            }
//        }
    }

    @Override
    public void removeAndRecycleView(View childView) {
        TableZoomLayout.LayoutParams layoutParams = (TableZoomLayout.LayoutParams) childView.getLayoutParams();
        int tableRow = layoutParams.row;
        int tableColumn = layoutParams.column;
        super.removeAndRecycleView(childView);
    }

//    @Override
//    public int getLayoutScrollX() {
//        return scrollX;
//    }
//
//    @Override
//    public int getLayoutScrollY() {
//        return scrollY;
//    }



    private int getTableRow(View child) {
        LayoutParams layoutParams= (LayoutParams) child.getLayoutParams();
        return layoutParams.row;
    }

    private int getTableColumn(View child) {
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

    @Override
    public boolean canScrollHorizontally(int direction) {
        if(null==adapter){
            return false;
        } else {
//            //Check the boundary of the screen horizontally
//            if(0 >= direction){
//                int left = direction + scrollX;
//                int paddingLeft = getPaddingLeft();
//                return left < paddingLeft;
//            } else {
//                int columnCount = adapter.getColumnCount();
//                int endTableColumn = tableIndexer.getEndTableColumn();
//                if(endTableColumn!=columnCount-1){
//                    return true;
//                } else {
//                    int left=scrollX;
//                    int measuredWidth = getMeasuredWidth();
//                    int tableCellOffsetX = tableIndexer.getTableCellOffsetX(endTableColumn);
//                    return left+measuredWidth<tableCellOffsetX;
//                }
//            }
        }
        return false;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if(null==adapter){
            return false;
        } else {
//            //Check the boundary of the screen horizontally
//            if(0 >= direction){
//                int top = direction + scrollY;
//                int paddingTop = getPaddingTop();
//                return top < paddingTop;
//            } else {
//                int rowCount = adapter.getRowCount();
//                int endTableRow = tableIndexer.getEndTableRow();
//                if(endTableRow!=rowCount-1){
//                    return true;
//                } else {
//                    int top=scrollY;
//                    int measuredHeight = getMeasuredHeight();
//                    int tableCellOffsetY = tableIndexer.getTableCellOffsetY(endTableRow);
//                    return top+measuredHeight<tableCellOffsetY;
//                }
//            }
        }
        return false;
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
        int tableRow = 0;

        int tableColumn=0;
        /**
         * The available space
         */
        int available = 0;
        /**
         * The scroll offset
         */
        int scrollingOffset = 0;
        /**
         * The direction of the layout.
         */
        int itemDirection = DIRECTION_END;
    }
}

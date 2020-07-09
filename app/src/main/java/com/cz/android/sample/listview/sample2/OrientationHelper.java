package com.cz.android.sample.listview.sample2;

import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Helper class for PullToRefreshLayout to abstract measurements depending on the View's orientation.
 * <p>
 * It is developed to easily support vertical and horizontal orientations in a PullToRefreshLayout but
 * can also be used to abstract calls around view bounds and child measurements
 *
 * @see #createDynamicHelper(SimpleNestedListView)
 * @see #createHorizontalHelper(SimpleNestedListView)
 * @see #createVerticalHelper(SimpleNestedListView)
 */
public abstract class OrientationHelper {
    protected final SimpleNestedListView layout;

    public static final int HORIZONTAL = RecyclerView.HORIZONTAL;

    public static final int VERTICAL = RecyclerView.VERTICAL;

    private OrientationHelper(SimpleNestedListView layout) {
        this.layout = layout;
    }

    public abstract boolean isHorizontal();

    public abstract boolean isVertical();

    public abstract int getOrientation();
    /**
     * Returns the start of the view
     */
    public abstract int getStart(View view);

    /**
     * Returns the start of the view in other direction.
     * For example for a horizontal helper. Call the method {@link #getStart(View)} will return left.
     * And calling this method will return top.
     */
    public abstract int getStartInOther(View view);

    /**
     * Returns the end of the view
     */
    public abstract int getEnd(View view);

    /**
     * Returns the end of the view in other direction.
     */
    public abstract int getEndInOther(View view);

    /**
     * Returns the total space without padding.
     * @return
     */
    public abstract int getTotalSpace();

    /**
     * Returns the total space in other direction without padding.
     * @return
     */
    public abstract int getTotalSpaceInOther();

    /**
     * Returns the space occupied by this View in the current orientation.
     *
     * @param view The view element to check
     * @return Total space occupied by this view
     * @see #getMeasurementInOther(View)
     */
    public abstract int getMeasurement(View view);

    public abstract int getDecorateMeasurement(View view);

    /**
     * Returns the space occupied by this View in the perpendicular orientation
     *
     * @param view The view element to check
     * @return Total space occupied by this view in the perpendicular orientation to current one
     * @see #getMeasurement(View)
     */
    public abstract int getMeasurementInOther(View view);

    public abstract int getDecorateMeasurementInOther(View view);

    /**
     * Returns the total space of the layout horizontally.
     *
     */
    public abstract int getStart();


    /**
     * Returns the total space of the layout horizontally.
     *
     */
    public abstract int getStartInOther();

    /**
     * Returns the end position of the layout without taking padding into account.
     *
     * @return The end boundary for this layout without considering padding.
     */
    public abstract int getEnd();

    /**
     * Returns the end position of the layout without taking padding into account.
     *
     * @return The end boundary for this layout without considering padding.
     */
    public abstract int getEndInOther();

    /**
     * Returns the total space to layout.
     *
     * @return Total space to layout children
     */
    public abstract int getMeasurement();
    /**
     * Returns the total space to layout.
     *
     * @return Total space to layout children
     */
    public abstract int getMeasurementAfterPadding();
    /**
     * Returns the total space of the layout in other direction.
     *
     * @return Total space of this layout
     */
    public abstract int getMeasurementInOther();

    /**
     * Returns the total space of the layout in other direction.
     *
     * @return Total space of this layout
     */
    public abstract int getMeasurementInOtherAfterPadding();

    /**
     * Returns the padding at the start of the layout. For horizontal helper, this is the left
     * padding and for vertical helper, this is the top padding. This method does not check
     * whether the layout is RTL or not.
     *
     * @return The padding at the end of the layout.
     */
    public abstract int getStartPadding();

    public abstract int getStartPaddingInOther();
    /**
     * Returns the padding at the end of the layout. For horizontal helper, this is the right
     * padding and for vertical helper, this is the bottom padding. This method does not check
     * whether the layout is RTL or not.
     *
     * @return The padding at the end of the layout.
     */
    public abstract int getEndPadding();

    public abstract int getEndPaddingInOther();

    /**
     * Returns the scroll value at the end of the layout. For horizontal helper, This value is same as scroll x.
     * For the vertical helper, this value is same as the scroll y.
     * @return
     */
    public abstract int getScroll();

    public abstract int getScrollInOther();

    /**
     * Return a value only if you want use it horizontally. If current direction is vertical. It will return zero.
     * @param value
     * @return
     */
    public abstract int getValueHorizontally(int value);

    /**
     * Return a value only if you want use it vertically. If current direction is horizontal. It will return zero.
     * Use this method to calculate to avoid check the direction.
     * @param value
     * @return
     */
    public abstract int getValueVertically(int value);

    /**
     * Creates an OrientationHelper for the given LayoutManager and orientation.
     *
     * @param parentView LayoutManager to attach to
     * @param orientation   Desired orientation. Should be {@link #HORIZONTAL} or {@link #VERTICAL}
     * @return A new OrientationHelper
     */
    public static OrientationHelper createOrientationHelper(
            SimpleNestedListView parentView, @RecyclerView.Orientation int orientation) {
        switch (orientation) {
            case HORIZONTAL:
                return createHorizontalHelper(parentView);
            case VERTICAL:
                return createVerticalHelper(parentView);
        }
        throw new IllegalArgumentException("invalid orientation");
    }

    /**
     * Create a orientation helper that support dynamic change orientation.
     * Which is mean you could change the orientation in any of your needs.
     * For example when you scroll from top to bottom, thus you could change the orientation to vertical.
     * And you move from left to right. You could change the direction to horizontal.
     * @param layout
     * @return
     */
    public static DynamicOrientationHelper createDynamicHelper(
            SimpleNestedListView layout) {
        OrientationHelper horizontalHelper = createHorizontalHelper(layout);
        OrientationHelper verticalHelper = createVerticalHelper(layout);
        return new DynamicOrientationHelper(layout,horizontalHelper,verticalHelper);
    }

    public static class DynamicOrientationHelper extends OrientationHelper {
        private final OrientationHelper horizontalHelper;
        private final OrientationHelper verticalHelper;
        private int orientation=VERTICAL;

        public DynamicOrientationHelper(SimpleNestedListView layout, OrientationHelper horizontalHelper, OrientationHelper verticalHelper) {
            super(layout);
            this.horizontalHelper = horizontalHelper;
            this.verticalHelper = verticalHelper;
        }

        public void setOrientation(int orientation){
            this.orientation=orientation;
        }

        @Override
        public boolean isHorizontal() {
            return orientation==HORIZONTAL;
        }

        @Override
        public boolean isVertical() {
            return orientation==VERTICAL;
        }

        @Override
        public int getOrientation() {
            return orientation;
        }

        @Override
        public int getStart(View view) {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getStart(view);
            } else {
                return verticalHelper.getStart(view);
            }
        }

        @Override
        public int getStartInOther(View view) {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getStartInOther(view);
            } else {
                return verticalHelper.getStartInOther(view);
            }
        }

        @Override
        public int getEnd(View view) {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getEnd(view);
            } else {
                return verticalHelper.getEnd(view);
            }
        }

        @Override
        public int getEndInOther(View view) {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getEndInOther(view);
            } else {
                return verticalHelper.getEndInOther(view);
            }
        }

        @Override
        public int getTotalSpace() {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getTotalSpace();
            } else {
                return verticalHelper.getTotalSpace();
            }
        }

        @Override
        public int getTotalSpaceInOther() {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getTotalSpaceInOther();
            } else {
                return verticalHelper.getTotalSpaceInOther();
            }
        }

        @Override
        public int getMeasurement(View view) {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getMeasurement(view);
            } else {
                return verticalHelper.getMeasurement(view);
            }
        }

        @Override
        public int getDecorateMeasurement(View view) {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getDecorateMeasurement(view);
            } else {
                return verticalHelper.getDecorateMeasurement(view);
            }
        }

        @Override
        public int getMeasurementInOther(View view) {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getMeasurementInOther(view);
            } else {
                return verticalHelper.getMeasurementInOther(view);
            }
        }

        @Override
        public int getDecorateMeasurementInOther(View view) {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getDecorateMeasurementInOther(view);
            } else {
                return verticalHelper.getDecorateMeasurementInOther(view);
            }
        }

        @Override
        public int getStart() {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getStart();
            } else {
                return verticalHelper.getStart();
            }
        }

        @Override
        public int getStartInOther() {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getStartInOther();
            } else {
                return verticalHelper.getStartInOther();
            }
        }

        @Override
        public int getEnd() {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getEnd();
            } else {
                return verticalHelper.getEnd();
            }
        }

        @Override
        public int getEndInOther() {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getEndInOther();
            } else {
                return verticalHelper.getEndInOther();
            }
        }

        @Override
        public int getMeasurement() {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getMeasurement();
            } else {
                return verticalHelper.getMeasurement();
            }
        }

        @Override
        public int getMeasurementAfterPadding() {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getMeasurementAfterPadding();
            } else {
                return verticalHelper.getMeasurementAfterPadding();
            }
        }

        @Override
        public int getMeasurementInOther() {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getMeasurementInOther();
            } else {
                return verticalHelper.getMeasurementInOther();
            }
        }

        @Override
        public int getMeasurementInOtherAfterPadding() {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getMeasurementInOtherAfterPadding();
            } else {
                return verticalHelper.getMeasurementInOtherAfterPadding();
            }
        }

        @Override
        public int getStartPadding() {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getStartPadding();
            } else {
                return verticalHelper.getStartPadding();
            }
        }

        @Override
        public int getStartPaddingInOther() {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getStartPaddingInOther();
            } else {
                return verticalHelper.getStartPaddingInOther();
            }
        }

        @Override
        public int getEndPadding() {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getEndPadding();
            } else {
                return verticalHelper.getEndPadding();
            }
        }

        @Override
        public int getEndPaddingInOther() {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getEndPaddingInOther();
            } else {
                return verticalHelper.getEndPaddingInOther();
            }
        }

        @Override
        public int getScroll() {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getScroll();
            } else {
                return verticalHelper.getScroll();
            }
        }

        @Override
        public int getScrollInOther() {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getScrollInOther();
            } else {
                return verticalHelper.getScrollInOther();
            }
        }

        @Override
        public int getValueHorizontally(int value) {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getValueHorizontally(value);
            } else {
                return verticalHelper.getValueHorizontally(value);
            }
        }

        @Override
        public int getValueVertically(int value) {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getValueVertically(value);
            } else {
                return verticalHelper.getValueVertically(value);
            }
        }
    }

    /**
     * Creates a horizontal OrientationHelper for the given LayoutManager.
     *
     * @param layout The LayoutManager to attach to.
     * @return A new OrientationHelper
     */
    public static OrientationHelper createHorizontalHelper(SimpleNestedListView layout) {
        return new OrientationHelper(layout) {

            @Override
            public boolean isHorizontal() {
                return true;
            }

            @Override
            public boolean isVertical() {
                return false;
            }

            @Override
            public int getOrientation() {
                return HORIZONTAL;
            }

            @Override
            public int getStart(View view) {
                return view.getLeft();
            }

            @Override
            public int getStartInOther(View view) {
                return view.getTop();
            }

            @Override
            public int getEnd(View view) {
                return view.getRight();
            }

            @Override
            public int getEndInOther(View view) {
                return view.getBottom();
            }

            @Override
            public int getTotalSpace() {
                return layout.getWidth()-layout.getPaddingLeft()-layout.getPaddingRight();
            }

            @Override
            public int getTotalSpaceInOther() {
                return layout.getHeight()-layout.getPaddingTop()-layout.getPaddingBottom();
            }

            @Override
            public int getMeasurement(View view) {
                return view.getMeasuredWidth();
            }

            @Override
            public int getDecorateMeasurement(View view) {
                return layout.getDecoratedMeasuredWidth(view);
            }

            @Override
            public int getMeasurementInOther(View view) {
                return view.getMeasuredHeight();
            }

            @Override
            public int getDecorateMeasurementInOther(View view) {
                return layout.getDecoratedMeasuredHeight(view);
            }

            @Override
            public int getStart() {
                return layout.getLeft();
            }

            @Override
            public int getStartInOther() {
                return layout.getTop();
            }

            @Override
            public int getEnd() {
                return layout.getRight();
            }

            @Override
            public int getEndInOther() {
                return layout.getBottom();
            }

            @Override
            public int getMeasurement() {
                return layout.getWidth();
            }

            @Override
            public int getMeasurementAfterPadding() {
                return layout.getWidth()-layout.getPaddingRight();
            }

            @Override
            public int getMeasurementInOther() {
                return layout.getHeight();
            }

            @Override
            public int getMeasurementInOtherAfterPadding() {
                return layout.getHeight()-layout.getPaddingBottom();
            }

            @Override
            public int getStartPadding() {
                return layout.getPaddingLeft();
            }

            @Override
            public int getStartPaddingInOther() {
                return layout.getPaddingTop();
            }

            @Override
            public int getEndPadding() {
                return layout.getPaddingRight();
            }

            @Override
            public int getEndPaddingInOther() {
                return layout.getPaddingBottom();
            }

            @Override
            public int getScroll() {
                return layout.getScrollX();
            }

            @Override
            public int getScrollInOther() {
                return layout.getScrollY();
            }

            @Override
            public int getValueHorizontally(int value) {
                return value;
            }

            @Override
            public int getValueVertically(int value) {
                return 0;
            }
        };
    }

    /**
     * Creates a vertical OrientationHelper for the given layout.
     *
     * @param layout The LayoutManager to attach to.
     * @return A new OrientationHelper
     */
    public static OrientationHelper createVerticalHelper(SimpleNestedListView layout) {
        return new OrientationHelper(layout) {

            @Override
            public boolean isHorizontal() {
                return false;
            }

            @Override
            public boolean isVertical() {
                return true;
            }

            @Override
            public int getOrientation() {
                return VERTICAL;
            }

            @Override
            public int getStart(View view) {
                return view.getTop();
            }

            @Override
            public int getStartInOther(View view) {
                return view.getLeft();
            }

            @Override
            public int getEnd(View view) {
                return view.getBottom();
            }

            @Override
            public int getEndInOther(View view) {
                return view.getRight();
            }

            @Override
            public int getTotalSpace() {
                return layout.getHeight()-layout.getPaddingTop()-layout.getPaddingBottom();
            }

            @Override
            public int getTotalSpaceInOther() {
                return layout.getWidth()-layout.getPaddingLeft()-layout.getPaddingRight();
            }

            @Override
            public int getMeasurement(View view) {
                return view.getMeasuredHeight();
            }

            @Override
            public int getDecorateMeasurement(View view) {
                return layout.getDecoratedMeasuredHeight(view);
            }

            @Override
            public int getMeasurementInOther(View view) {
                return view.getMeasuredWidth();
            }

            @Override
            public int getDecorateMeasurementInOther(View view) {
                return layout.getDecoratedMeasuredWidth(view);
            }

            @Override
            public int getStart() {
                return layout.getTop();
            }

            @Override
            public int getStartInOther() {
                return layout.getLeft();
            }

            @Override
            public int getEnd() {
                return layout.getBottom();
            }

            @Override
            public int getEndInOther() {
                return layout.getRight();
            }

            @Override
            public int getMeasurement() {
                return layout.getHeight();
            }

            @Override
            public int getMeasurementAfterPadding() {
                return layout.getHeight()-layout.getPaddingBottom();
            }

            @Override
            public int getMeasurementInOther() {
                return layout.getWidth();
            }

            @Override
            public int getMeasurementInOtherAfterPadding() {
                return layout.getWidth()-layout.getPaddingRight();
            }

            @Override
            public int getStartPadding() {
                return layout.getPaddingTop();
            }

            @Override
            public int getStartPaddingInOther() {
                return layout.getPaddingLeft();
            }

            @Override
            public int getEndPadding() {
                return layout.getPaddingBottom();
            }

            @Override
            public int getEndPaddingInOther() {
                return layout.getPaddingRight();
            }

            @Override
            public int getScroll() {
                return layout.getScrollY();
            }

            @Override
            public int getScrollInOther() {
                return layout.getScrollX();
            }

            @Override
            public int getValueHorizontally(int value) {
                return 0;
            }

            @Override
            public int getValueVertically(int value) {
                return value;
            }
        };
    }
}

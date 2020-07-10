package com.cz.android.sample.listview.list2;

import android.view.View;

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
    public abstract int getDecoratedStart(View view);

    /**
     * Returns the start of the view in other direction.
     * For example for a horizontal helper. Call the method {@link #getDecoratedStart(View)} will return left.
     * And calling this method will return top.
     */
    public abstract int getDecoratedStartInOther(View view);

    /**
     * Returns the end of the view
     */
    public abstract int getDecoratedEnd(View view);

    /**
     * Returns the end of the view in other direction.
     */
    public abstract int getDecoratedEndInOther(View view);

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
     * @return The very first pixel we can draw.
     */
    public abstract int getStartAfterPadding();

    /**
     * @return The last pixel we can draw
     */
    public abstract int getEndAfterPadding();

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
        public int getDecoratedStart(View view) {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getDecoratedStart(view);
            } else {
                return verticalHelper.getDecoratedStart(view);
            }
        }

        @Override
        public int getDecoratedStartInOther(View view) {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getDecoratedStartInOther(view);
            } else {
                return verticalHelper.getDecoratedStartInOther(view);
            }
        }

        @Override
        public int getDecoratedEnd(View view) {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getDecoratedEnd(view);
            } else {
                return verticalHelper.getDecoratedEnd(view);
            }
        }

        @Override
        public int getDecoratedEndInOther(View view) {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getDecoratedEndInOther(view);
            } else {
                return verticalHelper.getDecoratedEndInOther(view);
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
        public int getStartAfterPadding() {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getStartAfterPadding();
            } else {
                return verticalHelper.getStartAfterPadding();
            }
        }

        @Override
        public int getEndAfterPadding() {
            if(HORIZONTAL==orientation){
                return horizontalHelper.getEndAfterPadding();
            } else {
                return verticalHelper.getEndAfterPadding();
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
            public int getDecoratedStart(View view) {
                return layout.getDecoratedLeft(view);
            }

            @Override
            public int getDecoratedStartInOther(View view) {
                return layout.getDecoratedTop(view);
            }

            @Override
            public int getDecoratedEnd(View view) {
                return layout.getDecoratedRight(view);
            }

            @Override
            public int getDecoratedEndInOther(View view) {
                return layout.getDecoratedBottom(view);
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
            public int getStartAfterPadding() {
                return layout.getPaddingLeft();
            }

            @Override
            public int getEndAfterPadding() {
                return layout.getMeasuredWidth()-layout.getPaddingRight();
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
            public int getDecoratedStart(View view) {
                return layout.getDecoratedTop(view);
            }

            @Override
            public int getDecoratedStartInOther(View view) {
                return layout.getDecoratedLeft(view);
            }

            @Override
            public int getDecoratedEnd(View view) {
                return layout.getDecoratedBottom(view);
            }

            @Override
            public int getDecoratedEndInOther(View view) {
                return layout.getDecoratedRight(view);
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
            public int getStartAfterPadding() {
                return layout.getPaddingTop();
            }

            @Override
            public int getEndAfterPadding() {
                return layout.getMeasuredHeight()-layout.getPaddingBottom();
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
        };
    }
}

package com.cz.android.table;

import android.view.View;

/**
 * A helper class to do scroll offset calculations.
 */
class SimpleTableScrollbarHelper {
    /**
     * @param startChild View closest to start of the list. (top or left)
     * @param endChild   View closest to end of the list (bottom or right)
     */
    static int computeScrollOffsetHorizontally(TableZoomLayout listView,OrientationHelper orientationHelper, View startChild, View endChild) {
        if (listView.getChildCount() == 0 || startChild == null
                || endChild == null) {
            return 0;
        }
        final int minPosition = Math.min(listView.getTableColumn(startChild), listView.getTableColumn(endChild));
        final int itemsBefore = Math.max(0, minPosition);
        int firstChildStart =  orientationHelper.getDecoratedStart(startChild);
        int lastChildEnd = orientationHelper.getDecoratedEnd(endChild);
        final int laidOutArea = Math.abs(lastChildEnd - firstChildStart);
        final int itemRange = Math.abs(listView.getTableColumn(startChild) - listView.getTableColumn(endChild)) + 1;
        final float avgSizePerRow = (float) laidOutArea / itemRange;
        int startPadding = orientationHelper.getStartPadding();
        return Math.round(itemsBefore * avgSizePerRow + (startPadding - firstChildStart));
    }

    /**
     * @param startChild View closest to start of the list. (top or left)
     * @param endChild   View closest to end of the list (bottom or right)
     */
    static int computeScrollExtentHorizontally(TableZoomLayout listView,OrientationHelper orientationHelper, View startChild, View endChild) {
        if (listView.getChildCount() == 0 || startChild == null || endChild == null) {
            return 0;
        }
        int firstChildStart =  orientationHelper.getDecoratedStart(startChild);
        int lastChildEnd = orientationHelper.getDecoratedEnd(endChild);
        int totalSpace = orientationHelper.getTotalSpace();
        final int extend = lastChildEnd - firstChildStart;
        return Math.min(totalSpace, extend);
    }

    /**
     * @param startChild View closest to start of the list. (top or left)
     * @param endChild   View closest to end of the list (bottom or right)
     */
    static int computeScrollRangeHorizontally(TableZoomLayout listView,OrientationHelper orientationHelper, View startChild, View endChild) {
        if (listView.getChildCount() == 0 || startChild == null || endChild == null) {
            return 0;
        }
        // smooth scrollbar enabled. try to estimate better.
        int firstChildStart =  orientationHelper.getDecoratedStart(startChild);
        int lastChildEnd = orientationHelper.getDecoratedEnd(endChild);
        final int laidOutArea = lastChildEnd - firstChildStart;
        final int laidOutRange = Math.abs(listView.getTableColumn(startChild)
                - listView.getTableColumn(endChild))
                + 1;
        // estimate a size for full list.
        int itemCount=0;
        TableZoomLayout.Adapter adapter = listView.getAdapter();
        if(null!=adapter){
            itemCount=adapter.getColumnCount();
        }
        return (int) ((float) laidOutArea / laidOutRange * itemCount);
    }
    /**
     * @param startChild View closest to start of the list. (top or left)
     * @param endChild   View closest to end of the list (bottom or right)
     */
    static int computeScrollOffsetVertically(TableZoomLayout listView,OrientationHelper orientationHelper, View startChild, View endChild) {
        if (listView.getChildCount() == 0 || startChild == null
                || endChild == null) {
            return 0;
        }
        final int minPosition = Math.min(listView.getTableRow(startChild),
                listView.getTableRow(endChild));
        final int itemsBefore = Math.max(0, minPosition);
        int firstChildStart =  orientationHelper.getDecoratedStart(startChild);
        int lastChildEnd = orientationHelper.getDecoratedEnd(endChild);
        final int laidOutArea = Math.abs(lastChildEnd - firstChildStart);
        final int itemRange = Math.abs(listView.getTableRow(startChild) - listView.getTableRow(endChild)) + 1;
        final float avgSizePerRow = (float) laidOutArea / itemRange;
        int startPadding = orientationHelper.getStartPadding();
        return Math.round(itemsBefore * avgSizePerRow + (startPadding - firstChildStart));
    }

    /**
     * @param startChild View closest to start of the list. (top or left)
     * @param endChild   View closest to end of the list (bottom or right)
     */
    static int computeScrollExtentVertically(TableZoomLayout listView,OrientationHelper orientationHelper, View startChild, View endChild) {
        if (listView.getChildCount() == 0 || startChild == null || endChild == null) {
            return 0;
        }
        int firstChildStart =  orientationHelper.getDecoratedStart(startChild);
        int lastChildEnd = orientationHelper.getDecoratedEnd(endChild);
        int totalSpace = orientationHelper.getTotalSpace();
        final int extend = lastChildEnd - firstChildStart;
        return Math.min(totalSpace, extend);
    }

    /**
     * @param startChild View closest to start of the list. (top or left)
     * @param endChild   View closest to end of the list (bottom or right)
     */
    static int computeScrollRangeVertically(TableZoomLayout listView,OrientationHelper orientationHelper, View startChild, View endChild) {
        if (listView.getChildCount() == 0 || startChild == null || endChild == null) {
            return 0;
        }
        // smooth scrollbar enabled. try to estimate better.
        int firstChildStart =  orientationHelper.getDecoratedStart(startChild);
        int lastChildEnd = orientationHelper.getDecoratedEnd(endChild);
        final int laidOutArea = lastChildEnd - firstChildStart;
        final int laidOutRange = Math.abs(listView.getTableRow(startChild)
                - listView.getTableRow(endChild))
                + 1;
        // estimate a size for full list.
        int itemCount=0;
        TableZoomLayout.Adapter adapter = listView.getAdapter();
        if(null!=adapter){
            itemCount=adapter.getRowCount();
        }
        return (int) ((float) laidOutArea / laidOutRange * itemCount);
    }
}
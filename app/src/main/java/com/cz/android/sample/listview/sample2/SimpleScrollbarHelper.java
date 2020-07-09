package com.cz.android.sample.listview.sample2;

import android.view.View;

/**
 * A helper class to do scroll offset calculations.
 */
class SimpleScrollbarHelper {

    /**
     * @param startChild View closest to start of the list. (top or left)
     * @param endChild   View closest to end of the list (bottom or right)
     */
    static int computeScrollOffset(SimpleNestedListView listView,OrientationHelper orientationHelper, View startChild, View endChild) {
        if (listView.getChildCount() == 0 || startChild == null
                || endChild == null) {
            return 0;
        }
        final int minPosition = Math.min(listView.getPosition(startChild),
                listView.getPosition(endChild));
        final int itemsBefore = Math.max(0, minPosition);
        int firstChildStart =  orientationHelper.getStart(startChild);
        int lastChildEnd = orientationHelper.getEnd(endChild);
        final int laidOutArea = Math.abs(lastChildEnd - firstChildStart);
        final int itemRange = Math.abs(listView.getPosition(startChild) - listView.getPosition(endChild)) + 1;
        final float avgSizePerRow = (float) laidOutArea / itemRange;
        int startPadding = orientationHelper.getStartPadding();
        return Math.round(itemsBefore * avgSizePerRow + (startPadding - firstChildStart));
    }

    /**
     * @param startChild View closest to start of the list. (top or left)
     * @param endChild   View closest to end of the list (bottom or right)
     */
    static int computeScrollExtent(SimpleNestedListView listView,OrientationHelper orientationHelper, View startChild, View endChild) {
        if (listView.getChildCount() == 0 || startChild == null || endChild == null) {
            return 0;
        }
        int firstChildStart =  orientationHelper.getStart(startChild);
        int lastChildEnd = orientationHelper.getEnd(endChild);
        int totalSpace = orientationHelper.getTotalSpace();
        final int extend = lastChildEnd - firstChildStart;
        return Math.min(totalSpace, extend);
    }

    /**
     * @param startChild View closest to start of the list. (top or left)
     * @param endChild   View closest to end of the list (bottom or right)
     */
    static int computeScrollRange(SimpleNestedListView listView,OrientationHelper orientationHelper, View startChild, View endChild) {
        if (listView.getChildCount() == 0 || startChild == null || endChild == null) {
            return 0;
        }
        // smooth scrollbar enabled. try to estimate better.
        int firstChildStart =  orientationHelper.getStart(startChild);
        int lastChildEnd = orientationHelper.getEnd(endChild);
        final int laidOutArea = lastChildEnd - firstChildStart;
        final int laidOutRange = Math.abs(listView.getPosition(startChild)
                - listView.getPosition(endChild))
                + 1;
        // estimate a size for full list.
        int itemCount=0;
        SimpleNestedListView.Adapter adapter = listView.getAdapter();
        if(null!=adapter){
            itemCount=adapter.getItemCount();
        }
        return (int) ((float) laidOutArea / laidOutRange * itemCount);
    }
}
package com.cz.android.sample.nested.sample4;

import android.view.View;

/**
 * A helper class to do scroll offset calculations.
 */
class SimpleScrollbarHelper {

    /**
     * @param startChild View closest to start of the list. (top or left)
     * @param endChild   View closest to end of the list (bottom or right)
     */
    static int computeScrollOffset(SimpleNestedListView listView, View startChild, View endChild) {
        if (listView.getChildCount() == 0 || startChild == null
                || endChild == null) {
            return 0;
        }
        final int minPosition = Math.min(listView.getPosition(startChild),
                listView.getPosition(endChild));
        final int itemsBefore = Math.max(0, minPosition);
        final int laidOutArea = Math.abs(endChild.getBottom() - startChild.getTop());
        final int itemRange = Math.abs(listView.getPosition(startChild)
                - listView.getPosition(endChild)) + 1;
        final float avgSizePerRow = (float) laidOutArea / itemRange;
        int paddingTop = listView.getPaddingTop();
        return Math.round(itemsBefore * avgSizePerRow + (paddingTop - startChild.getTop()));
    }

    /**
     * @param startChild View closest to start of the list. (top or left)
     * @param endChild   View closest to end of the list (bottom or right)
     */
    static int computeScrollExtent(SimpleNestedListView listView, View startChild, View endChild) {
        if (listView.getChildCount() == 0 || startChild == null || endChild == null) {
            return 0;
        }
        final int extend = endChild.getBottom() - startChild.getTop();
        return Math.min(listView.getHeight(), extend);
    }

    /**
     * @param startChild View closest to start of the list. (top or left)
     * @param endChild   View closest to end of the list (bottom or right)
     */
    static int computeScrollRange(SimpleNestedListView listView, View startChild, View endChild) {
        if (listView.getChildCount() == 0 || startChild == null || endChild == null) {
            return 0;
        }
        // smooth scrollbar enabled. try to estimate better.
        final int laidOutArea = endChild.getBottom() - startChild.getTop();
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

    private SimpleScrollbarHelper() {
    }
}
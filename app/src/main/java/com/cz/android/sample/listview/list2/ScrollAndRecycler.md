## ScrollingAndRecycler

Talk about ListView and RecyclerView. The first thing we have to understand is the recycler strategy.
When we need recycle the view that out of the screen and how to recycle it?

* First, The easiest way to implement our own recycle strategy was remove the view out of the screen.
In that case, We do not have to use the other class fields to help us to calculate.

```
private void recycleFromStart() {
    int childCount = getChildCount();
    if(0 < childCount){
        int delta=xx;
        for(int i=0;i<childCount;i++){
            View childView = getChildAt(i);
            int bottom = childView.getBottom();
            if(bottom < delta){
                //We will cache the child view from zero to this specific position.
                recyclerChild(0,i);
                break;
            }
        }
    }
}
```



This will work properly if we scroll the screen gently. But soon, We have a problem.
If we move roughly. For example: Each view has the same size was 50dp.
We move it far away from the beginning. maybe 200dp in one time, or just scroll to a specific position programmatically.
Then we have to handle this. How to recycle the child while filling the content.

Here are the details:
* We should handle the scroll value when it does fill the new content.

```
fillAndRecyclerLayout:-12 mScrollingOffset:23
fillAndRecyclerLayout:-8 mScrollingOffset:4
fillAndRecyclerLayout:-4 mScrollingOffset:4
fillAndRecyclerLayout:-2 mScrollingOffset:2
fillAndRecyclerLayout:2 mScrollingOffset:2
fillAndRecyclerLayout:-105 mScrollingOffset:4
fillAndRecyclerLayout:-102 mScrollingOffset:3
fillAndRecyclerLayout:-99 mScrollingOffset:3
fillAndRecyclerLayout:-96 mScrollingOffset:3
fillAndRecyclerLayout:-93 mScrollingOffset:3
```

The offset is the amount of value we scrolled.

We use this as a baseline to recycle the child views.

![](https://raw.githubusercontent.com/momodae/LibraryResources/master/AndroidSampleProjects/imageimage2.jpg)

After we scroll 10 pixel down to the bottom. It will trigger recycling from the top. Though it may not available to fill the new content.


* If we scroll 200 pixel in once. It may cross two or three item. So what is the baseline of recycling strategy.

At the same points. Instead of filling all the available space all at once. We are going to fill the content and recycle the child.
The same problems. Where is the baseline for us to recycle the child from top.

We can not just recycle the child use the scroll offset value: 200 pixel. It might have other problem.
If the location near the bottom, and we the scroll value was over the bottom? If we just use the scroll value to recycle the list. It will be a disaster.







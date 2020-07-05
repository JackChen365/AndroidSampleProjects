/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cz.android.sample.transition;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;


import com.cz.android.layouttransition.LayoutTransition;
import com.cz.android.sample.R;

import java.util.ArrayList;

/**
 * A layout that arranges its children in a grid.  The size of the
 * cells is set by the {@link #setCellWidth(int)} method and the
 * android:cell_width and android:cell_height attributes in XML.
 * The number of rows and columns is determined at runtime.  Each
 * cell contains exactly one view, and they flow in the natural
 * child order (the order in which they were added, or the index
 * in {@link #addView(View, int)}.  Views can not span multiple cells.
 *
 * <p>This class was copied from the FixedGridLayout Api demo; see that demo for
 * more information on using the layout.</p>
 */
public class FixedGridLayout extends ViewGroup {
    private static final String TAG="TransitionDemonstrateLayout";
    // Used to animate add/remove changes in layout
    private LayoutTransition mTransition;
    // The set of views that are currently being transitioned. This list is used to track views
    // being removed that should not actually be removed from the parent yet because they are
    // being animated.
    private ArrayList<View> mTransitioningViews;
    // List of children changing visibility. This is used to potentially keep rendering
    // views during a transition when they otherwise would have become gone/invisible
    private ArrayList<View> mVisibilityChangingChildren;
    /**
     * Views which have been hidden or removed which need to be animated on
     * their way out.
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected ArrayList<View> mDisappearingChildren;

    private ArrayList<LayoutTransition> mPendingTransitions;

    private int cellWidth;
    private int cellHeight;

    public FixedGridLayout(Context context) {
        this(context,null);
    }

    public FixedGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FixedGridLayout, R.attr.fixedGridLayout, R.style.FixedGridLayout);
        setCellWidth(a.getDimensionPixelOffset(R.styleable.FixedGridLayout_fixed_cellWidth,0));
        setCellHeight(a.getDimensionPixelOffset(R.styleable.FixedGridLayout_fixed_cellHeight,0));
        a.recycle();
    }

    private void setCellWidth(int width) {
        cellWidth = width;
        requestLayout();
    }

    private void setCellHeight(int height) {
        cellHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int cellWidthSpec = MeasureSpec.makeMeasureSpec(cellWidth,
                MeasureSpec.AT_MOST);
        int cellHeightSpec = MeasureSpec.makeMeasureSpec(cellHeight,
                MeasureSpec.AT_MOST);

        int count = getChildCount();
        for (int index=0; index<count; index++) {
            final View child = getChildAt(index);
            if(View.GONE==child.getVisibility()){
                count--;
            } else {
                child.measure(cellWidthSpec, cellHeightSpec);
            }
        }
        // Use the size our parents gave us, but default to a minimum size to avoid
        // clipping transitioning children
        int minCount =  count > 3 ? count : 3;
        setMeasuredDimension(resolveSize(cellWidth * minCount, widthMeasureSpec),
                resolveSize(cellHeight * minCount, heightMeasureSpec));
    }


    private void layoutFixedGridLayout(boolean changed, int l, int t, int r, int b) {
        int cellWidth = this.cellWidth;
        int cellHeight = this.cellHeight;
        int columns = (r - l) / cellWidth;
        if (columns < 0) {
            columns = 1;
        }
        int x = 0;
        int y = 0;
        int i = 0;
        int count = getChildCount();
        for (int index=0; index<count; index++) {
            final View child = getChildAt(index);
            if(View.GONE!=child.getVisibility()){
                int w = child.getMeasuredWidth();
                int h = child.getMeasuredHeight();

                int left = x + ((cellWidth-w)/2);
                int top = y + ((cellHeight-h)/2);

                child.layout(left, top, left+w, top+h);
                if (i >= (columns-1)) {
                    // advance to next row
                    i = 0;
                    x = 0;
                    y += cellHeight;
                } else {
                    i++;
                    x += cellWidth;
                }
            }
        }
    }


    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        int oldVisibility = changedView.getVisibility();
        if (mTransition != null) {
            if (visibility == VISIBLE) {
                mTransition.showChild(this, changedView, oldVisibility);
            } else {
                mTransition.hideChild(this, changedView, visibility);
                if (mTransitioningViews != null && mTransitioningViews.contains(changedView)) {
                    // Only track this on disappearing views - appearing views are already visible
                    // and don't need special handling during drawChild()
                    if (mVisibilityChangingChildren == null) {
                        mVisibilityChangingChildren = new ArrayList<View>();
                    }
                    mVisibilityChangingChildren.add(changedView);
                    addDisappearingView(changedView);
                }
            }
        }
    }

    /**
     * Called when a view's visibility has changed. Notify the parent to take any appropriate
     * action.
     *
     * @param child The view whose visibility has changed
     * @param oldVisibility The previous visibility value (GONE, INVISIBLE, or VISIBLE).
     * @param newVisibility The new visibility value (GONE, INVISIBLE, or VISIBLE).
     * @hide
     */
    protected void onChildVisibilityChanged(View child, int oldVisibility, int newVisibility) {

    }

    public void requestTransitionStart(LayoutTransition transition) {
        if (mPendingTransitions == null || !mPendingTransitions.contains(transition)) {
            if (mPendingTransitions == null) {
                mPendingTransitions = new ArrayList<LayoutTransition>();
            }
            mPendingTransitions.add(transition);
        }
    }

    /**
     * Add a view which is removed from mChildren but still needs animation
     *
     * @param v View to add
     */
    private void addDisappearingView(View v) {
        ArrayList<View> disappearingChildren = mDisappearingChildren;

        if (disappearingChildren == null) {
            disappearingChildren = mDisappearingChildren = new ArrayList<View>();
        }

        disappearingChildren.add(v);
    }

    private LayoutTransition.TransitionListener mLayoutTransitionListener = new LayoutTransition.TransitionListener() {
        @Override
        public void startTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
            // We only care about disappearing items, since we need special logic to keep
            // those items visible after they've been 'removed'
            if (transitionType == LayoutTransition.DISAPPEARING) {
                startViewTransition(view);
            }
        }

        @Override
        public void endTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
            if (transitionType == LayoutTransition.DISAPPEARING && mTransitioningViews != null) {
                endViewTransition(view);
            }
        }
    };
    /**
     * Sets the LayoutTransition object for this ViewGroup. If the LayoutTransition object is
     * not null, changes in layout which occur because of children being added to or removed from
     * the ViewGroup will be animated according to the animations defined in that LayoutTransition
     * object. By default, the transition object is null (so layout changes are not animated).
     *
     * @param transition The LayoutTransition object that will animated changes in layout. A value
     * of <code>null</code> means no transition will run on layout changes.
     * @attr ref android.R.styleable#ViewGroup_animateLayoutChanges
     */
    public void setLayoutTransition(LayoutTransition transition) {
        if (mTransition != null) {
            mTransition.removeTransitionListener(mLayoutTransitionListener);
        }
        mTransition = transition;
        if (mTransition != null) {
            mTransition.addTransitionListener(mLayoutTransitionListener);
        }
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        if (mTransition != null) {
            // Don't prevent other add transitions from completing, but cancel remove
            // transitions to let them complete the process before we add to the container
            mTransition.cancel(LayoutTransition.DISAPPEARING);
        }
        if (child.getParent() != null) {
            throw new IllegalStateException("The specified child already has a parent. " +
                    "You must call removeView() on the child's parent first.");
        }
        if (mTransition != null) {
            mTransition.addChild(this, child);
        }
        super.addView(child, index, params);
    }

    @Override
    public void removeView(View view) {
        if (mTransition != null) {
            mTransition.removeChild(this, view);
        } else {
            int index = indexOfChild(view);
            super.removeViewAt(index);
        }
        if (view.getAnimation() != null || (mTransitioningViews != null && mTransitioningViews.contains(view))) {
            addDisappearingView(view);
        }
    }

    @Override
    public void removeViewAt(int index) {
        View view = getChildAt(index);
        if (mTransition != null) {
            mTransition.removeChild(this, view);
        } else {
            super.removeViewAt(index);
        }
        if (view.getAnimation() != null || (mTransitioningViews != null && mTransitioningViews.contains(view))) {
            addDisappearingView(view);
        }
    }

    @Override
    protected void removeDetachedView(View child, boolean animate) {
        if (mTransition != null) {
            mTransition.removeChild(this, child);
        } else {
            super.removeDetachedView(child,animate);
        }
        if ((animate && child.getAnimation() != null) ||
                (mTransitioningViews != null && mTransitioningViews.contains(child))) {
            addDisappearingView(child);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!mTransition.isChangingLayout()) {
            if (mTransition != null) {
                mTransition.layoutChange(this);
            }
        }
        layoutFixedGridLayout(changed, l, t, r, b);
    }

    /**
     * Utility function called by View during invalidation to determine whether a view that
     * is invisible or gone should still be invalidated because it is being transitioned (and
     * therefore still needs to be drawn).
     */
    boolean isViewTransitioning(View view) {
        return (mTransitioningViews != null && mTransitioningViews.contains(view));
    }

    /**
     * This method tells the ViewGroup that the given View object, which should have this
     * ViewGroup as its parent,
     * should be kept around  (re-displayed when the ViewGroup draws its children) even if it
     * is removed from its parent. This allows animations, such as those used by
     * {@link android.app.Fragment} and {@link android.animation.LayoutTransition} to animate
     * the removal of views. A call to this method should always be accompanied by a later call
     * to {@link #endViewTransition(View)}, such as after an animation on the View has finished,
     * so that the View finally gets removed.
     *
     * @param view The View object to be kept visible even if it gets removed from its parent.
     */
    public void startViewTransition(View view) {
        ViewParent parent = view.getParent();
        if (parent == this) {
            if (mTransitioningViews == null) {
                mTransitioningViews = new ArrayList<View>();
            }
            mTransitioningViews.add(view);
        }
    }

    /**
     * This method should always be called following an earlier call to
     * {@link #startViewTransition(View)}. The given View is finally removed from its parent
     * and will no longer be displayed. Note that this method does not perform the functionality
     * of removing a view from its parent; it just discontinues the display of a View that
     * has previously been removed.
     *
     * @return view The View object that has been removed but is being kept around in the visible
     * hierarchy by an earlier call to {@link #startViewTransition(View)}.
     */
    public void endViewTransition(View view) {
        if (mTransitioningViews != null) {
            mTransitioningViews.remove(view);
            final ArrayList<View> disappearingChildren = mDisappearingChildren;
            if (disappearingChildren != null && disappearingChildren.contains(view)) {
                disappearingChildren.remove(view);
                if (mVisibilityChangingChildren != null &&
                        mVisibilityChangingChildren.contains(view)) {
                    mVisibilityChangingChildren.remove(view);
                } else {
                    ViewParent parent = view.getParent();
                    if(null!=parent&&parent==this){
                        super.removeView(view);
                    }
                }
                invalidate();
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        boolean cancelDraw = getViewTreeObserver().dispatchOnPreDraw();
        if (!cancelDraw) {
            if (mPendingTransitions != null && mPendingTransitions.size() > 0) {
                for (int i = 0; i < mPendingTransitions.size(); ++i) {
                    mPendingTransitions.get(i).startChangingAnimations();
                }
                mPendingTransitions.clear();
            }
        }
        super.draw(canvas);
    }
}


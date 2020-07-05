package com.cz.android.gesture;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;

/**
 * @author Created by cz
 * @date 2020/6/28 12:27 PM
 * @email bingo110@126.com
 */
public abstract class SimpleViewGroup extends ViewGroup {
    private static final String TAG="SimpleView";
    private static final String[] ACTION_DESCRIPTION_ARRAY=new String[]{
        "Down","Up","Move","Cancel","Outside","PointerDown","PointerUp","HoverMove",
            "Scroll","HoverEnter","HoverExit","ButtonPress","ButtonRelease"
    };
    /**
     * When set, this ViewGroup should not intercept touch events.
     * {@hide}
     */
    protected static final int FLAG_DISALLOW_INTERCEPT = 0x80000;


    /**
     * Indicates whether the view is temporarily detached.
     *
     * @hide
     */
    static final int PFLAG_CANCEL_NEXT_UP_EVENT        = 0x04000000;

    /**
     * The offset, in pixels, by which the content of this view is scrolled
     * horizontally.
     * {@hide}
     */
    protected int mScrollX;
    /**
     * The offset, in pixels, by which the content of this view is scrolled
     * vertically.
     * {@hide}
     */
    protected int mScrollY;

    public int mPrivateFlags;
    /**
     * Internal flags.
     *
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected int mGroupFlags;
    // Target of Motion events
    private View mMotionTarget;
    private final Rect mTempRect = new Rect();

    public SimpleViewGroup(@NonNull Context context) {
        super(context);
    }

    public SimpleViewGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleViewGroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
        if (disallowIntercept == ((mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0)) {
            // We're already in this state, assume our ancestors are too
            return;
        }

        if (disallowIntercept) {
            mGroupFlags |= FLAG_DISALLOW_INTERCEPT;
        } else {
            mGroupFlags &= ~FLAG_DISALLOW_INTERCEPT;
        }
    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        mPrivateFlags |= PFLAG_CANCEL_NEXT_UP_EVENT;
    }

    @Override
    protected void onDetachedFromWindow() {
        mPrivateFlags &= ~PFLAG_CANCEL_NEXT_UP_EVENT;
        super.onDetachedFromWindow();
    }

    private String getResourceEntryName(){
        int id = getId();
        String description="no_id";
        if(View.NO_ID!=id){
            Resources resources = getResources();
            description=resources.getResourceEntryName(id);
        }
        return description;
    }
    private int getHierarchyDepth(){
        int depth=0;
        ViewParent parent = getParent();
        while(null!=parent){
            depth++;
            parent = parent.getParent();
        }
        return depth;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean result = dispatchTouchEventInternal(ev);
        Class<? extends SimpleViewGroup> clazz = getClass();
        String className = clazz.getSimpleName();
        int hierarchyDepth = getHierarchyDepth();
        String resourceEntryName = getResourceEntryName();
        final int action = ev.getActionMasked();
        CharSequence formatTab = padStart("", hierarchyDepth, '\t');
        Log.i(TAG,formatTab+"dispatchTouchEvent:"+className+"["+resourceEntryName+"]"+" depth:"+hierarchyDepth+" action:"+ACTION_DESCRIPTION_ARRAY[action]+" result:"+result);
        return result;
    }

    private boolean dispatchTouchEventInternal(MotionEvent ev) {
        if (!onFilterTouchEventForSecurity(ev)) {
            return false;
        }
        final int action = ev.getActionMasked();
        final float xf = ev.getX();
        final float yf = ev.getY();
        final float scrolledXFloat = xf + mScrollX;
        final float scrolledYFloat = yf + mScrollY;
        final Rect frame = mTempRect;

        boolean disallowIntercept = (mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0;

        if (action == MotionEvent.ACTION_DOWN) {
            if (mMotionTarget != null) {
                // this is weird, we got a pen down, but we thought it was
                // already down!
                // XXX: We should probably send an ACTION_UP to the current
                // target.
                mMotionTarget = null;
            }
            // If we're disallowing intercept or if we're allowing and we didn't
            // intercept
            if (disallowIntercept || !onInterceptTouchEvent(ev)) {
                // reset this event's action (just to protect ourselves)
                ev.setAction(MotionEvent.ACTION_DOWN);
                // We know we want to dispatch the event down, find a child
                // who can handle it, start with the front-most child.
                final int scrolledXInt = (int) scrolledXFloat;
                final int scrolledYInt = (int) scrolledYFloat;
                final int count = getChildCount();

                for (int i = count - 1; i >= 0; i--) {
                    final View child = getChildAt(i);
                    if ((child.getVisibility()) == VISIBLE || child.getAnimation() != null) {
                        child.getHitRect(frame);
                        if (frame.contains(scrolledXInt, scrolledYInt)) {
                            // offset the event to the view's coordinate system
                            final float xc = scrolledXFloat - child.getLeft();
                            final float yc = scrolledYFloat - child.getTop();
                            ev.setLocation(xc, yc);
//                            setCancelNextUpEvent(child,false);
                            if (child.dispatchTouchEvent(ev)) {
                                // Event handled, we have a target now.
                                mMotionTarget = child;
                                return true;
                            }
                            // The event didn't get handled, try the next view.
                            // Don't reset the event's location, it's not
                            // necessary here.
                        }
                    }
                }
            }
        }

        boolean isUpOrCancel = (action == MotionEvent.ACTION_UP) ||
                (action == MotionEvent.ACTION_CANCEL);

        if (isUpOrCancel) {
            // Note, we've already copied the previous state to our local
            // variable, so this takes effect on the next event
            mGroupFlags &= ~FLAG_DISALLOW_INTERCEPT;
        }

        // The event wasn't an ACTION_DOWN, dispatch it to our target if
        // we have one.
        final View target = mMotionTarget;
        if (target == null) {
            // We don't have a target, this means we're handling the
            // event as a regular view.
            ev.setLocation(xf, yf);
            if ((mPrivateFlags & PFLAG_CANCEL_NEXT_UP_EVENT) != 0) {
                ev.setAction(MotionEvent.ACTION_CANCEL);
                mPrivateFlags &= ~PFLAG_CANCEL_NEXT_UP_EVENT;
            }
            return super.dispatchTouchEvent(ev);
        }

        // if have a target, see if we're allowed to and want to intercept its
        // events
        if (!disallowIntercept && onInterceptTouchEvent(ev)) {
            final float xc = scrolledXFloat - (float) target.getLeft();
            final float yc = scrolledYFloat - (float) target.getTop();
            mPrivateFlags &= ~PFLAG_CANCEL_NEXT_UP_EVENT;
            ev.setAction(MotionEvent.ACTION_CANCEL);
            ev.setLocation(xc, yc);
            if (!target.dispatchTouchEvent(ev)) {
                // target didn't handle ACTION_CANCEL. not much we can do
                // but they should have.
            }
            // clear the target
            mMotionTarget = null;
            // Don't dispatch this event to our own view, because we already
            // saw it when intercepting; we just want to give the following
            // event to the normal onTouchEvent().
            return true;
        }

        if (isUpOrCancel) {
            mMotionTarget = null;
        }

        // finally offset the event to the target's coordinate system and
        // dispatch the event.
        final float xc = scrolledXFloat - (float) target.getLeft();
        final float yc = scrolledYFloat - (float) target.getTop();
        ev.setLocation(xc, yc);

//        if (isCancelNextUpEvent(target)) {
//            ev.setAction(MotionEvent.ACTION_CANCEL);
////            setCancelNextUpEvent(target,false);
//            mMotionTarget = null;
//        }

        return target.dispatchTouchEvent(ev);
    }

    private CharSequence padStart(CharSequence text,int length,char padChar) {
        if (length < 0)
            throw new IllegalArgumentException("Desired length $length is less than zero.");
        if (length <= text.length())
            return text.subSequence(0, text.length());

        StringBuilder sb = new StringBuilder(length);
        for (int i=1;i<(length - text.length());i++)
            sb.append(padChar);
        sb.append(text);
        return sb;
    }

    private boolean isCancelNextUpEvent(View child){
        int privateFlags = getPrivateFlags(child);
        return (privateFlags & PFLAG_CANCEL_NEXT_UP_EVENT) != 0;
    }

    private void setCancelNextUpEvent(View child,boolean isCancelNextUpEvent){
        int privateFlags = getPrivateFlags(child);
        if(isCancelNextUpEvent){
            privateFlags |=PFLAG_CANCEL_NEXT_UP_EVENT;
        } else {
            privateFlags &= ~PFLAG_CANCEL_NEXT_UP_EVENT;
        }
        setPrivateFlags(child,privateFlags);
    }

    private void setPrivateFlags(View child,int privateFlags){
        Class<? extends View> clazz = View.class;
        try {
            Field privateFlagsField = clazz.getDeclaredField("mPrivateFlags");
            privateFlagsField.setAccessible(true);
            privateFlagsField.set(child,privateFlags);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private int getPrivateFlags(View child){
        Class<? extends View> clazz = View.class;
        try {
            Field privateFlagsField = clazz.getDeclaredField("mPrivateFlags");
            privateFlagsField.setAccessible(true);
            return (int) privateFlagsField.get(child);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Set the scrolled position of your view. This will cause a call to
     * {@link #onScrollChanged(int, int, int, int)} and the view will be
     * invalidated.
     * @param x the x position to scroll to
     * @param y the y position to scroll to
     */
    public void scrollTo(int x, int y) {
        if (mScrollX != x || mScrollY != y) {
            int oldX = mScrollX;
            int oldY = mScrollY;
            mScrollX = x;
            mScrollY = y;
            onScrollChanged(mScrollX, mScrollY, oldX, oldY);
            if (!awakenScrollBars()) {
                invalidate();
            }
        }
    }

    /**
     * Move the scrolled position of your view. This will cause a call to
     * {@link #onScrollChanged(int, int, int, int)} and the view will be
     * invalidated.
     * @param x the amount of pixels to scroll by horizontally
     * @param y the amount of pixels to scroll by vertically
     */
    public void scrollBy(int x, int y) {
        scrollTo(mScrollX + x, mScrollY + y);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        if (ev.isFromSource(InputDevice.SOURCE_MOUSE)
//                && ev.getAction() == MotionEvent.ACTION_DOWN
//                && ev.isButtonPressed(MotionEvent.BUTTON_PRIMARY)
//                && isOnScrollbarThumb(ev.getX(), ev.getY())) {
//            return true;
//        }
//        return false;
        Class<? extends SimpleViewGroup> clazz = getClass();
        String className = clazz.getSimpleName();
        int hierarchyDepth = getHierarchyDepth();
        String resourceEntryName = getResourceEntryName();
        final int action = ev.getActionMasked();
        boolean result = onInterceptTouchEventInternal(ev);
        CharSequence formatTab = padStart("", hierarchyDepth, '\t');
        Log.i(TAG,formatTab+"onInterceptTouchEvent:"+className+"["+resourceEntryName+"]"+" depth:"+hierarchyDepth+" action:"+ACTION_DESCRIPTION_ARRAY[action]+" result:"+result);
        return result;
    }

    private boolean onInterceptTouchEventInternal(MotionEvent ev) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ev.isFromSource(InputDevice.SOURCE_MOUSE)
                    && ev.getAction() == MotionEvent.ACTION_DOWN
                    && ev.isButtonPressed(MotionEvent.BUTTON_PRIMARY)) {
                return true;
            }
        } else {
            if (ev.isFromSource(InputDevice.SOURCE_MOUSE)
                    && ev.getAction() == MotionEvent.ACTION_DOWN) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        final int scrollX = mScrollX;
        final int scrollY = mScrollY;
        canvas.save();
        canvas.translate(-scrollX,-scrollY);
        super.draw(canvas);
        canvas.restore();
    }
}

package com.cz.android.gesture;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * @author Created by cz
 * @date 2020/6/30 11:37 AM
 * @email bingo110@126.com
 */
public class SimpleView extends View {
    private static final String TAG="SimpleView";
    private static final String[] ACTION_DESCRIPTION_ARRAY=new String[]{
            "Down","Up","Move","Cancel","Outside","PointerDown","PointerUp","HoverMove",
            "Scroll","HoverEnter","HoverExit","ButtonPress","ButtonRelease"
    };
    private OnTouchListener mOnTouchListener;
    /**
     * Indicates a prepressed state;
     * the short time between ACTION_DOWN and recognizing
     * a 'real' press. Prepressed is used to recognize quick taps
     * even when they are shorter than ViewConfiguration.getTapTimeout().
     *
     * @hide
     */
    private static final int PREPRESSED             = 0x02000000;
    private CheckForLongPress mPendingCheckForLongPress;
    private CheckForTap mPendingCheckForTap = null;
    private PerformClick mPerformClick;


    private UnsetPressedState mUnsetPressedState;

    /**
     * Whether the long press's action has been invoked.  The tap's action is invoked on the
     * up event while a long press is invoked as soon as the long press duration is reached, so
     * a long press could be performed before the tap is checked, in which case the tap's action
     * should not be invoked.
     */
    private boolean mHasPerformedLongPress;
    private int mTouchSlop;
    int mPrivateFlags;
    public SimpleView(Context context) {
        super(context);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mTouchSlop=viewConfiguration.getScaledTouchSlop();
    }

    public SimpleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mTouchSlop=viewConfiguration.getScaledTouchSlop();
    }

    public SimpleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mTouchSlop=viewConfiguration.getScaledTouchSlop();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SimpleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mTouchSlop=viewConfiguration.getScaledTouchSlop();
    }

    /**
     * Remove the longpress detection timer.
     */
    private void removeLongPressCallback() {
        if (mPendingCheckForLongPress != null) {
            removeCallbacks(mPendingCheckForLongPress);
        }
    }

    /**
     * Remove the prepress detection timer.
     */
    private void removeUnsetPressCallback() {
        if (isPressed() && mUnsetPressedState != null) {
            setPressed(false);
            removeCallbacks(mUnsetPressedState);
        }
    }

    /**
     * Remove the tap detection timer.
     */
    private void removeTapCallback() {
        if (mPendingCheckForTap != null) {
            mPrivateFlags &= ~PREPRESSED;
            removeCallbacks(mPendingCheckForTap);
        }
    }

    /**
     * Cancels a pending long press.  Your subclass can use this if you
     * want the context menu to come up if the user presses and holds
     * at the same place, but you don't want it to come up if they press
     * and then move around enough to cause scrolling.
     */
    public void cancelLongPress() {
        removeLongPressCallback();

        /*
         * The prepressed state handled by the tap callback is a display
         * construct, but the tap callback will post a long press callback
         * less its own timeout. Remove it here.
         */
        removeTapCallback();
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        this.mOnTouchListener=l;
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

    /**
     * Pass the touch screen motion event down to the target view, or this
     * view if it is the target.
     *
     * @param event The motion event to be dispatched.
     * @return True if the event was handled by the view, false otherwise.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean result = dispatchTouchEventInternal(event);
        Class<? extends SimpleView> clazz = getClass();
        String className = clazz.getSimpleName();
        int hierarchyDepth = getHierarchyDepth();
        String resourceEntryName = getResourceEntryName();
        final int action = event.getActionMasked();
        CharSequence formatTab = padStart("", hierarchyDepth, '\t');
        Log.i(TAG,formatTab+"dispatchTouchEvent:"+className+"["+resourceEntryName+"]"+" depth:"+hierarchyDepth+" action:"+ACTION_DESCRIPTION_ARRAY[action]+" result:"+result);
        return result;
    }

    private boolean dispatchTouchEventInternal(MotionEvent event) {
        if (!onFilterTouchEventForSecurity(event)) {
            return false;
        }
        if (mOnTouchListener != null && isEnabled() &&
                mOnTouchListener.onTouch(this, event)) {
            return true;
        }
        return onTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = onTouchEventInternal(event);
        Class<? extends SimpleView> clazz = getClass();
        String className = clazz.getSimpleName();
        int hierarchyDepth = getHierarchyDepth();
        String resourceEntryName = getResourceEntryName();
        final int action = event.getActionMasked();
        CharSequence formatTab = padStart("", hierarchyDepth, '\t');
        Log.i(TAG,formatTab+"onTouchEvent:"+className+"["+resourceEntryName+"]"+" depth:"+hierarchyDepth+" action:"+ACTION_DESCRIPTION_ARRAY[action]+" result:"+result);
        return result;
    }

    private boolean onTouchEventInternal(MotionEvent event) {
        if (!isEnabled()) {
            // A disabled view that is clickable still consumes the touch
            // events, it just doesn't respond to them.
            return (isClickable() || isLongClickable());
        }

        TouchDelegate touchDelegate = getTouchDelegate();
        if (touchDelegate != null) {
            if (touchDelegate.onTouchEvent(event)) {
                return true;
            }
        }
        if (isClickable() || isLongClickable()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    boolean prepressed = (mPrivateFlags & PREPRESSED) != 0;
                    if ((isPressed() || prepressed)) {
                        // take focus if we don't have it already and we should in
                        // touch mode.
                        boolean focusTaken = false;
                        if (isFocusable() && isFocusableInTouchMode() && !isFocused()) {
                            focusTaken = requestFocus();
                        }

                        if (!mHasPerformedLongPress) {
                            // This is a tap, so remove the longpress check
                            removeLongPressCallback();

                            // Only perform take click actions if we were in the pressed state
                            if (!focusTaken) {
                                // Use a Runnable and post this rather than calling
                                // performClick directly. This lets other visual state
                                // of the view update before click actions start.
                                if (mPerformClick == null) {
                                    mPerformClick = new PerformClick();
                                }
                                if (!post(mPerformClick)) {
                                    performClick();
                                }
                            }
                        }

                        if (mUnsetPressedState == null) {
                            mUnsetPressedState = new UnsetPressedState();
                        }

                        if (prepressed) {
                            setPressed(true);
                            refreshDrawableState();
                            postDelayed(mUnsetPressedState,
                                    ViewConfiguration.getPressedStateDuration());
                        } else if (!post(mUnsetPressedState)) {
                            // If the post failed, unpress right now
                            mUnsetPressedState.run();
                        }
                        removeTapCallback();
                    }
                    break;

                case MotionEvent.ACTION_DOWN:
                    if (mPendingCheckForTap == null) {
                        mPendingCheckForTap = new CheckForTap();
                    }
                    mPrivateFlags |= PREPRESSED;
                    mHasPerformedLongPress = false;
                    postDelayed(mPendingCheckForTap, ViewConfiguration.getTapTimeout());
                    break;

                case MotionEvent.ACTION_CANCEL:
                    setPressed(false);
                    refreshDrawableState();
                    removeTapCallback();
                    break;

                case MotionEvent.ACTION_MOVE:
                    final int x = (int) event.getX();
                    final int y = (int) event.getY();

                    // Be lenient about moving outside of buttons
                    int slop = mTouchSlop;
                    if ((x < 0 - slop) || (x >= getWidth() + slop) ||
                            (y < 0 - slop) || (y >= getHeight() + slop)) {
                        // Outside button
                        removeTapCallback();
                        if (isPressed()) {
                            // Remove any future long press/tap checks
                            removeLongPressCallback();

                            // Need to switch from pressed to not pressed
                            setPressed(false);
                            refreshDrawableState();
                        }
                    }
                    break;
            }
            return true;
        }

        return false;
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

    private void postCheckForLongClick(int delayOffset) {
        mHasPerformedLongPress = false;

        if (mPendingCheckForLongPress == null) {
            mPendingCheckForLongPress = new CheckForLongPress();
        }
        mPendingCheckForLongPress.rememberWindowAttachCount();
        postDelayed(mPendingCheckForLongPress,
                ViewConfiguration.getLongPressTimeout() - delayOffset);
    }


    class CheckForLongPress implements Runnable {

        private int mOriginalWindowAttachCount;

        public void run() {
            ViewParent parent = getParent();
            int windowAttachCount = getWindowAttachCount();
            if (isPressed() && (parent != null)
                    && mOriginalWindowAttachCount == windowAttachCount) {
                if (performLongClick()) {
                    mHasPerformedLongPress = true;
                }
            }
        }

        public void rememberWindowAttachCount() {
            int windowAttachCount = getWindowAttachCount();
            mOriginalWindowAttachCount = windowAttachCount;
        }
    }

    private final class CheckForTap implements Runnable {
        public void run() {
            mPrivateFlags &= ~PREPRESSED;
            setPressed(true);
            refreshDrawableState();
            if (isLongClickable()) {
                postCheckForLongClick(ViewConfiguration.getTapTimeout());
            }
        }
    }

    private final class PerformClick implements Runnable {
        public void run() {
            performClick();
        }
    }

    private final class UnsetPressedState implements Runnable {
        public void run() {
            setPressed(false);
        }
    }
}

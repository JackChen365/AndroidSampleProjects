package com.cz.android.sample.drawable;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.cz.android.drawable.Drawable;
import com.cz.android.sample.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Created by cz
 * @date 2020/6/29 2:02 PM
 * @email bingo110@126.com
 */
public class RippleTextView extends AppCompatTextView implements Drawable.Callback2 {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private Drawable rippleDrawable;
    private boolean backgroundSizeChanged;
    public RippleTextView(Context context) {
        this(context, null,0);
    }

    public RippleTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RippleTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RippleTextView);
        int resourceId = a.getResourceId(R.styleable.RippleTextView_ripple_background, View.NO_ID);
        if(View.NO_ID!=resourceId){
            Resources r = getResources();
            InputStream as = r.openRawResource(resourceId);
            XmlPullParser xmlPullParser = Xml.newPullParser();
            try {
                xmlPullParser.setInput(as,"utf-8");
                Drawable drawable = Drawable.createFromXml(r, xmlPullParser);
                if(null!=drawable){
                    setRippleBackground(drawable);
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        a.recycle();
    }

    public void setRippleBackground(Drawable d) {
        boolean requestLayout = false;
        /*
         * Regardless of whether we're setting a new background or not, we want
         * to clear the previous drawable.
         */
        if (rippleDrawable != null) {
            rippleDrawable.setCallback(null);
            unscheduleDrawable2(rippleDrawable);
        }

        if (d != null) {
            // Compare the minimum sizes of the old Drawable and the new.  If there isn't an old or
            // if it has a different minimum size, we should layout again
            if (rippleDrawable == null || rippleDrawable.getMinimumHeight() != d.getMinimumHeight() ||
                    rippleDrawable.getMinimumWidth() != d.getMinimumWidth()) {
                requestLayout = true;
            }

            d.setCallback(this);
            if (d.isStateful()) {
                d.setState(getDrawableState());
            }
            d.setVisible(getVisibility() == VISIBLE, false);
            rippleDrawable = d;

        } else {
            /* Remove the background */
            rippleDrawable = null;
            /*
             * When the background is set, we try to apply its padding to this
             * View. When the background is removed, we don't touch this View's
             * padding. This is noted in the Javadocs. Hence, we don't need to
             * requestLayout(), the invalidate() below is sufficient.
             */

            // The old background's minimum size could have affected this
            // View's layout, so let's requestLayout
            requestLayout = true;
        }

        if (requestLayout) {
            requestLayout();
        }

        backgroundSizeChanged = true;
        invalidate();

        if (requestLayout) {
            requestLayout();
        }

        backgroundSizeChanged = true;
        invalidate();
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if(null!=rippleDrawable){
            rippleDrawable.jumpToCurrentState();
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        final int[] state = getDrawableState();
        boolean changed = false;
        final Drawable hl = rippleDrawable;
        if (hl != null && hl.isStateful()) {
            changed |= hl.setState(state);
        }
        if (changed) {
            invalidate();
        }
    }

    @Override
    public void dispatchDrawableHotspotChanged(float x, float y) {
        super.dispatchDrawableHotspotChanged(x, y);
        if(null!=rippleDrawable){
            rippleDrawable.setHotspot(x,y);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        drawBackground(canvas);
        super.draw(canvas);
    }

    /**
     * Sets the correct background bounds and rebuilds the outline, if needed.
     * <p/>
     * This is called by LayoutLib.
     */
    private void setBackgroundBounds() {
        final Drawable background = rippleDrawable;
        if (background != null&&backgroundSizeChanged) {
            int width = getWidth();
            int height = getHeight();
            background.setBounds(0, 0, width, height);
            backgroundSizeChanged = false;
        }
    }

    private void drawBackground(Canvas canvas) {
        final Drawable background = rippleDrawable;
        if (background == null) {
            return;
        }
        setBackgroundBounds();

        final int scrollX = getScrollX();
        final int scrollY = getScrollY();
        if ((scrollX | scrollY) == 0) {
            background.draw(canvas);
        } else {
            canvas.translate(scrollX, scrollY);
            background.draw(canvas);
            canvas.translate(-scrollX, -scrollY);
        }
    }


    @Override
    public void invalidateDrawable2(Drawable who) {
        final Rect dirty = who.getBounds();
        final int scrollX = getScrollX();
        final int scrollY = getScrollY();

        invalidate(dirty.left + scrollX, dirty.top + scrollY,
                dirty.right + scrollX, dirty.bottom + scrollY);
    }

    /**
     * Unschedule any events associated with the given Drawable.  This can be
     * used when selecting a new Drawable into a view, so that the previous
     * one is completely unscheduled.
     *
     * @param who The Drawable to unschedule.
     *
     * @see #drawableStateChanged
     */
    public void unscheduleDrawable2(Drawable who) {
        handler.removeCallbacksAndMessages(who);
    }

    @Override
    public void scheduleDrawable2(Drawable who, Runnable what, long when) {
        handler.postAtTime(what, who, when);
    }

    @Override
    public void unscheduleDrawable2(Drawable who, Runnable what) {
        handler.removeCallbacks(what, who);
    }
}

package com.cz.simple.nested;

import android.view.View;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

/**
 * @author Created by cz
 * @date 2020/7/7 10:25 AM
 * @email bingo110@126.com
 */
public class SimpleNestedScrollingChildHelper {
    private int[] mTempNestedScrollConsumed;
    private View nestedScrollingChild;
    private SimpleNestedScrollingParent touchedNestedScrollingParent;
    private SimpleNestedScrollingParent unTouchedNestedScrollingParent;
    private boolean enable=false;
    public SimpleNestedScrollingChildHelper(@NonNull View view) {
        this.nestedScrollingChild=view;
    }

    public void setNestedScrollingEnabled(boolean enable) {
        this.enable=enable;
    }

    public boolean isNestedScrollingEnabled() {
        return enable;
    }

    public boolean hasNestedScrollingParent(int type){
        return null!=getNestedScrollingParent(type);
    }

    public SimpleNestedScrollingParent getNestedScrollingParent(int type) {
        if(ViewCompat.TYPE_TOUCH==type){
            return touchedNestedScrollingParent;
        } else if(ViewCompat.TYPE_NON_TOUCH==type){
            return unTouchedNestedScrollingParent;
        }
        return null;
    }

    public boolean startNestedScroll(int axes, int type) {
        if(null!=getNestedScrollingParent(type)){
            //Already in process.
            return true;
        }
        ViewParent parent = nestedScrollingChild.getParent();
        View child=nestedScrollingChild;
        if(isNestedScrollingEnabled()){
            while(null!=parent){
                if(parent instanceof SimpleNestedScrollingParent){
                    SimpleNestedScrollingParent nestedScrollingParent = (SimpleNestedScrollingParent) parent;
                    setNestedScrollingParent(nestedScrollingParent,type);
                    nestedScrollingParent.onNestedScrollAccepted(child,nestedScrollingChild,axes,type);
                    break;
                }
                if(parent instanceof View){
                    child= (View) parent;
                    parent= parent.getParent();
                }
            }
        }
        return false;
    }

    private void setNestedScrollingParent(SimpleNestedScrollingParent nestedScrollingParent,int type) {
        if(ViewCompat.TYPE_TOUCH==type){
            touchedNestedScrollingParent=  nestedScrollingParent;
        } else if(ViewCompat.TYPE_NON_TOUCH==type){
            unTouchedNestedScrollingParent= nestedScrollingParent;
        }
    }

    public boolean dispatchNestedPreScroll(int dx, int dy, @NonNull int[] consumed, @Nullable int[] offsetInWindow, int type) {
        SimpleNestedScrollingParent nestedScrollingParent = getNestedScrollingParent(type);
        if(null==nestedScrollingParent){
            return false;
        }
        if(isNestedScrollingEnabled()){

            if(0 != dx || 0 != dy){
                int startX=0;
                int startY=0;
                if(null!=offsetInWindow){
                    nestedScrollingChild.getLocationInWindow(offsetInWindow);
                    startX=offsetInWindow[0];
                    startY=offsetInWindow[1];
                }
                if(null==consumed){
                    consumed=getTempNestedScrollConsumed();
                    consumed[0]=0;
                    consumed[1]=0;
                }
                nestedScrollingParent.onNestedPreScroll(nestedScrollingChild,dx,dy,consumed,type);
                if(null!=offsetInWindow){
                    nestedScrollingChild.getLocationInWindow(offsetInWindow);
                    offsetInWindow[0]-=startX;
                    offsetInWindow[1]-=startY;
                }
                return consumed[0] != 0 || consumed[1] != 0;
            } else if(null!=offsetInWindow){
                offsetInWindow[0]=0;
                offsetInWindow[1]=0;
            }
        }
        return false;
    }

    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow, int type, int[] consumed) {
        SimpleNestedScrollingParent nestedScrollingParent = getNestedScrollingParent(type);
        if(null==nestedScrollingParent){
            return false;
        }
        if(isNestedScrollingEnabled()){
            if(0 != dxConsumed || 0 != dyConsumed|| 0 !=dxUnconsumed|| 0 != dyUnconsumed){
                int startX=0;
                int startY=0;
                if(null!=offsetInWindow){
                    nestedScrollingChild.getLocationInWindow(offsetInWindow);
                    startX=offsetInWindow[0];
                    startY=offsetInWindow[1];
                }
                if(null==consumed){
                    consumed=getTempNestedScrollConsumed();
                    consumed[0]=0;
                    consumed[1]=0;
                }
                nestedScrollingParent.onNestedScroll(nestedScrollingChild,dxConsumed,dyConsumed,dxUnconsumed,dyUnconsumed,type,consumed);
                if(null!=offsetInWindow){
                    nestedScrollingChild.getLocationInWindow(offsetInWindow);
                    offsetInWindow[0]-=startX;
                    offsetInWindow[1]-=startY;
                }
                return true;
            }
        }
        return false;
    }

    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        if(isNestedScrollingEnabled()){
            SimpleNestedScrollingParent nestedScrollingParent = getNestedScrollingParent(ViewCompat.TYPE_TOUCH);
            if(null!=nestedScrollingParent){
                return nestedScrollingParent.onNestedFling(nestedScrollingChild,velocityX,velocityY,consumed);
            }
        }
        return false;
    }

    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        if(isNestedScrollingEnabled()){
            SimpleNestedScrollingParent nestedScrollingParent = getNestedScrollingParent(ViewCompat.TYPE_TOUCH);
            if(null!=nestedScrollingParent){
                return nestedScrollingParent.onNestedPreFling(nestedScrollingChild,velocityX,velocityY);
            }
        }
        return false;
    }

    public void stopNestedScroll(int type) {
        SimpleNestedScrollingParent nestedScrollingParent = getNestedScrollingParent(type);
        if(null!=nestedScrollingParent){
            nestedScrollingParent.onStopNestedScroll(nestedScrollingChild,type);
            setNestedScrollingParent(null,type);
        }
    }

    private int[] getTempNestedScrollConsumed() {
        if (mTempNestedScrollConsumed == null) {
            mTempNestedScrollConsumed = new int[2];
        }
        return mTempNestedScrollConsumed;
    }
}

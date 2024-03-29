/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.cz.android.text.span;

import android.text.TextPaint;

import com.cz.android.text.style.CharacterStyle;
import com.cz.android.text.style.UpdateLayout;


/**
 * The classes that affect character-level text formatting in a way that
 * changes the width or height of characters extend this class.
 */
public abstract class MetricAffectingSpan
extends CharacterStyle
implements UpdateLayout {

	public abstract void updateMeasureState(TextPaint p);

    /**
     * Returns "this" for most MetricAffectingSpans, but for 
     * MetricAffectingSpans that were generated by {@link #wrap},
     * returns the underlying MetricAffectingSpan.
     */
    @Override
    public MetricAffectingSpan getUnderlying() {
        return this;
    }

    /**
     * A Passthrough MetricAffectingSpan is one that
     * passes {@link #updateDrawState} and {@link #updateMeasureState}
     * calls through to the specified MetricAffectingSpan 
     * while still being a distinct object,
     * and is therefore able to be attached to the same Spannable
     * to which the specified MetricAffectingSpan is already attached.
     */
    /* package */ public static class Passthrough extends MetricAffectingSpan {
        private MetricAffectingSpan mStyle;
        
        /**
         * Creates a new Passthrough of the specfied MetricAffectingSpan.
         */
        public Passthrough(MetricAffectingSpan cs) {
            mStyle = cs;
        }

        /**
         * Passes updateDrawState through to the underlying MetricAffectingSpan.
         */
        @Override
        public void updateDrawState(TextPaint tp) {
            mStyle.updateDrawState(tp);
        }

        /**
         * Passes updateMeasureState through to the underlying MetricAffectingSpan.
         */
        @Override
        public void updateMeasureState(TextPaint tp) {
            mStyle.updateMeasureState(tp);
        }
    
        /**
         * Returns the MetricAffectingSpan underlying this one, or the one
         * underlying it if it too is a Passthrough.
         */
        @Override
        public MetricAffectingSpan getUnderlying() {
            return mStyle.getUnderlying();
        }
    }
}

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

import android.os.Parcel;
import android.text.TextPaint;

import com.cz.android.text.style.CharacterStyle;
import com.cz.android.text.style.UpdateAppearance;
import com.cz.android.text.utils.TextUtilsCompat;

public class ForegroundColorSpan extends CharacterStyle implements UpdateAppearance {

    private final int mColor;

	public ForegroundColorSpan(int color) {
		mColor = color;
	}

    public ForegroundColorSpan(Parcel src) {
        mColor = src.readInt();
    }
    
    public int getSpanTypeId() {
        return TextUtilsCompat.FOREGROUND_COLOR_SPAN;
    }
    
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mColor);
    }

	public int getForegroundColor() {
		return mColor;
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setColor(mColor);
	}
}

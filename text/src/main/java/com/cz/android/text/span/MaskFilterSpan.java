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

import android.graphics.MaskFilter;
import android.text.TextPaint;

import com.cz.android.text.style.CharacterStyle;
import com.cz.android.text.style.UpdateAppearance;

public class MaskFilterSpan extends CharacterStyle implements UpdateAppearance {

	private MaskFilter mFilter;

	public MaskFilterSpan(MaskFilter filter) {
		mFilter = filter;
	}

	public MaskFilter getMaskFilter() {
		return mFilter;
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setMaskFilter(mFilter);
	}
}

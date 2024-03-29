/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.cz.android.exoplayer.util;

import java.util.Arrays;

/**
 * An append-only, auto-growing {@code long[]}.
 */
public class LongArray {

  private static final int DEFAULT_INITIAL_CAPACITY = 32;

  private int size;
  private long[] values;

  public LongArray() {
    this(DEFAULT_INITIAL_CAPACITY);
  }

  /**
   * @param initialCapacity The initial capacity of the array.
   */
  public LongArray(int initialCapacity) {
    values = new long[initialCapacity];
  }

  /**
   * Appends a value.
   *
   * @param value The value to append.
   */
  public void add(long value) {
    if (size == values.length) {
      values = Arrays.copyOf(values, size * 2);
    }
    values[size++] = value;
  }

  /**
   * Gets a value.
   *
   * @param index The index.
   * @return The corresponding value.
   * @throws IndexOutOfBoundsException If the index is less than zero, or greater than or equal to
   *     {@link #size()}
   */
  public long get(int index) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException("Invalid size " + index + ", size is " + size);
    }
    return values[index];
  }

  /**
   * Gets the current size of the array.
   *
   * @return The current size of the array.
   */
  public int size() {
    return size;
  }

}

/*
 * Copyright (C) 2012 Square, Inc.
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
package com.squareup.okhttp.internal.spdy;

public final class Settings {
  /**
   * From the spdy/3 spec, the default initial window size for all streams is
   * 64 KiB. (Chrome 25 uses 10 MiB).
   */
  public static final int DEFAULT_INITIAL_WINDOW_SIZE = 64 * 1024;

  /** Peer request to clear durable settings. */
  public static final int FLAG_CLEAR_PREVIOUSLY_PERSISTED_SETTINGS = 0x1;

  /** Sent by servers only. The peer requests this setting persisted for future connections. */
  public static final int PERSIST_VALUE = 0x1;
  /** Sent by clients only. The client is reminding the server of a persisted value. */
  public static final int PERSISTED = 0x2;

  /** Sender's estimate of max incoming kbps. */
  public static final int UPLOAD_BANDWIDTH = 0x1;
  /** Sender's estimate of max outgoing kbps. */
  public static final int DOWNLOAD_BANDWIDTH = 0x2;
  /** Sender's estimate of milliseconds between sending a request and receiving a response. */
  public static final int ROUND_TRIP_TIME = 0x3;
  /** Sender's maximum number of concurrent streams. */
  public static final int MAX_CONCURRENT_STREAMS = 0x4;
  /** Current CWND in Packets. */
  public static final int CURRENT_CWND = 0x5;
  /** Retransmission rate. Percentage */
  public static final int DOWNLOAD_RETRANS_RATE = 0x6;
  /** Window size in bytes. */
  public static final int INITIAL_WINDOW_SIZE = 0x7;
  /** Window size in bytes. */
  public static final int CLIENT_CERTIFICATE_VECTOR_SIZE = 0x8;
  /** Total number of settings. */
  public static final int COUNT = 0x9;

  /** Bitfield of which flags that values. */
  private int set;

  /** Bitfield of flags that have {@link #PERSIST_VALUE}. */
  private int persistValue;

  /** Bitfield of flags that have {@link #PERSISTED}. */
  private int persisted;

  /** Flag values. */
  private final int[] values = new int[COUNT];

  public void set(int id, int idFlags, int value) {
    if (id >= values.length) {
      return; // Discard unknown settings.
    }

    int bit = 1 << id;
    set |= bit;
    if ((idFlags & PERSIST_VALUE) != 0) {
      persistValue |= bit;
    } else {
      persistValue &= ~bit;
    }
    if ((idFlags & PERSISTED) != 0) {
      persisted |= bit;
    } else {
      persisted &= ~bit;
    }

    values[id] = value;
  }

  /** Returns true if a value has been assigned for the setting {@code id}. */
  boolean isSet(int id) {
    int bit = 1 << id;
    return (set & bit) != 0;
  }

  /** Returns the value for the setting {@code id}, or 0 if unset. */
  public int get(int id) {
    return values[id];
  }

  /** Returns the flags for the setting {@code id}, or 0 if unset. */
  public int flags(int id) {
    int result = 0;
    if (isPersisted(id)) result |= Settings.PERSISTED;
    if (persistValue(id)) result |= Settings.PERSIST_VALUE;
    return result;
  }

  /** Returns the number of settings that have values assigned. */
  int size() {
    return Integer.bitCount(set);
  }

  public int getUploadBandwidth(int defaultValue) {
    int bit = 1 << UPLOAD_BANDWIDTH;
    return (bit & set) != 0 ? values[UPLOAD_BANDWIDTH] : defaultValue;
  }

  public int getDownloadBandwidth(int defaultValue) {
    int bit = 1 << DOWNLOAD_BANDWIDTH;
    return (bit & set) != 0 ? values[DOWNLOAD_BANDWIDTH] : defaultValue;
  }

  public int getRoundTripTime(int defaultValue) {
    int bit = 1 << ROUND_TRIP_TIME;
    return (bit & set) != 0 ? values[ROUND_TRIP_TIME] : defaultValue;
  }

  public int getMaxConcurrentStreams(int defaultValue) {
    int bit = 1 << MAX_CONCURRENT_STREAMS;
    return (bit & set) != 0 ? values[MAX_CONCURRENT_STREAMS] : defaultValue;
  }

  public int getCurrentCwnd(int defaultValue) {
    int bit = 1 << CURRENT_CWND;
    return (bit & set) != 0 ? values[CURRENT_CWND] : defaultValue;
  }

  public int getDownloadRetransRate(int defaultValue) {
    int bit = 1 << DOWNLOAD_RETRANS_RATE;
    return (bit & set) != 0 ? values[DOWNLOAD_RETRANS_RATE] : defaultValue;
  }

  public int getInitialWindowSize(int defaultValue) {
    int bit = 1 << INITIAL_WINDOW_SIZE;
    return (bit & set) != 0 ? values[INITIAL_WINDOW_SIZE] : defaultValue;
  }

  public int getClientCertificateVectorSize(int defaultValue) {
    int bit = 1 << CLIENT_CERTIFICATE_VECTOR_SIZE;
    return (bit & set) != 0 ? values[CLIENT_CERTIFICATE_VECTOR_SIZE] : defaultValue;
  }

  /**
   * Returns true if this user agent should use this setting in future SPDY
   * connections to the same host.
   */
  public boolean persistValue(int id) {
    int bit = 1 << id;
    return (persistValue & bit) != 0;
  }

  /** Returns true if this setting was persisted. */
  public boolean isPersisted(int id) {
    int bit = 1 << id;
    return (persisted & bit) != 0;
  }

  /**
   * Writes {@code other} into this. If any setting is populated by this and
   * {@code other}, the value and flags from {@code other} will be kept.
   */
  public void merge(Settings other) {
    for (int i = 0; i < COUNT; i++) {
      if (!other.isSet(i)) continue;
      set(i, other.flags(i), other.get(i));
    }
  }
}

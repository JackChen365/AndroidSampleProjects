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
package com.cz.android.exoplayer.upstream;

import com.cz.android.exoplayer.util.Assertions;
import com.cz.android.exoplayer.util.Clock;
import com.cz.android.exoplayer.util.SlidingPercentile;
import com.cz.android.exoplayer.util.SystemClock;

import android.os.Handler;

/**
 * Counts transferred bytes while transfers are open and creates a bandwidth sample and updated
 * bandwidth estimate each time a transfer ends.
 */
public class DefaultBandwidthMeter implements BandwidthMeter, TransferListener {

  /**
   * Interface definition for a callback to be notified of {@link DefaultBandwidthMeter} events.
   */
  public interface EventListener {

    /**
     * Invoked periodically to indicate that bytes have been transferred.
     *
     * @param elapsedMs The time taken to transfer the bytes, in milliseconds.
     * @param bytes The number of bytes transferred.
     * @param bandwidthEstimate The estimated bandwidth in bytes/sec, or {@link #NO_ESTIMATE} if no
     *     estimate is available. Note that this estimate is typically derived from more information
     *     than {@code bytes} and {@code elapsedMs}.
     */
    void onBandwidthSample(int elapsedMs, long bytes, long bandwidthEstimate);

  }

  private static final int DEFAULT_MAX_WEIGHT = 2000;

  private final Handler eventHandler;
  private final EventListener eventListener;
  private final Clock clock;
  private final SlidingPercentile slidingPercentile;

  private long accumulator;
  private long startTimeMs;
  private long bandwidthEstimate;
  private int streamCount;

  public DefaultBandwidthMeter() {
    this(null, null);
  }

  public DefaultBandwidthMeter(Handler eventHandler, EventListener eventListener) {
    this(eventHandler, eventListener, new SystemClock());
  }

  public DefaultBandwidthMeter(Handler eventHandler, EventListener eventListener, Clock clock) {
    this(eventHandler, eventListener, clock, DEFAULT_MAX_WEIGHT);
  }

  public DefaultBandwidthMeter(Handler eventHandler, EventListener eventListener, int maxWeight) {
    this(eventHandler, eventListener, new SystemClock(), maxWeight);
  }

  public DefaultBandwidthMeter(Handler eventHandler, EventListener eventListener, Clock clock,
      int maxWeight) {
    this.eventHandler = eventHandler;
    this.eventListener = eventListener;
    this.clock = clock;
    this.slidingPercentile = new SlidingPercentile(maxWeight);
    bandwidthEstimate = NO_ESTIMATE;
  }

  /**
   * Gets the estimated bandwidth.
   *
   * @return Estimated bandwidth in bytes/sec, or {@link #NO_ESTIMATE} if no estimate is available.
   */
  @Override
  public synchronized long getEstimate() {
    return bandwidthEstimate;
  }

  @Override
  public synchronized void onTransferStart() {
    if (streamCount == 0) {
      startTimeMs = clock.elapsedRealtime();
    }
    streamCount++;
  }

  @Override
  public synchronized void onBytesTransferred(int bytes) {
    accumulator += bytes;
  }

  @Override
  public synchronized void onTransferEnd() {
    Assertions.checkState(streamCount > 0);
    long nowMs = clock.elapsedRealtime();
    int elapsedMs = (int) (nowMs - startTimeMs);
    if (elapsedMs > 0) {
      float bytesPerSecond = accumulator * 1000 / elapsedMs;
      slidingPercentile.addSample(computeWeight(accumulator), bytesPerSecond);
      float bandwidthEstimateFloat = slidingPercentile.getPercentile(0.5f);
      bandwidthEstimate = bandwidthEstimateFloat == Float.NaN
          ? NO_ESTIMATE : (long) bandwidthEstimateFloat;
      notifyBandwidthSample(elapsedMs, accumulator, bandwidthEstimate);
    }
    streamCount--;
    if (streamCount > 0) {
      startTimeMs = nowMs;
    }
    accumulator = 0;
  }

  // TODO: Use media time (bytes / mediaRate) as weight.
  private int computeWeight(long mediaBytes) {
    return (int) Math.sqrt(mediaBytes);
  }

  private void notifyBandwidthSample(final int elapsedMs, final long bytes,
      final long bandwidthEstimate) {
    if (eventHandler != null && eventListener != null) {
      eventHandler.post(new Runnable()  {
        @Override
        public void run() {
          eventListener.onBandwidthSample(elapsedMs, bytes, bandwidthEstimate);
        }
      });
    }
  }

}

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

import java.io.IOException;
import java.util.PriorityQueue;

/**
 * A network task prioritization mechanism.
 * <p>
 * Manages different priority network tasks. A network task that wishes to have its priority
 * respected, and respect the priority of other tasks, should register itself with the lock prior
 * to making network requests. It should then call one of the lock's proceed methods frequently
 * during execution, so as to ensure that it continues only if it is the highest (or equally
 * highest) priority task.
 * <p>
 * Note that lower integer values correspond to higher priorities.
 */
public final class NetworkLock {

  /**
   * Thrown when a task is attempts to proceed when it does not have the highest priority.
   */
  public static class PriorityTooLowException extends IOException {

    public PriorityTooLowException(int priority, int highestPriority) {
      super("Priority too low [priority=" + priority + ", highest=" + highestPriority + "]");
    }

  }

  public static final NetworkLock instance = new NetworkLock();

  /**
   * Priority for network tasks associated with media streaming.
   */
  public static final int STREAMING_PRIORITY = 0;
  /**
   * Priority for network tasks associated with background downloads.
   */
  public static final int DOWNLOAD_PRIORITY = 10;

  private final PriorityQueue<Integer> queue;

  private NetworkLock() {
    queue = new PriorityQueue<Integer>();
  }

  /**
   * Blocks until the passed priority is the lowest one (i.e. highest priority).
   *
   * @param priority The priority of the task that would like to proceed.
   */
  public synchronized void proceed(int priority) throws InterruptedException {
    while (queue.peek() < priority) {
      wait();
    }
  }

  /**
   * A non-blocking variant of {@link #proceed(int)}.
   *
   * @param priority The priority of the task that would like to proceed.
   * @return Whether the passed priority is allowed to proceed.
   */
  public synchronized boolean proceedNonBlocking(int priority) {
    return queue.peek() >= priority;
  }

  /**
   * A throwing variant of {@link #proceed(int)}.
   *
   * @param priority The priority of the task that would like to proceed.
   * @throws PriorityTooLowException If the passed priority is not high enough to proceed.
   */
  public synchronized void proceedOrThrow(int priority) throws PriorityTooLowException {
    int highestPriority = queue.peek();
    if (highestPriority < priority) {
      throw new PriorityTooLowException(priority, highestPriority);
    }
  }

  /**
   * Register a new task.
   * <p>
   * The task must call {@link #remove(int)} when done.
   *
   * @param priority The priority of the task.
   */
  public synchronized void add(int priority) {
    queue.add(priority);
  }

  /**
   * Unregister a task.
   *
   * @param priority The priority of the task.
   */
  public synchronized void remove(int priority) {
    queue.remove(priority);
    notifyAll();
  }

}

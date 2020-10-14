package com.cz.android.simplehttp.request;

/**
 * This timeout uses a background thread to take action exactly when the timeout
 * occurs. Use this to implement timeouts where they aren't supported natively,
 * such as to sockets that are blocked on writing.
 *
 * <p>Subclasses should override {@link #timedOut} to take action when a timeout
 * occurs. This method will be invoked by the shared watchdog thread so it
 * should not do any long-running operations. Otherwise we risk starving other
 * timeouts from being triggered.
 *
 * <p>Callers should call {@link #enter} before doing work that is subject to
 * timeouts, and {@link #exit} afterwards. The return value of {@link #exit}
 * indicates whether a timeout was triggered. Note that the call to {@link
 * #timedOut} is asynchronous, and may be called after {@link #exit}.
 */
public class AsyncTimeout extends Timeout {
  /**
   * The watchdog thread processes a linked list of pending timeouts, sorted in
   * the order to be triggered. This class synchronizes on AsyncTimeout.class.
   * This lock guards the queue.
   *
   * <p>Head's 'next' points to the first element of the linked list. The first
   * element is the next node to time out, or null if the queue is empty. The
   * head is null until the watchdog thread is started.
   */
  private static AsyncTimeout head;

  /** True if this node is currently in the queue. */
  private boolean inQueue;

  /** The next node in the linked list. */
  private AsyncTimeout next;

  /** If scheduled, this is the time that the watchdog should time this out. */
  private long timeoutAt;

  public final void enter() {
    if (inQueue) throw new IllegalStateException("Unbalanced enter/exit");
    long timeoutNanos = timeoutNanos();
    boolean hasDeadline = hasDeadline();
    if (timeoutNanos == 0 && !hasDeadline) {
      return; // No timeout and no deadline? Don't bother with the queue.
    }
    inQueue = true;
    scheduleTimeout(this, timeoutNanos, hasDeadline);
  }

  private static synchronized void scheduleTimeout(
      AsyncTimeout node, long timeoutNanos, boolean hasDeadline) {
    // Start the watchdog thread and create the head node when the first timeout is scheduled.
    if (head == null) {
      head = new AsyncTimeout();
      new Watchdog().start();
    }

    long now = System.nanoTime();
    if (timeoutNanos != 0 && hasDeadline) {
      // Compute the earliest event; either timeout or deadline. Because nanoTime can wrap around,
      // Math.min() is undefined for absolute values, but meaningful for relative ones.
      node.timeoutAt = now + Math.min(timeoutNanos, node.deadlineNanoTime() - now);
    } else if (timeoutNanos != 0) {
      node.timeoutAt = now + timeoutNanos;
    } else if (hasDeadline) {
      node.timeoutAt = node.deadlineNanoTime();
    } else {
      throw new AssertionError();
    }

    // Insert the node in sorted order.
    long remainingNanos = node.remainingNanos(now);
    for (AsyncTimeout prev = head; true; prev = prev.next) {
      if (prev.next == null || remainingNanos < prev.next.remainingNanos(now)) {
        node.next = prev.next;
        prev.next = node;
        if (prev == head) {
          AsyncTimeout.class.notify(); // Wake up the watchdog when inserting at the front.
        }
        break;
      }
    }
  }

  /** Returns true if the timeout occurred. */
  public final boolean exit() {
    if (!inQueue) return false;
    inQueue = false;
    return cancelScheduledTimeout(this);
  }

  /** Returns true if the timeout occurred. */
  private static synchronized boolean cancelScheduledTimeout(AsyncTimeout node) {
    // Remove the node from the linked list.
    for (AsyncTimeout prev = head; prev != null; prev = prev.next) {
      if (prev.next == node) {
        prev.next = node.next;
        node.next = null;
        return false;
      }
    }

    // The node wasn't found in the linked list: it must have timed out!
    return true;
  }

  /**
   * Returns the amount of time left until the time out. This will be negative
   * if the timeout has elapsed and the timeout should occur immediately.
   */
  private long remainingNanos(long now) {
    return timeoutAt - now;
  }

  /**
   * Invoked by the watchdog thread when the time between calls to {@link
   * #enter()} and {@link #exit()} has exceeded the timeout.
   */
  protected void timedOut() {
  }

  private static final class Watchdog extends Thread {
    public Watchdog() {
      super("Okio Watchdog");
      setDaemon(true);
    }

    public void run() {
      while (true) {
        try {
          AsyncTimeout timedOut = awaitTimeout();

          // Didn't find a node to interrupt. Try again.
          if (timedOut == null) continue;

          // Close the timed out node.
          timedOut.timedOut();
        } catch (InterruptedException ignored) {
        }
      }
    }
  }

  /**
   * Removes and returns the node at the head of the list, waiting for it to
   * time out if necessary. Returns null if the situation changes while waiting:
   * either a newer node is inserted at the head, or the node being waited on
   * has been removed.
   */
  private static synchronized AsyncTimeout awaitTimeout() throws InterruptedException {
    // Get the next eligible node.
    AsyncTimeout node = head.next;

    // The queue is empty. Wait for something to be enqueued.
    if (node == null) {
      AsyncTimeout.class.wait();
      return null;
    }

    long waitNanos = node.remainingNanos(System.nanoTime());

    // The head of the queue hasn't timed out yet. Await that.
    if (waitNanos > 0) {
      // Waiting is made complicated by the fact that we work in nanoseconds,
      // but the API wants (millis, nanos) in two arguments.
      long waitMillis = waitNanos / 1000000L;
      waitNanos -= (waitMillis * 1000000L);
      AsyncTimeout.class.wait(waitMillis, (int) waitNanos);
      return null;
    }

    // The head of the queue has timed out. Remove it.
    head.next = node.next;
    node.next = null;
    return node;
  }
}

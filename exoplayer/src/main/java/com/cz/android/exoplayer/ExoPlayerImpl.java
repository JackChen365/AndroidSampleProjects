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
package com.cz.android.exoplayer;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Concrete implementation of {@link ExoPlayer}.
 */
/* package */ final class ExoPlayerImpl implements ExoPlayer {

  private static final String TAG = "ExoPlayerImpl";

  private final Handler eventHandler;
  private final ExoPlayerImplInternal internalPlayer;
  private final CopyOnWriteArraySet<Listener> listeners;
  private final boolean[] rendererEnabledFlags;

  private boolean playWhenReady;
  private int playbackState;
  private int pendingPlayWhenReadyAcks;

  /**
   * Constructs an instance. Must be invoked from a thread that has an associated {@link Looper}.
   *
   * @param rendererCount The number of {@link TrackRenderer}s that will be passed to
   *     {@link #prepare(TrackRenderer[])}.
   * @param minBufferMs A minimum duration of data that must be buffered for playback to start
   *     or resume following a user action such as a seek.
   * @param minRebufferMs A minimum duration of data that must be buffered for playback to resume
   *     after a player invoked rebuffer (i.e. a rebuffer that occurs due to buffer depletion, and
   *     not due to a user action such as starting playback or seeking).
   */
  @SuppressLint("HandlerLeak")
  public ExoPlayerImpl(int rendererCount, int minBufferMs, int minRebufferMs) {
    Log.i(TAG, "Init " + ExoPlayerLibraryInfo.VERSION);
    this.playbackState = STATE_IDLE;
    this.listeners = new CopyOnWriteArraySet<Listener>();
    this.rendererEnabledFlags = new boolean[rendererCount];
    for (int i = 0; i < rendererEnabledFlags.length; i++) {
      rendererEnabledFlags[i] = true;
    }
    eventHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        ExoPlayerImpl.this.handleEvent(msg);
      }
    };
    internalPlayer = new ExoPlayerImplInternal(eventHandler, playWhenReady, rendererEnabledFlags,
        minBufferMs, minRebufferMs);
  }

  @Override
  public Looper getPlaybackLooper() {
    return internalPlayer.getPlaybackLooper();
  }

  @Override
  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  @Override
  public int getPlaybackState() {
    return playbackState;
  }

  @Override
  public void prepare(TrackRenderer... renderers) {
    internalPlayer.prepare(renderers);
  }

  @Override
  public void setRendererEnabled(int index, boolean enabled) {
    if (rendererEnabledFlags[index] != enabled) {
      rendererEnabledFlags[index] = enabled;
      internalPlayer.setRendererEnabled(index, enabled);
    }
  }

  @Override
  public boolean getRendererEnabled(int index) {
    return rendererEnabledFlags[index];
  }

  @Override
  public void setPlayWhenReady(boolean playWhenReady) {
    if (this.playWhenReady != playWhenReady) {
      this.playWhenReady = playWhenReady;
      pendingPlayWhenReadyAcks++;
      internalPlayer.setPlayWhenReady(playWhenReady);
      for (Listener listener : listeners) {
        listener.onPlayerStateChanged(playWhenReady, playbackState);
      }
    }
  }

  @Override
  public boolean getPlayWhenReady() {
    return playWhenReady;
  }

  @Override
  public boolean isPlayWhenReadyCommitted() {
    return pendingPlayWhenReadyAcks == 0;
  }

  @Override
  public void seekTo(int positionMs) {
    internalPlayer.seekTo(positionMs);
  }

  @Override
  public void stop() {
    internalPlayer.stop();
  }

  @Override
  public void release() {
    internalPlayer.release();
    eventHandler.removeCallbacksAndMessages(null);
  }

  @Override
  public void sendMessage(ExoPlayerComponent target, int messageType, Object message) {
    internalPlayer.sendMessage(target, messageType, message);
  }

  @Override
  public void blockingSendMessage(ExoPlayerComponent target, int messageType, Object message) {
    internalPlayer.blockingSendMessage(target, messageType, message);
  }

  @Override
  public int getDuration() {
    return internalPlayer.getDuration();
  }

  @Override
  public int getCurrentPosition() {
    return internalPlayer.getCurrentPosition();
  }

  @Override
  public int getBufferedPosition() {
    return internalPlayer.getBufferedPosition();
  }

  @Override
  public int getBufferedPercentage() {
    int bufferedPosition = getBufferedPosition();
    int duration = getDuration();
    return bufferedPosition == ExoPlayer.UNKNOWN_TIME || duration == ExoPlayer.UNKNOWN_TIME ? 0
        : (duration == 0 ? 100 : (bufferedPosition * 100) / duration);
  }

  // Not private so it can be called from an inner class without going through a thunk method.
  /* package */ void handleEvent(Message msg) {
    switch (msg.what) {
      case ExoPlayerImplInternal.MSG_STATE_CHANGED: {
        playbackState = msg.arg1;
        for (Listener listener : listeners) {
          listener.onPlayerStateChanged(playWhenReady, playbackState);
        }
        break;
      }
      case ExoPlayerImplInternal.MSG_SET_PLAY_WHEN_READY_ACK: {
        pendingPlayWhenReadyAcks--;
        if (pendingPlayWhenReadyAcks == 0) {
          for (Listener listener : listeners) {
            listener.onPlayWhenReadyCommitted();
          }
        }
        break;
      }
      case ExoPlayerImplInternal.MSG_ERROR: {
        ExoPlaybackException exception = (ExoPlaybackException) msg.obj;
        for (Listener listener : listeners) {
          listener.onPlayerError(exception);
        }
        break;
      }
    }
  }

}

package com.cz.android.sample.player.full;

import com.cz.android.exoplayer.ExoPlayer;
import com.cz.android.exoplayer.MediaCodecAudioTrackRenderer.AudioTrackInitializationException;
import com.cz.android.exoplayer.MediaCodecTrackRenderer.DecoderInitializationException;
import com.cz.android.sample.player.full.player.DemoPlayer;
import com.cz.android.exoplayer.util.VerboseLogUtil;

import android.media.MediaCodec.CryptoException;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Logs player events using {@link Log}.
 */
public class EventLogger implements DemoPlayer.Listener, DemoPlayer.InfoListener,
    DemoPlayer.InternalErrorListener {

  private static final String TAG = "EventLogger";
  private static final NumberFormat TIME_FORMAT;
  static {
    TIME_FORMAT = NumberFormat.getInstance(Locale.US);
    TIME_FORMAT.setMinimumFractionDigits(2);
    TIME_FORMAT.setMaximumFractionDigits(2);
  }

  private long sessionStartTimeMs;
  private long[] loadStartTimeMs;

  public EventLogger() {
    loadStartTimeMs = new long[DemoPlayer.RENDERER_COUNT];
  }

  public void startSession() {
    sessionStartTimeMs = SystemClock.elapsedRealtime();
    Log.d(TAG, "start [0]");
  }

  public void endSession() {
    Log.d(TAG, "end [" + getSessionTimeString() + "]");
  }

  // DemoPlayer.Listener

  @Override
  public void onStateChanged(boolean playWhenReady, int state) {
    Log.d(TAG, "state [" + getSessionTimeString() + ", " + playWhenReady + ", " +
        getStateString(state) + "]");
  }

  @Override
  public void onError(Exception e) {
    Log.e(TAG, "playerFailed [" + getSessionTimeString() + "]", e);
  }

  @Override
  public void onVideoSizeChanged(int width, int height) {
    Log.d(TAG, "videoSizeChanged [" + width + ", " + height + "]");
  }

  // DemoPlayer.InfoListener

  @Override
  public void onBandwidthSample(int elapsedMs, long bytes, long bandwidthEstimate) {
    Log.d(TAG, "bandwidth [" + getSessionTimeString() + ", " + bytes +
        ", " + getTimeString(elapsedMs) + ", " + bandwidthEstimate + "]");
  }

  @Override
  public void onDroppedFrames(int count, long elapsed) {
    Log.d(TAG, "droppedFrames [" + getSessionTimeString() + ", " + count + "]");
  }

  @Override
  public void onLoadStarted(int sourceId, int formatId, int trigger, boolean isInitialization,
      int mediaStartTimeMs, int mediaEndTimeMs, long totalBytes) {
    loadStartTimeMs[sourceId] = SystemClock.elapsedRealtime();
    if (VerboseLogUtil.isTagEnabled(TAG)) {
      Log.v(TAG, "loadStart [" + getSessionTimeString() + ", " + sourceId
          + ", " + mediaStartTimeMs + ", " + mediaEndTimeMs + "]");
    }
  }

  @Override
  public void onLoadCompleted(int sourceId) {
    if (VerboseLogUtil.isTagEnabled(TAG)) {
      long downloadTime = SystemClock.elapsedRealtime() - loadStartTimeMs[sourceId];
      Log.v(TAG, "loadEnd [" + getSessionTimeString() + ", " + sourceId + ", " +
          downloadTime + "]");
    }
  }

  @Override
  public void onVideoFormatEnabled(int formatId, int trigger, int mediaTimeMs) {
    Log.d(TAG, "videoFormat [" + getSessionTimeString() + ", " + formatId + ", " +
        Integer.toString(trigger) + "]");
  }

  @Override
  public void onAudioFormatEnabled(int formatId, int trigger, int mediaTimeMs) {
    Log.d(TAG, "audioFormat [" + getSessionTimeString() + ", " + formatId + ", " +
        Integer.toString(trigger) + "]");
  }

  // DemoPlayer.InternalErrorListener

  @Override
  public void onUpstreamError(int sourceId, IOException e) {
    printInternalError("upstreamError", e);
  }

  @Override
  public void onConsumptionError(int sourceId, IOException e) {
    printInternalError("consumptionError", e);
  }

  @Override
  public void onRendererInitializationError(Exception e) {
    printInternalError("rendererInitError", e);
  }

  @Override
  public void onDrmSessionManagerError(Exception e) {
    printInternalError("drmSessionManagerError", e);
  }

  @Override
  public void onDecoderInitializationError(DecoderInitializationException e) {
    printInternalError("decoderInitializationError", e);
  }

  @Override
  public void onAudioTrackInitializationError(AudioTrackInitializationException e) {
    printInternalError("audioTrackInitializationError", e);
  }

  @Override
  public void onCryptoError(CryptoException e) {
    printInternalError("cryptoError", e);
  }

  private void printInternalError(String type, Exception e) {
    Log.e(TAG, "internalError [" + getSessionTimeString() + ", " + type + "]", e);
  }

  private String getStateString(int state) {
    switch (state) {
      case ExoPlayer.STATE_BUFFERING:
        return "B";
      case ExoPlayer.STATE_ENDED:
        return "E";
      case ExoPlayer.STATE_IDLE:
        return "I";
      case ExoPlayer.STATE_PREPARING:
        return "P";
      case ExoPlayer.STATE_READY:
        return "R";
      default:
        return "?";
    }
  }

  private String getSessionTimeString() {
    return getTimeString(SystemClock.elapsedRealtime() - sessionStartTimeMs);
  }

  private String getTimeString(long timeMs) {
    return TIME_FORMAT.format((timeMs) / 1000f);
  }

}

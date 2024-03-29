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
package com.cz.android.sample.player.full.player;

import com.cz.android.exoplayer.DefaultLoadControl;
import com.cz.android.exoplayer.LoadControl;
import com.cz.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.cz.android.exoplayer.MediaCodecUtil;
import com.cz.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.cz.android.exoplayer.TrackRenderer;
import com.cz.android.exoplayer.chunk.ChunkSampleSource;
import com.cz.android.exoplayer.chunk.ChunkSource;
import com.cz.android.exoplayer.chunk.FormatEvaluator;
import com.cz.android.exoplayer.chunk.FormatEvaluator.AdaptiveEvaluator;
import com.cz.android.exoplayer.chunk.MultiTrackChunkSource;
import com.cz.android.sample.player.full.player.DemoPlayer.RendererBuilder;
import com.cz.android.sample.player.full.player.DemoPlayer.RendererBuilderCallback;
import com.cz.android.exoplayer.drm.DrmSessionManager;
import com.cz.android.exoplayer.drm.MediaDrmCallback;
import com.cz.android.exoplayer.drm.StreamingDrmSessionManager;
import com.cz.android.exoplayer.smoothstreaming.SmoothStreamingChunkSource;
import com.cz.android.exoplayer.smoothstreaming.SmoothStreamingManifest;
import com.cz.android.exoplayer.smoothstreaming.SmoothStreamingManifest.StreamElement;
import com.cz.android.exoplayer.smoothstreaming.SmoothStreamingManifest.TrackElement;
import com.cz.android.exoplayer.smoothstreaming.SmoothStreamingManifestFetcher;
import com.cz.android.exoplayer.text.TextTrackRenderer;
import com.cz.android.exoplayer.text.ttml.TtmlParser;
import com.cz.android.exoplayer.upstream.BufferPool;
import com.cz.android.exoplayer.upstream.DataSource;
import com.cz.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.cz.android.exoplayer.upstream.HttpDataSource;
import com.cz.android.exoplayer.util.ManifestFetcher.ManifestCallback;
import com.cz.android.exoplayer.util.Util;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.UnsupportedSchemeException;
import android.os.Handler;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;

/**
 * A {@link RendererBuilder} for SmoothStreaming.
 */
public class SmoothStreamingRendererBuilder implements RendererBuilder,
    ManifestCallback<SmoothStreamingManifest> {

  private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
  private static final int VIDEO_BUFFER_SEGMENTS = 200;
  private static final int AUDIO_BUFFER_SEGMENTS = 60;
  private static final int TTML_BUFFER_SEGMENTS = 2;

  private final String userAgent;
  private final String url;
  private final String contentId;
  private final MediaDrmCallback drmCallback;
  private final TextView debugTextView;

  private DemoPlayer player;
  private RendererBuilderCallback callback;

  public SmoothStreamingRendererBuilder(String userAgent, String url, String contentId,
      MediaDrmCallback drmCallback, TextView debugTextView) {
    this.userAgent = userAgent;
    this.url = url;
    this.contentId = contentId;
    this.drmCallback = drmCallback;
    this.debugTextView = debugTextView;
  }

  @Override
  public void buildRenderers(DemoPlayer player, RendererBuilderCallback callback) {
    this.player = player;
    this.callback = callback;
    SmoothStreamingManifestFetcher mpdFetcher = new SmoothStreamingManifestFetcher(this);
    mpdFetcher.execute(url + "/Manifest", contentId);
  }

  @Override
  public void onManifestError(String contentId, Exception e) {
    callback.onRenderersError(e);
  }

  @Override
  public void onManifest(String contentId, SmoothStreamingManifest manifest) {
    Handler mainHandler = player.getMainHandler();
    LoadControl loadControl = new DefaultLoadControl(new BufferPool(BUFFER_SEGMENT_SIZE));
    DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(mainHandler, player);

    // Check drm support if necessary.
    DrmSessionManager drmSessionManager = null;
    if (manifest.protectionElement != null) {
      if (Util.SDK_INT < 18) {
        callback.onRenderersError(new UnsupportedOperationException(
            "Protected content not supported on API level " + Util.SDK_INT));
        return;
      }
      try {
        drmSessionManager = V18Compat.getDrmSessionManager(manifest.protectionElement.uuid, player,
            drmCallback);
      } catch (UnsupportedSchemeException e) {
        callback.onRenderersError(e);
        return;
      }
    }

    // Obtain stream elements for playback.
    int maxDecodableFrameSize = MediaCodecUtil.maxH264DecodableFrameSize();
    int audioStreamElementCount = 0;
    int textStreamElementCount = 0;
    int videoStreamElementIndex = -1;
    ArrayList<Integer> videoTrackIndexList = new ArrayList<Integer>();
    for (int i = 0; i < manifest.streamElements.length; i++) {
      if (manifest.streamElements[i].type == StreamElement.TYPE_AUDIO) {
        audioStreamElementCount++;
      } else if (manifest.streamElements[i].type == StreamElement.TYPE_TEXT) {
        textStreamElementCount++;
      } else if (videoStreamElementIndex == -1
          && manifest.streamElements[i].type == StreamElement.TYPE_VIDEO) {
        videoStreamElementIndex = i;
        StreamElement streamElement = manifest.streamElements[i];
        for (int j = 0; j < streamElement.tracks.length; j++) {
          TrackElement trackElement = streamElement.tracks[j];
          if (trackElement.maxWidth * trackElement.maxHeight <= maxDecodableFrameSize) {
            videoTrackIndexList.add(j);
          } else {
            // The device isn't capable of playing this stream.
          }
        }
      }
    }
    int[] videoTrackIndices = new int[videoTrackIndexList.size()];
    for (int i = 0; i < videoTrackIndexList.size(); i++) {
      videoTrackIndices[i] = videoTrackIndexList.get(i);
    }

    // Build the video renderer.
    DataSource videoDataSource = new HttpDataSource(userAgent, HttpDataSource.REJECT_PAYWALL_TYPES,
        bandwidthMeter);
    ChunkSource videoChunkSource = new SmoothStreamingChunkSource(url, manifest,
        videoStreamElementIndex, videoTrackIndices, videoDataSource,
        new AdaptiveEvaluator(bandwidthMeter));
    ChunkSampleSource videoSampleSource = new ChunkSampleSource(videoChunkSource, loadControl,
        VIDEO_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, true, mainHandler, player,
        DemoPlayer.TYPE_VIDEO);
    MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(videoSampleSource,
        drmSessionManager, true, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 5000,
        mainHandler, player, 50);

    // Build the audio renderer.
    final String[] audioTrackNames;
    final MultiTrackChunkSource audioChunkSource;
    final MediaCodecAudioTrackRenderer audioRenderer;
    if (audioStreamElementCount == 0) {
      audioTrackNames = null;
      audioChunkSource = null;
      audioRenderer = null;
    } else {
      audioTrackNames = new String[audioStreamElementCount];
      ChunkSource[] audioChunkSources = new ChunkSource[audioStreamElementCount];
      DataSource audioDataSource = new HttpDataSource(userAgent,
          HttpDataSource.REJECT_PAYWALL_TYPES, bandwidthMeter);
      FormatEvaluator audioFormatEvaluator = new FormatEvaluator.FixedEvaluator();
      audioStreamElementCount = 0;
      for (int i = 0; i < manifest.streamElements.length; i++) {
        if (manifest.streamElements[i].type == StreamElement.TYPE_AUDIO) {
          audioTrackNames[audioStreamElementCount] = manifest.streamElements[i].name;
          audioChunkSources[audioStreamElementCount] = new SmoothStreamingChunkSource(url, manifest,
              i, new int[] {0}, audioDataSource, audioFormatEvaluator);
          audioStreamElementCount++;
        }
      }
      audioChunkSource = new MultiTrackChunkSource(audioChunkSources);
      ChunkSampleSource audioSampleSource = new ChunkSampleSource(audioChunkSource, loadControl,
          AUDIO_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, true, mainHandler, player,
          DemoPlayer.TYPE_AUDIO);
      audioRenderer = new MediaCodecAudioTrackRenderer(audioSampleSource, drmSessionManager, true,
          mainHandler, player);
    }

    // Build the text renderer.
    final String[] textTrackNames;
    final MultiTrackChunkSource textChunkSource;
    final TrackRenderer textRenderer;
    if (textStreamElementCount == 0) {
      textTrackNames = null;
      textChunkSource = null;
      textRenderer = null;
    } else {
      textTrackNames = new String[textStreamElementCount];
      ChunkSource[] textChunkSources = new ChunkSource[textStreamElementCount];
      DataSource ttmlDataSource = new HttpDataSource(userAgent, HttpDataSource.REJECT_PAYWALL_TYPES,
          bandwidthMeter);
      FormatEvaluator ttmlFormatEvaluator = new FormatEvaluator.FixedEvaluator();
      textStreamElementCount = 0;
      for (int i = 0; i < manifest.streamElements.length; i++) {
        if (manifest.streamElements[i].type == StreamElement.TYPE_TEXT) {
          textTrackNames[textStreamElementCount] = manifest.streamElements[i].language;
          textChunkSources[textStreamElementCount] = new SmoothStreamingChunkSource(url, manifest,
              i, new int[] {0}, ttmlDataSource, ttmlFormatEvaluator);
          textStreamElementCount++;
        }
      }
      textChunkSource = new MultiTrackChunkSource(textChunkSources);
      ChunkSampleSource ttmlSampleSource = new ChunkSampleSource(textChunkSource, loadControl,
          TTML_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, true, mainHandler, player,
          DemoPlayer.TYPE_TEXT);
      textRenderer = new TextTrackRenderer(ttmlSampleSource, new TtmlParser(), player,
          mainHandler.getLooper());
    }

    // Build the debug renderer.
    TrackRenderer debugRenderer = debugTextView != null
        ? new DebugTrackRenderer(debugTextView, videoRenderer, videoSampleSource)
        : null;

    // Invoke the callback.
    String[][] trackNames = new String[DemoPlayer.RENDERER_COUNT][];
    trackNames[DemoPlayer.TYPE_AUDIO] = audioTrackNames;
    trackNames[DemoPlayer.TYPE_TEXT] = textTrackNames;

    MultiTrackChunkSource[] multiTrackChunkSources =
        new MultiTrackChunkSource[DemoPlayer.RENDERER_COUNT];
    multiTrackChunkSources[DemoPlayer.TYPE_AUDIO] = audioChunkSource;
    multiTrackChunkSources[DemoPlayer.TYPE_TEXT] = textChunkSource;

    TrackRenderer[] renderers = new TrackRenderer[DemoPlayer.RENDERER_COUNT];
    renderers[DemoPlayer.TYPE_VIDEO] = videoRenderer;
    renderers[DemoPlayer.TYPE_AUDIO] = audioRenderer;
    renderers[DemoPlayer.TYPE_TEXT] = textRenderer;
    renderers[DemoPlayer.TYPE_DEBUG] = debugRenderer;
    callback.onRenderers(trackNames, multiTrackChunkSources, renderers);
  }

  @TargetApi(18)
  private static class V18Compat {

    public static DrmSessionManager getDrmSessionManager(UUID uuid, DemoPlayer player,
        MediaDrmCallback drmCallback) throws UnsupportedSchemeException {
      return new StreamingDrmSessionManager(uuid, player.getPlaybackLooper(), drmCallback,
          player.getMainHandler(), player);
    }

  }

}

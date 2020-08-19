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
package com.cz.android.sample.player.simple;

import com.cz.android.exoplayer.DefaultLoadControl;
import com.cz.android.exoplayer.LoadControl;
import com.cz.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.cz.android.exoplayer.MediaCodecUtil;
import com.cz.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.cz.android.exoplayer.SampleSource;
import com.cz.android.exoplayer.chunk.ChunkSampleSource;
import com.cz.android.exoplayer.chunk.ChunkSource;
import com.cz.android.exoplayer.chunk.Format;
import com.cz.android.exoplayer.chunk.FormatEvaluator;
import com.cz.android.exoplayer.chunk.FormatEvaluator.AdaptiveEvaluator;
import com.cz.android.exoplayer.dash.DashMp4ChunkSource;
import com.cz.android.exoplayer.dash.mpd.AdaptationSet;
import com.cz.android.exoplayer.dash.mpd.MediaPresentationDescription;
import com.cz.android.exoplayer.dash.mpd.MediaPresentationDescriptionFetcher;
import com.cz.android.exoplayer.dash.mpd.Period;
import com.cz.android.exoplayer.dash.mpd.Representation;
import com.cz.android.sample.player.simple.SimplePlayerActivity.RendererBuilder;
import com.cz.android.sample.player.simple.SimplePlayerActivity.RendererBuilderCallback;
import com.cz.android.exoplayer.upstream.BufferPool;
import com.cz.android.exoplayer.upstream.DataSource;
import com.cz.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.cz.android.exoplayer.upstream.HttpDataSource;
import com.cz.android.exoplayer.util.ManifestFetcher.ManifestCallback;

import android.media.MediaCodec;
import android.os.AsyncTask;
import android.os.Handler;

import java.util.ArrayList;

/**
 * A {@link RendererBuilder} for DASH VOD.
 */
/* package */ class DashVodRendererBuilder implements RendererBuilder,
    ManifestCallback<MediaPresentationDescription> {

  private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
  private static final int VIDEO_BUFFER_SEGMENTS = 200;
  private static final int AUDIO_BUFFER_SEGMENTS = 60;

  private final SimplePlayerActivity playerActivity;
  private final String userAgent;
  private final String url;
  private final String contentId;

  private RendererBuilderCallback callback;

  public DashVodRendererBuilder(SimplePlayerActivity playerActivity, String userAgent, String url,
      String contentId) {
    this.playerActivity = playerActivity;
    this.userAgent = userAgent;
    this.url = url;
    this.contentId = contentId;
  }

  @Override
  public void buildRenderers(RendererBuilderCallback callback) {
    this.callback = callback;
    MediaPresentationDescriptionFetcher mpdFetcher = new MediaPresentationDescriptionFetcher(this);
    mpdFetcher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, contentId);
  }

  @Override
  public void onManifestError(String contentId, Exception e) {
    callback.onRenderersError(e);
  }

  @Override
  public void onManifest(String contentId, MediaPresentationDescription manifest) {
    Handler mainHandler = playerActivity.getMainHandler();
    LoadControl loadControl = new DefaultLoadControl(new BufferPool(BUFFER_SEGMENT_SIZE));
    DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

    // Obtain Representations for playback.
    int maxDecodableFrameSize = MediaCodecUtil.maxH264DecodableFrameSize();
    Representation audioRepresentation = null;
    ArrayList<Representation> videoRepresentationsList = new ArrayList<Representation>();
    Period period = manifest.periods.get(0);
    for (int i = 0; i < period.adaptationSets.size(); i++) {
      AdaptationSet adaptationSet = period.adaptationSets.get(i);
      int adaptationSetType = adaptationSet.type;
      for (int j = 0; j < adaptationSet.representations.size(); j++) {
        Representation representation = adaptationSet.representations.get(j);
        if (audioRepresentation == null && adaptationSetType == AdaptationSet.TYPE_AUDIO) {
          audioRepresentation = representation;
        } else if (adaptationSetType == AdaptationSet.TYPE_VIDEO) {
          Format format = representation.format;
          if (format.width * format.height <= maxDecodableFrameSize) {
            videoRepresentationsList.add(representation);
          } else {
            // The device isn't capable of playing this stream.
          }
        }
      }
    }
    Representation[] videoRepresentations = new Representation[videoRepresentationsList.size()];
    videoRepresentationsList.toArray(videoRepresentations);

    // Build the video renderer.
    DataSource videoDataSource = new HttpDataSource(userAgent, HttpDataSource.REJECT_PAYWALL_TYPES,
        bandwidthMeter);
    ChunkSource videoChunkSource = new DashMp4ChunkSource(videoDataSource,
        new AdaptiveEvaluator(bandwidthMeter), videoRepresentations);
    ChunkSampleSource videoSampleSource = new ChunkSampleSource(videoChunkSource, loadControl,
        VIDEO_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, true);
    MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(videoSampleSource,
        MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 0, mainHandler, playerActivity, 50);

    // Build the audio renderer.
    DataSource audioDataSource = new HttpDataSource(userAgent, HttpDataSource.REJECT_PAYWALL_TYPES,
        bandwidthMeter);
    ChunkSource audioChunkSource = new DashMp4ChunkSource(audioDataSource,
        new FormatEvaluator.FixedEvaluator(), audioRepresentation);
    SampleSource audioSampleSource = new ChunkSampleSource(audioChunkSource, loadControl,
        AUDIO_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, true);
    MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(
        audioSampleSource);
    callback.onRenderers(videoRenderer, audioRenderer);
  }

}

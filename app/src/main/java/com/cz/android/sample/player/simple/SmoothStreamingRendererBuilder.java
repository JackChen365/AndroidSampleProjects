package com.cz.android.sample.player.simple;

import com.cz.android.exoplayer.DefaultLoadControl;
import com.cz.android.exoplayer.LoadControl;
import com.cz.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.cz.android.exoplayer.MediaCodecUtil;
import com.cz.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.cz.android.exoplayer.SampleSource;
import com.cz.android.exoplayer.chunk.ChunkSampleSource;
import com.cz.android.exoplayer.chunk.ChunkSource;
import com.cz.android.exoplayer.chunk.FormatEvaluator;
import com.cz.android.exoplayer.chunk.FormatEvaluator.AdaptiveEvaluator;
import com.cz.android.sample.player.simple.SimplePlayerActivity.RendererBuilder;
import com.cz.android.sample.player.simple.SimplePlayerActivity.RendererBuilderCallback;
import com.cz.android.exoplayer.smoothstreaming.SmoothStreamingChunkSource;
import com.cz.android.exoplayer.smoothstreaming.SmoothStreamingManifest;
import com.cz.android.exoplayer.smoothstreaming.SmoothStreamingManifest.StreamElement;
import com.cz.android.exoplayer.smoothstreaming.SmoothStreamingManifest.TrackElement;
import com.cz.android.exoplayer.smoothstreaming.SmoothStreamingManifestFetcher;
import com.cz.android.exoplayer.upstream.BufferPool;
import com.cz.android.exoplayer.upstream.DataSource;
import com.cz.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.cz.android.exoplayer.upstream.HttpDataSource;
import com.cz.android.exoplayer.util.ManifestFetcher.ManifestCallback;

import android.media.MediaCodec;
import android.os.Handler;

import java.util.ArrayList;

/**
 * A {@link RendererBuilder} for SmoothStreaming.
 */
class SmoothStreamingRendererBuilder implements RendererBuilder,
    ManifestCallback<SmoothStreamingManifest> {

  private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
  private static final int VIDEO_BUFFER_SEGMENTS = 200;
  private static final int AUDIO_BUFFER_SEGMENTS = 60;

  private final SimplePlayerActivity playerActivity;
  private final String userAgent;
  private final String url;
  private final String contentId;

  private RendererBuilderCallback callback;

  public SmoothStreamingRendererBuilder(SimplePlayerActivity playerActivity, String userAgent,
      String url, String contentId) {
    this.playerActivity = playerActivity;
    this.userAgent = userAgent;
    this.url = url;
    this.contentId = contentId;
  }

  @Override
  public void buildRenderers(RendererBuilderCallback callback) {
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
    Handler mainHandler = playerActivity.getMainHandler();
    LoadControl loadControl = new DefaultLoadControl(new BufferPool(BUFFER_SEGMENT_SIZE));
    DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

    // Obtain stream elements for playback.
    int maxDecodableFrameSize = MediaCodecUtil.maxH264DecodableFrameSize();
    int audioStreamElementIndex = -1;
    int videoStreamElementIndex = -1;
    ArrayList<Integer> videoTrackIndexList = new ArrayList<Integer>();
    for (int i = 0; i < manifest.streamElements.length; i++) {
      if (audioStreamElementIndex == -1
          && manifest.streamElements[i].type == StreamElement.TYPE_AUDIO) {
        audioStreamElementIndex = i;
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
        VIDEO_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, true);
    MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(videoSampleSource,
        MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 0, mainHandler, playerActivity, 50);

    // Build the audio renderer.
    DataSource audioDataSource = new HttpDataSource(userAgent, HttpDataSource.REJECT_PAYWALL_TYPES,
        bandwidthMeter);
    ChunkSource audioChunkSource = new SmoothStreamingChunkSource(url, manifest,
        audioStreamElementIndex, new int[] {0}, audioDataSource,
        new FormatEvaluator.FixedEvaluator());
    SampleSource audioSampleSource = new ChunkSampleSource(audioChunkSource, loadControl,
        AUDIO_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, true);
    MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(
        audioSampleSource);
    callback.onRenderers(videoRenderer, audioRenderer);
  }

}

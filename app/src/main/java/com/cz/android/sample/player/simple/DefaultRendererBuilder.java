package com.cz.android.sample.player.simple;

import com.cz.android.exoplayer.FrameworkSampleSource;
import com.cz.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.cz.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.cz.android.sample.player.simple.SimplePlayerActivity.RendererBuilder;
import com.cz.android.sample.player.simple.SimplePlayerActivity.RendererBuilderCallback;

import android.media.MediaCodec;
import android.net.Uri;

/**
 * A {@link RendererBuilder} for streams that can be read using
 * {@link android.media.MediaExtractor}.
 */
/* package */ class DefaultRendererBuilder implements RendererBuilder {

  private final SimplePlayerActivity playerActivity;
  private final Uri uri;

  public DefaultRendererBuilder(SimplePlayerActivity playerActivity, Uri uri) {
    this.playerActivity = playerActivity;
    this.uri = uri;
  }

  @Override
  public void buildRenderers(RendererBuilderCallback callback) {
    // Build the video and audio renderers.
    FrameworkSampleSource sampleSource = new FrameworkSampleSource(playerActivity, uri, null, 2);
    MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(sampleSource,
        MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 0, playerActivity.getMainHandler(),
        playerActivity, 50);
    MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource);

    // Invoke the callback.
    callback.onRenderers(videoRenderer, audioRenderer);
  }

}

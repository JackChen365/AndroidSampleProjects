package com.cz.android.sample.player.full;

import com.cz.android.exoplayer.ExoPlayer;
import com.cz.android.exoplayer.VideoSurfaceView;
import com.cz.android.sample.R;
import com.cz.android.sample.player.DemoUtil;
import com.cz.android.sample.player.full.player.DashVodRendererBuilder;
import com.cz.android.sample.player.full.player.DefaultRendererBuilder;
import com.cz.android.sample.player.full.player.DemoPlayer;
import com.cz.android.sample.player.full.player.DemoPlayer.RendererBuilder;
import com.cz.android.sample.player.full.player.SmoothStreamingRendererBuilder;
import com.cz.android.exoplayer.util.VerboseLogUtil;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

/**
 * An activity that plays media using {@link DemoPlayer}.
 */
public class FullPlayerActivity extends Activity implements SurfaceHolder.Callback, OnClickListener,
    DemoPlayer.Listener, DemoPlayer.TextListener {

  private static final int MENU_GROUP_TRACKS = 1;
  private static final int ID_OFFSET = 2;

  private EventLogger eventLogger;
  private MediaController mediaController;
  private View debugRootView;
  private View shutterView;
  private VideoSurfaceView surfaceView;
  private TextView debugTextView;
  private TextView playerStateTextView;
  private TextView subtitlesTextView;
  private Button videoButton;
  private Button audioButton;
  private Button textButton;
  private Button retryButton;

  private DemoPlayer player;
  private boolean playerNeedsPrepare;

  private boolean autoPlay = true;
  private int playerPosition;
  private boolean enableBackgroundAudio = false;

  private Uri contentUri;
  private int contentType;
  private String contentId;

  // Activity lifecycle

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    contentUri = intent.getData();
    contentType = intent.getIntExtra(DemoUtil.CONTENT_TYPE_EXTRA, DemoUtil.TYPE_OTHER);
    contentId = intent.getStringExtra(DemoUtil.CONTENT_ID_EXTRA);

    setContentView(R.layout.activity_player_full);
    View root = findViewById(R.id.root);
    root.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View arg0, MotionEvent arg1) {
        if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
          toggleControlsVisibility();
        }
        return true;
      }
    });

    shutterView = findViewById(R.id.shutter);
    debugRootView = findViewById(R.id.controls_root);

    surfaceView = (VideoSurfaceView) findViewById(R.id.surface_view);
    surfaceView.getHolder().addCallback(this);
    debugTextView = (TextView) findViewById(R.id.debug_text_view);

    playerStateTextView = (TextView) findViewById(R.id.player_state_view);
    subtitlesTextView = (TextView) findViewById(R.id.subtitles);

    mediaController = new MediaController(this);
    mediaController.setAnchorView(root);
    retryButton = (Button) findViewById(R.id.retry_button);
    retryButton.setOnClickListener(this);
    videoButton = (Button) findViewById(R.id.video_controls);
    audioButton = (Button) findViewById(R.id.audio_controls);
    textButton = (Button) findViewById(R.id.text_controls);
  }

  @Override
  public void onResume() {
    super.onResume();
    preparePlayer();
  }

  @Override
  public void onPause() {
    super.onPause();
    if (!enableBackgroundAudio) {
      releasePlayer();
    } else {
      player.blockingClearSurface();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    releasePlayer();
  }

  // OnClickListener methods

  @Override
  public void onClick(View view) {
    if (view == retryButton) {
      autoPlay = true;
      preparePlayer();
    }
  }

  // Internal methods

  private RendererBuilder getRendererBuilder() {
    String userAgent = DemoUtil.getUserAgent(this);
    switch (contentType) {
      case DemoUtil.TYPE_SS_VOD:
        return new SmoothStreamingRendererBuilder(userAgent, contentUri.toString(), contentId,
            new SmoothStreamingTestMediaDrmCallback(), debugTextView);
      case DemoUtil.TYPE_DASH_VOD:
        return new DashVodRendererBuilder(userAgent, contentUri.toString(), contentId,
            new WidevineTestMediaDrmCallback(contentId), debugTextView);
      default:
        return new DefaultRendererBuilder(this, contentUri, debugTextView);
    }
  }

  private void preparePlayer() {
    if (player == null) {
      player = new DemoPlayer(getRendererBuilder());
      player.addListener(this);
      player.setTextListener(this);
      player.seekTo(playerPosition);
      playerNeedsPrepare = true;
      mediaController.setMediaPlayer(player.getPlayerControl());
      mediaController.setEnabled(true);
      eventLogger = new EventLogger();
      eventLogger.startSession();
      player.addListener(eventLogger);
      player.setInfoListener(eventLogger);
      player.setInternalErrorListener(eventLogger);
    }
    if (playerNeedsPrepare) {
      player.prepare();
      playerNeedsPrepare = false;
      updateButtonVisibilities();
    }
    player.setSurface(surfaceView.getHolder().getSurface());
    maybeStartPlayback();
  }

  private void maybeStartPlayback() {
    if (autoPlay && (player.getSurface().isValid()
        || player.getSelectedTrackIndex(DemoPlayer.TYPE_VIDEO) == DemoPlayer.DISABLED_TRACK)) {
      player.setPlayWhenReady(true);
      autoPlay = false;
    }
  }

  private void releasePlayer() {
    if (player != null) {
      playerPosition = player.getCurrentPosition();
      player.release();
      player = null;
      eventLogger.endSession();
      eventLogger = null;
    }
  }

  // DemoPlayer.Listener implementation

  @Override
  public void onStateChanged(boolean playWhenReady, int playbackState) {
    if (playbackState == ExoPlayer.STATE_ENDED) {
      showControls();
    }
    String text = "playWhenReady=" + playWhenReady + ", playbackState=";
    switch(playbackState) {
      case ExoPlayer.STATE_BUFFERING:
        text += "buffering";
        break;
      case ExoPlayer.STATE_ENDED:
        text += "ended";
        break;
      case ExoPlayer.STATE_IDLE:
        text += "idle";
        break;
      case ExoPlayer.STATE_PREPARING:
        text += "preparing";
        break;
      case ExoPlayer.STATE_READY:
        text += "ready";
        break;
      default:
        text += "unknown";
        break;
    }
    playerStateTextView.setText(text);
    updateButtonVisibilities();
  }

  @Override
  public void onError(Exception e) {
    playerNeedsPrepare = true;
    updateButtonVisibilities();
    showControls();
  }

  @Override
  public void onVideoSizeChanged(int width, int height) {
    shutterView.setVisibility(View.GONE);
    surfaceView.setVideoWidthHeightRatio(height == 0 ? 1 : (float) width / height);
  }

  // User controls

  private void updateButtonVisibilities() {
    retryButton.setVisibility(playerNeedsPrepare ? View.VISIBLE : View.GONE);
    videoButton.setVisibility(haveTracks(DemoPlayer.TYPE_VIDEO) ? View.VISIBLE : View.GONE);
    audioButton.setVisibility(haveTracks(DemoPlayer.TYPE_AUDIO) ? View.VISIBLE : View.GONE);
    textButton.setVisibility(haveTracks(DemoPlayer.TYPE_TEXT) ? View.VISIBLE : View.GONE);
  }

  private boolean haveTracks(int type) {
    return player != null && player.getTracks(type) != null;
  }

  public void showVideoPopup(View v) {
    PopupMenu popup = new PopupMenu(this, v);
    configurePopupWithTracks(popup, null, DemoPlayer.TYPE_VIDEO);
    popup.show();
  }

  public void showAudioPopup(View v) {
    PopupMenu popup = new PopupMenu(this, v);
    Menu menu = popup.getMenu();
    menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.enable_background_audio);
    final MenuItem backgroundAudioItem = menu.findItem(0);
    backgroundAudioItem.setCheckable(true);
    backgroundAudioItem.setChecked(enableBackgroundAudio);
    OnMenuItemClickListener clickListener = new OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        if (item == backgroundAudioItem) {
          enableBackgroundAudio = !item.isChecked();
          return true;
        }
        return false;
      }
    };
    configurePopupWithTracks(popup, clickListener, DemoPlayer.TYPE_AUDIO);
    popup.show();
  }

  public void showTextPopup(View v) {
    PopupMenu popup = new PopupMenu(this, v);
    configurePopupWithTracks(popup, null, DemoPlayer.TYPE_TEXT);
    popup.show();
  }

  public void showVerboseLogPopup(View v) {
    PopupMenu popup = new PopupMenu(this, v);
    Menu menu = popup.getMenu();
    menu.add(Menu.NONE, 0, Menu.NONE, R.string.logging_normal);
    menu.add(Menu.NONE, 1, Menu.NONE, R.string.logging_verbose);
    menu.setGroupCheckable(Menu.NONE, true, true);
    menu.findItem((VerboseLogUtil.areAllTagsEnabled()) ? 1 : 0).setChecked(true);
    popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == 0) {
          VerboseLogUtil.setEnableAllTags(false);
        } else {
          VerboseLogUtil.setEnableAllTags(true);
        }
        return true;
      }
    });
    popup.show();
  }

  private void configurePopupWithTracks(PopupMenu popup,
      final OnMenuItemClickListener customActionClickListener,
      final int trackType) {
    if (player == null) {
      return;
    }
    String[] tracks = player.getTracks(trackType);
    if (tracks == null) {
      return;
    }
    popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        return (customActionClickListener != null
            && customActionClickListener.onMenuItemClick(item))
            || onTrackItemClick(item, trackType);
      }
    });
    Menu menu = popup.getMenu();
    // ID_OFFSET ensures we avoid clashing with Menu.NONE (which equals 0)
    menu.add(MENU_GROUP_TRACKS, DemoPlayer.DISABLED_TRACK + ID_OFFSET, Menu.NONE, R.string.off);
    if (tracks.length == 1 && TextUtils.isEmpty(tracks[0])) {
      menu.add(MENU_GROUP_TRACKS, DemoPlayer.PRIMARY_TRACK + ID_OFFSET, Menu.NONE, R.string.on);
    } else {
      for (int i = 0; i < tracks.length; i++) {
        menu.add(MENU_GROUP_TRACKS, i + ID_OFFSET, Menu.NONE, tracks[i]);
      }
    }
    menu.setGroupCheckable(MENU_GROUP_TRACKS, true, true);
    menu.findItem(player.getSelectedTrackIndex(trackType) + ID_OFFSET).setChecked(true);
  }

  private boolean onTrackItemClick(MenuItem item, int type) {
    if (player == null || item.getGroupId() != MENU_GROUP_TRACKS) {
      return false;
    }
    player.selectTrack(type, item.getItemId() - ID_OFFSET);
    return true;
  }

  private void toggleControlsVisibility()  {
    if (mediaController.isShowing()) {
      mediaController.hide();
      debugRootView.setVisibility(View.GONE);
    } else {
      showControls();
    }
  }

  private void showControls() {
    mediaController.show(0);
    debugRootView.setVisibility(View.VISIBLE);
  }

  // DemoPlayer.TextListener implementation

  @Override
  public void onText(String text) {
    if (TextUtils.isEmpty(text)) {
      subtitlesTextView.setVisibility(View.INVISIBLE);
    } else {
      subtitlesTextView.setVisibility(View.VISIBLE);
      subtitlesTextView.setText(text);
    }
  }

  // SurfaceHolder.Callback implementation

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    if (player != null) {
      player.setSurface(holder.getSurface());
      maybeStartPlayback();
    }
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    // Do nothing.
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    if (player != null) {
      player.blockingClearSurface();
    }
  }

}

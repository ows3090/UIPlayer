package ows.boostcourse.uiplayer2;


import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.analytics.AnalyticsCollector;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.util.Clock;

public class SimpleUIPlayer extends SimpleExoPlayer {


    public SimpleUIPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        super(renderersFactory, trackSelector, loadControl, drmSessionManager);
    }

    public SimpleUIPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, AnalyticsCollector.Factory analyticsCollectorFactory) {
        super(renderersFactory, trackSelector, loadControl, drmSessionManager, analyticsCollectorFactory);
    }

    public SimpleUIPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, AnalyticsCollector.Factory analyticsCollectorFactory, Clock clock) {
        super(renderersFactory, trackSelector, loadControl, drmSessionManager, analyticsCollectorFactory, clock);
    }

    public void prepare(HlsMediaSource hlsmediaSource) {
        super.prepare(hlsmediaSource);
    }

    public void play() {
        setPlayWhenReady(true);
    }

    public void stop() {
        setPlayWhenReady(false);
    }

}


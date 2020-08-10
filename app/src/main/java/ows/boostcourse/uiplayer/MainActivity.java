package ows.boostcourse.uiplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import ows.boostcourse.uiplayer.databinding.ActivityMainBinding;
import ows.boostcourse.uiplayer2.PlayerListener;
import ows.boostcourse.uiplayer2.UIPlayer;
import ows.boostcourse.uiplayer2.UIService;

public class MainActivity extends AppCompatActivity {

    public static final String URL = "https://d3rlna7iyyu8wu.cloudfront.net/skip_armstrong/skip_armstrong_multi_language_subs.m3u8";
    ActivityMainBinding binding;

    UIPlayer uiPlayer;
    DataSource.Factory dataSourceFactory;
    HlsMediaSource hlsMediaSource;

    public PlayerListener playerListener = new PlayerListener() {
        @Override
        public void onRequest(String[] url) {
            Log.d("msg","playerlistener");
            uiPlayer.stop(hlsMediaSource,false,false);
            final String first_url = url[0];
            final String second_url = url[1];
            hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(second_url));
            uiPlayer.newPlay(hlsMediaSource);
//            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
//            builder.setNegativeButton(url[0], new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(first_url));
//                    uiPlayer.newPlay(hlsMediaSource);
//                }
//            });
//
//            builder.setNegativeButton(url[1], new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(second_url));
//                    uiPlayer.newPlay(hlsMediaSource);
//                }
//            });
//            AlertDialog alertDialog = builder.create();
//            alertDialog.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 네트워크 대역폭을 설정하는 요소
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter.Builder().build();

        // 트랙 섹션 중 하나로 적응형 트랙섹션
        AdaptiveTrackSelection.Factory adaptiveTrackSelection = new AdaptiveTrackSelection.Factory(defaultBandwidthMeter);

        // 기본 트랙 섹션 설정
        DefaultTrackSelector defaultTrackSelector = new DefaultTrackSelector(adaptiveTrackSelection);

        // 미디어 버퍼링을 컨트롤
        LoadControl loadControl = new DefaultLoadControl();

        // 미디어 파일 읽고, 디코딩 후 렌더링 요소
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this);

        uiPlayer = new UIPlayer(renderersFactory,defaultTrackSelector,loadControl,null);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        uiPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });
        binding.exoplayer.setPlayer(uiPlayer);
        uiPlayer.bindService(this, UIService.class);
        uiPlayer.setPlayerListener(playerListener);

        Uri uri = Uri.parse(URL);
        dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this,"example-test"));
        hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);

        uiPlayer.play(hlsMediaSource,uri.getHost(),uri.getPort());
    }
}

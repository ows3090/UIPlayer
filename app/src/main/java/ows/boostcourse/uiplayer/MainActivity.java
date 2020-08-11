package ows.boostcourse.uiplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
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

public class MainActivity extends AppCompatActivity implements PlayerListener{

    public static final String URL = "https://d3rlna7iyyu8wu.cloudfront.net/skip_armstrong/skip_armstrong_multi_language_subs.m3u8";
    ActivityMainBinding binding;

    UIPlayer uiPlayer;
    DataSource.Factory dataSourceFactory;
    HlsMediaSource hlsMediaSource;

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

        binding.exoplayer.setPlayer(uiPlayer);
        uiPlayer.bindService(this, UIService.class);
        uiPlayer.setPlayerListener(this);

        Uri uri = Uri.parse(URL);
        dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this,"example-test"));
        hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);

        uiPlayer.prepare(hlsMediaSource);
        uiPlayer.play(uri.getHost(),uri.getPort());
    }

    @Override
    public void onRequest(String[] url) {
            final String first_url = url[0];
            final String second_url = url[1];
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("선택을 고르세여");
            builder.setMessage(" 하나만 골라야합니다");
            builder.setPositiveButton("1", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(first_url));
                    uiPlayer.sendResponse(hlsMediaSource);
                }
            });

            builder.setNegativeButton("2", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(second_url));
                    uiPlayer.sendResponse(hlsMediaSource);
                }
            });
            builder.show();
    }
}

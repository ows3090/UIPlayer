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
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;

import ows.boostcourse.uiplayer.databinding.ActivityMainBinding;
import ows.boostcourse.uiplayer2.PlayerListener;
import ows.boostcourse.uiplayer2.UIPlayer;
import ows.boostcourse.uiplayer2.UIService;

public class MainActivity extends AppCompatActivity{

    ActivityMainBinding binding;

    public static final String URL = "https://d3rlna7iyyu8wu.cloudfront.net/skip_armstrong/skip_armstrong_multi_language_subs.m3u8";

    // 미디어 Uri
    Uri uri;

    // 실행할 UIPlayer
    UIPlayer uiPlayer;

    // 데이터를 요청하기 위한 구성요소 (HTTP, uri ..)
    DataSource.Factory dataSourceFactory;

    // HLS에 필요한 미디어 샘플 소스
    HlsMediaSource hlsMediaSource;

    // UIplayer로 돌아오는 callback Listener 구현
    PlayerListener playerListener = new PlayerListener() {

        @Override
        public void onConnet() {
            // socket 통신을 위한 host, port 정보 보내고 실행
            uiPlayer.play(uri.getHost(),uri.getPort());
        }

        @Override
        public void onUserSelect(String[] url) {
            final String first_url = url[0];
            final String second_url = url[1];

            // 다이얼로그 생성
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

        // UIPlayer 생성
        uiPlayer = new UIPlayer(renderersFactory,defaultTrackSelector,loadControl,null);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        binding.exoplayer.setPlayer(uiPlayer);

        // UIPlayer와 서비스의 바인딩
        uiPlayer.bindService(this, UIService.class);

        // 첫 미디어 스트리밍될 uri
        uri = Uri.parse(URL);
        dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this,"example-test"));
        hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);

        // UIPlayer 미디어 샘플 실행할 준비
        uiPlayer.prepare(playerListener, hlsMediaSource);




    }
}

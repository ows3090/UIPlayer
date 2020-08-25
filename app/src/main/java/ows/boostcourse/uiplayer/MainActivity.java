package ows.boostcourse.uiplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import ows.boostcourse.uiplayer.databinding.ActivityMainBinding;
import ows.boostcourse.uiplayer2.IAPlayer;
import ows.boostcourse.uiplayer2.IAListener;
import ows.boostcourse.uiplayer2.UIMessage;
import ows.boostcourse.uiplayer2.SocketService;

public class MainActivity extends AppCompatActivity{

    public static final String URL = "https://multiplatform-f.akamaihd.net/i/multi/will/bunny/big_buck_bunny_,640x360_400,640x360_700,640x360_1000,950x540_1500,.f4v.csmil/master.m3u8";
    ActivityMainBinding binding;

    IAPlayer iaPlayer;      // 실행할 IAPlayer

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

        // IAPlayer 생성
        iaPlayer = new IAPlayer(renderersFactory,defaultTrackSelector,loadControl,null);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        binding.exoplayer.setPlayer(iaPlayer.getIAPlayer());

        // 첫 미디어 스트리밍될 uri
        // 미디어 Uri
        Uri uri = Uri.parse(URL);

        // 데이터를 요청하기 위한 구성요소 (HTTP, uri ..)
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this,"example-test"));

        // HLS에 필요한 미디어 샘플 소스
        HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        iaPlayer.setDataSourceFactory(dataSourceFactory);
        iaPlayer.setHostAndPort("localhost",5001);

        // IAPlayer 미디어 샘플 실행할 준비
        iaPlayer.prepare(

                // UIplayer로 돌아오는 callback Listener 구현
                new IAListener() {

                    // 소켓통신할 서비스 연결 이벤트 콜백
                    @Override
                    public void onConnet() {

                    }

                    // 사용자에게 응답받는 이벤트 콜백
                    @Override
                    public void onUserSelect(UIMessage uiMessage) {

                        // 다이얼로그 생성
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("선택을 고르세여");
                        builder.setMessage(" 하나만 골라야합니다");

                        builder.setPositiveButton("1", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                iaPlayer.decidePlayer(0);
                                binding.exoplayer.setPlayer(iaPlayer.getIAPlayer());
                            }
                        });

                        builder.setNegativeButton("2", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                iaPlayer.decidePlayer(1);
                                binding.exoplayer.setPlayer(iaPlayer.getIAPlayer());
                            }
                        });
                        builder.show();
                    }
                },
                hlsMediaSource
       );

        // 소켓통신 서비스 연결
        iaPlayer.connect(this, SocketService.class);


    }
}

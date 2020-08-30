package ows.boostcourse.iaplayer;

import android.content.Context;
import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class IAPlayerTest {

    String URL = "https://multiplatform-f.akamaihd.net/i/multi/will/bunny/big_buck_bunny_,640x360_400,640x360_700,640x360_1000,950x540_1500,.f4v.csmil/master.m3u8";
    Context context;
    IAPlayer iaPlayer;
    SimpleIAPlayer simpleIAPlayer;
    IAListener iaListener;
    HlsMediaSource hlsMediaSource;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // 네트워크 대역폭을 설정하는 요소
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter.Builder().build();

        // 트랙 섹션 중 하나로 적응형 트랙섹션
        AdaptiveTrackSelection.Factory adaptiveTrackSelection = new AdaptiveTrackSelection.Factory(defaultBandwidthMeter);

        // 기본 트랙 섹션 설정
        DefaultTrackSelector defaultTrackSelector = new DefaultTrackSelector(adaptiveTrackSelection);

        // 미디어 버퍼링을 컨트롤
        LoadControl loadControl = new DefaultLoadControl();

        // 미디어 파일 읽고, 디코딩 후 렌더링 요소
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(context);

        iaPlayer = new IAPlayer(renderersFactory,defaultTrackSelector,loadControl,null);
        simpleIAPlayer = new SimpleIAPlayer(renderersFactory,defaultTrackSelector,loadControl,null);
        iaListener = new IAListener() {
            @Override
            public void onConnet() {

            }

            @Override
            public void onUserSelect(IAMeesage uiMessage) {

            }
        };

        Uri uri = Uri.parse(URL);

        // 데이터를 요청하기 위한 구성요소 (HTTP, uri ..)
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context,"example-test"));

        // HLS에 필요한 미디어 샘플 소스
        hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }

    @Test
    public void getIAPlayer() {
        assertEquals(simpleIAPlayer,iaPlayer.getIAPlayer());
    }

    @Test
    public void connect() {
        boolean check = iaPlayer.connect(context,SocketService.class);
        assertEquals(false,check);
    }

    @Test
    public void disconnect() {
        boolean check = iaPlayer.disconnect();
        assertEquals(false,check);
    }

    @Test
    public void prepare() {
        boolean check = iaPlayer.prepare(iaListener,hlsMediaSource);
        assertEquals(true, check);
    }

    @Test
    public void play() {
        boolean check = iaPlayer.play("localhost",5001);
        assertEquals(true,check);
    }

    @Test
    public void decidePlayer() {
        assertEquals(4,2+1);
    }
}
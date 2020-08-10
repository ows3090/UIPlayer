package ows.boostcourse.uiplayer2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsCollector;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.util.Clock;

public class UIPlayer extends SimpleExoPlayer {

    private UIService uiService;        // 백그라운드 서비스로 소켓 연결
    private boolean isService;          // 서비스 실행 확인

    // 서버에서 사용자 응답 요구 이벤트 리스너
    private PlayerListener playerListener;
    private SocketListener socketListener = new SocketListener() {
        @Override
        public void onReceive(String[] url) {
            Log.d("msg","socketlistener");
            playerListener.onRequest(url);
        }
    };

    // 서비스 바인딩에 필요한 서비스 Connection 객체
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UIService.LocalBinder localBinder = (UIService.LocalBinder)service;
            uiService = localBinder.getService();
            isService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isService = false;
        }
    };


    public UIPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        super(renderersFactory, trackSelector, loadControl, drmSessionManager);
        //uiService.setSocketListener(socketListener);
        isService = false;
    }

    public UIPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, AnalyticsCollector.Factory analyticsCollectorFactory) {
        super(renderersFactory, trackSelector, loadControl, drmSessionManager, analyticsCollectorFactory);
        //uiService.setSocketListener(socketListener);
        isService = false;
    }

    public UIPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, AnalyticsCollector.Factory analyticsCollectorFactory, Clock clock) {
        super(renderersFactory, trackSelector, loadControl, drmSessionManager, analyticsCollectorFactory, clock);
        //uiService.setSocketListener(socketListener);
        isService = false;
    }

    // playerListener 설정
    public void setPlayerListener(PlayerListener playerListener){
        this.playerListener = playerListener;
    }


    // 서비스 바인딩
    public void bindService(Context context,Class<?> name){
        Intent intent = new Intent(context,name);
        context.bindService(intent,conn,context.BIND_AUTO_CREATE);
    }

    // 미디어 스트리밍 실행, 서버 소켓과의 연결, 사용자 응답 이벤트
    public void play(HlsMediaSource hlsMediaSource,final String host, final int port){
        prepare(hlsMediaSource);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isService){
                    uiService.setSocketListener(socketListener);
                    uiService.setHostAndPort(host,port);
                    uiService.connet();
                }
                else{
                    Log.d("msg","no service");
                }
            }
        },1000);
    }

    public void stop(HlsMediaSource hlsMediaSource, boolean resetposition, boolean resetState){
        prepare(hlsMediaSource,resetposition,resetState);
    }

    // 새로운 스트리밍 실행
    public void newPlay(HlsMediaSource hlsMediaSource){
        Log.d("msg","new play");
        prepare(hlsMediaSource);
    }




}

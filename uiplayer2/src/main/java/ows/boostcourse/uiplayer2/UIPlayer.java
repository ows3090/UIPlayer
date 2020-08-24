package ows.boostcourse.uiplayer2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.analytics.AnalyticsCollector;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Clock;
import com.google.android.exoplayer2.video.VideoListener;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class UIPlayer{

    private final int STATE_DEFAULT = 0;        // 기본값
    private final int STATE_PREPARE = 1;        // 재생 준비 중
    private final int STATE_READY = 2;          // 재생 준비 완료
    private int playerState;

    private int MAX_PLAYERCOUNT = 4;       // 스트리밍 분기될 임시 Player 최대 갯수
    private int currentPlayerCount = 2;     // 스트리밍 분기될 임시 Player 갯수
    private boolean isPrepare;

    // 서버소켓과 연결하기 위한 Host, Port 정보
    private String host;
    private int port;

    private SimpleUIPlayer mainPlayer;      // 현재 실행될 Player
    private SimpleUIPlayer[] playerList = new SimpleUIPlayer[MAX_PLAYERCOUNT];      // 스트리밍 분기를 위한 임시 Player

    private DataSource.Factory dataSourceFactory;

    // User와 통신하기 위한 리스너
    private UIListener uiListener;

    private Context context;
    private UIService uiService;        // 소켓통신할 서비스

    // 비동기를 위한 핸들러, Runnable 객체
    private Handler playerHandler = new Handler();
    private Runnable playerRunnable = new Runnable() {
        @Override
        public void run() {
            sendPlayTime();
        }
    };

    // 서비스 바인딩에 필요한 서비스 Connection 객체
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UIService.LocalBinder localBinder = (UIService.LocalBinder) service;
            uiService = localBinder.getService();
            uiListener.onConnet();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    // UIPlayer와 UIService의 상호작용 이벤트 리스너
    private SocketListener socketListener = new SocketListener() {

        @Override
        public void onPreceed() {
            serviceRequest();
        }

        @Override
        public void onStart(UIMessage uiMessage) {
            currentPlayerCount = uiMessage.getUrlCount();
            isPrepare = true;

            for(int i=0;i<currentPlayerCount;i++){
                Uri uri = Uri.parse(uiMessage.getUrl()[i]);
                HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
                playerList[i].prepare(hlsMediaSource);
                Log.d("msg","샘플 백그라운드 준비");
            }
        }

        @Override
        public void onRequestSelect(UIMessage uiMessage) {
            playerState = STATE_DEFAULT;
            mainPlayer.stop();
            serviceStop();
            uiListener.onUserSelect(uiMessage);
        }
    };

    // SimpleUIPlayer의 재생관련 이벤트 리스너
    private Player.EventListener eventListener = new Player.EventListener() {

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
            Log.d("msg","onTimelineChanged");
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Log.d("msg","onTracksChanged");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Log.d("msg","onLoadingChanged : "+isLoading);
            if(!isLoading){
                if(!isPrepare){
                    playerState = STATE_READY;
                    Log.d("msg","준비완료");
                    play("localhost",5001);
                }
//                else{
//                    Log.d("msg","새 스틔밍 시작");
//                    playerState = STATE_READY;
//                    serviceStart();
//                    serviceRequest();
//                }
            }
            else{
                playerState = STATE_PREPARE;
                Log.d("msg","준비중");
            }
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.d("msg","onPlayerStateChanged playWhenReady" + playWhenReady + " playerState : "+playerState+" playbackState : "+playbackState);
            if(playerState == STATE_READY){
                if(playWhenReady){
                    Log.d("msg","진행중");
                }
                else{
                    Log.d("msg","멈춤");
                    serviceStop();
                }
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            Log.d("msg","onRepeatModeChanged");
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            Log.d("msg","onShuffleModeEnabledChanged");
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.d("msg","onPlayerError");
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            Log.d("msg","onPositionDiscontinuity");
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            Log.d("msg","onPlaybackParametersChanged");
        }

        @Override
        public void onSeekProcessed() {
            Log.d("msg","onSeekProcessed");
        }
    };

    public UIPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        init();
        mainPlayer = new SimpleUIPlayer(renderersFactory,trackSelector,loadControl,null);
        for(int i = 0; i< MAX_PLAYERCOUNT; i++){
            playerList[i] = new SimpleUIPlayer(renderersFactory,trackSelector,loadControl,null);
        }
    }

    public UIPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, AnalyticsCollector.Factory analyticsCollectorFactory) {
        init();
        mainPlayer = new SimpleUIPlayer(renderersFactory,trackSelector,loadControl,null);
        for(int i = 0; i< MAX_PLAYERCOUNT; i++){
            playerList[i] = new SimpleUIPlayer(renderersFactory,trackSelector,loadControl,null);
        }
    }

    public UIPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, AnalyticsCollector.Factory analyticsCollectorFactory, Clock clock) {
        init();
        mainPlayer = new SimpleUIPlayer(renderersFactory,trackSelector,loadControl,null);
        for(int i = 0; i< MAX_PLAYERCOUNT; i++){
            playerList[i] = new SimpleUIPlayer(renderersFactory,trackSelector,loadControl,null);
        }
    }

    private void init(){
        this.playerState = STATE_DEFAULT;
        this.isPrepare = false;
        this.host = "localhost";
        this.port = 5001;
    }

    public SimpleUIPlayer getUIPlayer(){
        return mainPlayer;
    }

    public void connect(Context context, Class<?> name){
        this.context = context;
        Intent intent = new Intent(context,name);
        context.bindService(intent,conn,context.BIND_AUTO_CREATE);
    }

    public void disconnect(){
        context.unbindService(conn);
    }

    public void setDataSourceFactory(DataSource.Factory dataSourceFactory){
        this.dataSourceFactory = dataSourceFactory;
    }

    public void setHostAndPort(String host, int port){
        this.host = host;
        this.port = port;
    }

    // 해당 미디어 소스 mainPlayer에 준비
    public void prepare(UIListener uiListener, HlsMediaSource hlsmediaSource) {
        this.uiListener = uiListener;
        mainPlayer.prepare(hlsmediaSource);
        mainPlayer.addListener(eventListener);
    }

    // 플레이어 재생
    public void play(String host, int port){
        Log.d("msg","play");
        isPrepare = false;
        uiService.init(socketListener,host,port);
        mainPlayer.play();
        serviceStart();
        serviceRequest();

    }

    // 서버소켓 연결 (시작 시, 다음 스트리밍 분기 확인)
    private void serviceStart(){
        Log.d("msg","serviceStart");
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                uiService.start();
            }
        });
    }

    // 서버소켓에 요청 핸들러에 Runnable객체 전달
    private void serviceRequest(){
        Log.d("msg","serviceRequest");
        playerHandler.post(playerRunnable);
    }

    // 서버소켓 연결 종료
    private void serviceStop(){
        Log.d("msg","serviceStop");
        playerHandler.removeCallbacks(playerRunnable);
    }

    // 서버소켓에 플레이저 재생 시간 전송
    private void sendPlayTime(){
        //Log.d("msg","sendPlayTime");
        uiService.connet((mainPlayer.getCurrentPosition()/1000)*1000);
    }

    // 다시 시작
    public void replay(){
        Log.d("msg","replay");
        serviceRequest();
    }

    // 사용자 응답 결과를 바탕으로 새로운 플레이어 지정 후 실행
    public void decidePlayer(int position){
        Log.d("msg","decidePlayer");
        mainPlayer = playerList[position];
        play(host,port);
    }


}

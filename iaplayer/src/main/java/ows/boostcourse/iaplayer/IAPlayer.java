package ows.boostcourse.iaplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

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

public class IAPlayer {

    private final static String TAG = "TAG";
    private final static String DEFAULT_HOST = "localhost";
    private final static int DEFAULT_PORT = 5001;

    private boolean isChanged;          // 스트리밍이 바뀌었는지 확인 여부
    private boolean isStart;            // 스트리밍 시작했는지 확인 여부
    private boolean playerNum;          // 스트리밍 변화 횟수만큼

    private int MAX_PLAYERCOUNT = 3;            // 스트리밍 분기될 임시 Player 최대 갯수
    private int currentPlayerCount;         // 스트리밍 분기될 임시 Player 갯수
    private int nextId;
    private SimpleIAPlayer mainPlayer;          // 현재 실행될 Player
    private SimpleIAPlayer[] playerList = new SimpleIAPlayer[MAX_PLAYERCOUNT*2];            // 스트리밍 분기를 위한 임시 Player

    private DataSource.Factory dataSourceFactory;           // MediaSource를 만들기 위한 DataSource.Factory
    private Context context;            // Context 정보
    private Class<?> name;          // 클래스 명

    private String host;                        // 서버소켓과 연결하기 위한 Host 정보
    private int port;                           // 서버소켓과 연결하기 위한 Port 정보
    private SocketService socketService;        // 소켓 통신할 서비스

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
            SocketService.LocalBinder localBinder = (SocketService.LocalBinder) service;
            socketService = localBinder.getService();
            iaListener.onConnet();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    // User와 통신하기 위한 리스너
    private IAListener iaListener;

    // UIPlayer와 UIService의 상호작용 이벤트 리스너
    private ServiceListener serviceListener = new ServiceListener() {

        @Override
        public void onPreceed() {
            serviceStop();
            serviceRequest();
        }

        @Override
        public void onGetEvent(final IAMeesage iaMeesage) {
            currentPlayerCount = iaMeesage.getUrlCount();
            playerNum = !playerNum;
            isChanged = true;

            for(int i=0;i<currentPlayerCount;i++){
                int index = 0;
                if(playerNum){
                    index = MAX_PLAYERCOUNT;
                }

                final int differ = index;
                final int position = i;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Uri uri = Uri.parse(iaMeesage.getUrl()[position]);
                        final HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
                        playerList[position+differ].prepare(hlsMediaSource);
                        Log.d(TAG,"샘플 백그라운드 준비");
                    }
                }).start();
            }
        }

        @Override
        public void onResponse(IAMeesage iaMeesage) {
            Log.d(TAG,"onRequestSelect");
            isStart = false;
            mainPlayer.stop();
            serviceStop();
            iaListener.onUserSelect(iaMeesage);
        }
    };


    // SimpleUIPlayer의 재생관련 이벤트 리스너
    private Player.EventListener eventListener = new Player.EventListener() {

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
            Log.d(TAG,"onTimelineChanged");
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Log.d(TAG,"onTracksChanged");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Log.d(TAG,"onLoadingChanged is " + isLoading);
            if(!isLoading){
                if(!isChanged){
                    play(host,port);
                }
                else{

                }
            }
            else{

            }
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.d(TAG,"onPlayerStateChanged playWhenReady" + playWhenReady + " playbackState : "+playbackState);
            if(playbackState == Player.STATE_READY){
                if(playWhenReady){
                    if(!isStart){
                        serviceStart();
                        serviceRequest();
                    }
                    else{ serviceRequest();}
                } else{ serviceStop();}
            }
            else{

            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            Log.d(TAG,"onRepeatModeChanged");
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            Log.d(TAG,"onShuffleModeEnabledChanged");
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.d(TAG,"onPlayerError");
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            Log.d(TAG,"onPositionDiscontinuity");
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            Log.d(TAG,"onPlaybackParametersChanged");
        }

        @Override
        public void onSeekProcessed() {
            Log.d(TAG,"onSeekProcessed");
        }
    };

    // 생성자
    public IAPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        this.isChanged = false;
        this.isStart = false;
        this.playerNum = false;
        this.currentPlayerCount = MAX_PLAYERCOUNT;
        this.nextId = 0;
        this.host = DEFAULT_HOST;
        this.port = DEFAULT_PORT;
        mainPlayer = new SimpleIAPlayer(renderersFactory,trackSelector,loadControl,null);
        for(int i = 0; i< MAX_PLAYERCOUNT*2; i++){
            playerList[i] = new SimpleIAPlayer(renderersFactory,trackSelector,loadControl,null);
        }
    }

    public IAPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, AnalyticsCollector.Factory analyticsCollectorFactory) {
        this.isChanged = false;
        this.isStart = false;
        this.playerNum = false;
        this.currentPlayerCount = MAX_PLAYERCOUNT;
        this.nextId = 0;
        this.host = DEFAULT_HOST;
        this.port = DEFAULT_PORT;
        mainPlayer = new SimpleIAPlayer(renderersFactory,trackSelector,loadControl,null);
        for(int i = 0; i< MAX_PLAYERCOUNT*2; i++){
            playerList[i] = new SimpleIAPlayer(renderersFactory,trackSelector,loadControl,null);
        }
    }

    public IAPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, AnalyticsCollector.Factory analyticsCollectorFactory, Clock clock) {
        this.isChanged = false;
        this.isStart = false;
        this.playerNum = false;
        this.currentPlayerCount = MAX_PLAYERCOUNT;
        this.nextId = 0;
        this.host = DEFAULT_HOST;
        this.port = DEFAULT_PORT;
        mainPlayer = new SimpleIAPlayer(renderersFactory,trackSelector,loadControl,null);
        for(int i = 0; i< MAX_PLAYERCOUNT*2; i++){
            playerList[i] = new SimpleIAPlayer(renderersFactory,trackSelector,loadControl,null);
        }
    }

    // mainPlayer의 get 함수
    public SimpleIAPlayer getIAPlayer(){
        return mainPlayer;
    }

    // 서비스와 connect하는 함수
    public void connect(Context context, Class<?> name){
        this.context = context;
        this.name = name;
        Intent intent = new Intent(context,name);
        context.bindService(intent,conn,context.BIND_AUTO_CREATE);
    }

    // 서비스와 disconnect하는 함수
    public void disconnect(){
        context.unbindService(conn);
    }

    // Uri을 Mediasource로 바꾸기 위한 DataSource.Factory 지정
    public void setDataSourceFactory(DataSource.Factory dataSourceFactory){
        this.dataSourceFactory = dataSourceFactory;
    }

    // 소켓 통신을 위한 Host, Port정보 설정
    public void setHostAndPort(String host, int port){
        this.host = host;
        this.port = port;
    }

    // 해당 미디어 소스 mainPlayer에 준비
    public void prepare(IAListener uiListener, HlsMediaSource hlsmediaSource) {
        this.iaListener = uiListener;
        mainPlayer.prepare(hlsmediaSource);
        mainPlayer.addListener(eventListener);
    }

    // 플레이어 재생
    public void play(String host, int port){
        Log.d(TAG,"play");
        socketService.init(serviceListener,host,port);
        mainPlayer.play();
    }

    // 서버소켓 연결 (시작 시, 다음 스트리밍 분기 확인)
    private void serviceStart(){
        Log.d(TAG,"serviceStart");
        isStart = true;
        socketService.getIATime(nextId);
    }

    // 서버소켓에 요청 핸들러에 Runnable객체 전달
    private void serviceRequest(){
        playerHandler.postDelayed(playerRunnable,1000);
    }

    // 서버소켓 연결 종료
    private void serviceStop(){
        playerHandler.removeCallbacks(playerRunnable);
    }

    // 서버소켓에 플레이저 재생 시간 전송
    private void sendPlayTime(){
        socketService.sendToSocket((mainPlayer.getCurrentPosition()/1000)*1000);
    }

    // 사용자 응답 결과를 바탕으로 새로운 플레이어 지정 후 실행
    public void decidePlayer(int position,int nextId){
        Log.d(TAG,"decidePlayer");
        this.nextId = nextId;
        if(playerNum){
            mainPlayer = playerList[position+MAX_PLAYERCOUNT];
        }else{
            mainPlayer = playerList[position];
        }
        mainPlayer.addListener(eventListener);
        play(host,port);
    }


}

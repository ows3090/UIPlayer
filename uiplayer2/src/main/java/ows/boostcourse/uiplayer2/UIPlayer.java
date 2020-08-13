package ows.boostcourse.uiplayer2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
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
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.util.Clock;

public class UIPlayer extends SimpleExoPlayer implements Player.EventListener{

    // 소켓통신할 서비스 객체
    private UIService uiService;

    // 서비스 바인딩 유무
    private boolean isService;

    // User와 UIPlayer의 상호작용 이벤트 리스너
    private PlayerListener playerListener;

    // UIPlayer와 UIService의 상호작용 이벤트 리스너
    private SocketListener socketListener = new SocketListener() {

        @Override
        public void onPreceed() {
            confirmPlaytime();
        }

        @Override
        public void onRequestSelect(String[] url) {
            setPlayWhenReady(false);
            playerListener.onUserSelect(url);
        }
    };

    private Handler playerHandler = new Handler();
    private Runnable playerAction = new Runnable() {
        @Override
        public void run() {
            requestPlaytime();
        }
    };

    // 서비스 바인딩에 필요한 서비스 Connection 객체
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized ((Boolean)isService) {
                UIService.LocalBinder localBinder = (UIService.LocalBinder) service;
                uiService = localBinder.getService();
                isService = true;
                playerListener.onConnet();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isService = false;
        }
    };

    // 생성자
    public UIPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        super(renderersFactory, trackSelector, loadControl, drmSessionManager);
        isService = false;
    }

    public UIPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, AnalyticsCollector.Factory analyticsCollectorFactory) {
        super(renderersFactory, trackSelector, loadControl, drmSessionManager, analyticsCollectorFactory);
        isService = false;
    }

    public UIPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, AnalyticsCollector.Factory analyticsCollectorFactory, Clock clock) {
        super(renderersFactory, trackSelector, loadControl, drmSessionManager, analyticsCollectorFactory, clock);
        isService = false;
    }

    // 서비스 바인딩
    public void bindService(Context context,Class<?> name){
        Intent intent = new Intent(context,name);
        context.bindService(intent,conn,context.BIND_AUTO_CREATE);
    }

    public void prepare(PlayerListener playerListener,HlsMediaSource hlsmediaSource) {
        this.playerListener = playerListener;
        super.prepare(hlsmediaSource);
    }

    // 미디어 스트리밍 실행, 서버 소켓과의 연결, 사용자 응답 이벤트
    public void play(final String host, final int port){
        uiService.init(socketListener, host,port);
        setPlayWhenReady(true);
    }

    // 미디어 샘플 재생시간 서버소켓에 전송
    public void requestPlaytime(){
        uiService.connet(getCurrentPosition()/1000);
    }

    // 미디어 샘플 재생시간 확인
    public void confirmPlaytime(){
        playerHandler.post(playerAction);
    }

    // 사용자 응답 처리
    public void sendResponse(MediaSource mediaSource){
        prepare(mediaSource);
        setPlayWhenReady(true);
    }

    // 미디어 샘플 실행중지
    public void stop(){
        setPlayWhenReady(false);
    }


    @Override
    public void addListener(Player.EventListener listener) {
        super.addListener(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    ////////////// Player.EventListener Implementation /////////////////////////////////

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
        Log.d("msg","onLoadingChanged");
        if(isLoading){
            confirmPlaytime();
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.d("msg","onPlayerStateChanged"+playbackState);
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
    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
}

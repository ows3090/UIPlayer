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
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.util.Clock;

public class SimpleUIPlayer extends SimpleExoPlayer implements Player.EventListener{

    // 소켓통신할 서비스 객체
    UIService uiService;

    // 서비스 바인딩 유무
    boolean isService;

    // player Ready 완료상태
    boolean isReady;

    Context context;

    Class<?> name;

    private PlayerListener playerListener;

    // 비동기통신 handler 객체
    private Handler playerHandler = new Handler();

    // handler에 포함할 Runnable 객체
    private Runnable playerAction = new Runnable() {
        @Override
        public void run() {
            requestPlayTimeData();
        }
    };

    // UIPlayer와 UIService의 상호작용 이벤트 리스너
    private SocketListener socketListener = new SocketListener() {

        @Override
        public void onPreceed() {
            requestAsync();
        }

        @Override
        public void onStart(String[] url) {
            playerListener.onPreparePlayer(url);
        }

        @Override
        public void onRequestSelect(String[] url) {
            isReady = false ;
            setPlayWhenReady(isReady);
            disconnect();
            playerListener.onSelectPlayer(url);
        }
    };

    // 서비스 바인딩에 필요한 서비스 Connection 객체
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized ((Boolean)isService) {
                Log.d("msg","onServiceCOnnected");
                UIService.LocalBinder localBinder = (UIService.LocalBinder) service;
                uiService = localBinder.getService();
                isService = true;
                playerListener.onConnectRespone();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isService = false;
        }
    };


    public SimpleUIPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        super(renderersFactory, trackSelector, loadControl, drmSessionManager);
        isService = false;
        isReady = false;
    }

    public SimpleUIPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, AnalyticsCollector.Factory analyticsCollectorFactory) {
        super(renderersFactory, trackSelector, loadControl, drmSessionManager, analyticsCollectorFactory);
        isService = false;
        isReady = false;
    }

    public SimpleUIPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, AnalyticsCollector.Factory analyticsCollectorFactory, Clock clock) {
        super(renderersFactory, trackSelector, loadControl, drmSessionManager, analyticsCollectorFactory, clock);
        isService = false;
        isReady = false;
    }

    public void prepare(PlayerListener playerListener, HlsMediaSource hlsmediaSource) {
        this.playerListener = playerListener;
        super.prepare(hlsmediaSource);
    }

    public void connect(Context context, Class<?> name){
        this.context = context;
        this.name = name;
        Intent intent = new Intent(context,name);
        context.bindService(intent,conn,context.BIND_AUTO_CREATE);
    }

    public void play(String host, int port){
        isReady = true;
        uiService.init(socketListener,host,port);
        setPlayWhenReady(isReady);
        requestAsync();
        requestStart();
    }

    public void requestStart(){
        uiService.start();
    }

    public void requestAsync(){
        playerHandler.post(playerAction);
    }

    public void requestPlayTimeData(){
        uiService.connet((getCurrentPosition()/1000)*1000);
    }

    public void removeRequestAsync(){
        playerHandler.removeMessages(0);
    }

    public void disconnect(){
        context.unbindService(conn);
    }









    @Override
    public void addListener(Player.EventListener listener) {
        super.addListener(this);
    }

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
            Log.d("msg","onLoadingChanged is true");
            //requestAsync();
        }
        else{
            //removeRequestAsync();
            //requestAsync();
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.d("msg","onPlayerStateChanged "+playWhenReady);
        if(!isReady){
            if(playbackState == Player.STATE_IDLE){
                Log.d("msg","onPlayerStateChanged is false");
                removeRequestAsync();
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
        removeRequestAsync();
        requestAsync();
    }
}

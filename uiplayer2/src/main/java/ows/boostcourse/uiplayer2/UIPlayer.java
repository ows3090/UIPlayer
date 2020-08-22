package ows.boostcourse.uiplayer2;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.analytics.AnalyticsCollector;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Clock;
import com.google.android.exoplayer2.video.VideoListener;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class UIPlayer{

    private int MAX_PLAYERCOUNT = 5;
    private int currentPlayerCount = 2;

    private ExecutorService executorService;

    private Context context;

    private Class<?> name;

    private DataSource.Factory dataSourceFactory;

    private UIListener uiListener;

    private String[] url = new String[2];

    private Handler prepareHandler = new Handler();

//    private Runnable runnable = new Runnable() {
//        @Override
//        public void run() {
//            for(int i=0;i<currentPlayerCount;i++){
//
//                ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)executorService;
//
//                final int position = i;
//                settingPlayer(position,url[position]);
////                new Thread(new Runnable() {
////                    @Override
////                    public void run() {
////                        settingPlayer(position,url[position]);
////                    }
////                }).start();
//            }
//        }
//    };


    private PlayerListener playerListener = new PlayerListener() {

        @Override
        public void onConnectRespone() {
            uiListener.onConnet();
        }

        @Override
        public void onPreparePlayer(final String[] url) {
            setUrl(url);
            //prepareHandler.post(runnable);
            for(int i=0;i<currentPlayerCount;i++){
                final int position = i;
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Uri uri = Uri.parse(url[position]);
                        HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
                        playerList[position].prepare(playerListener,hlsMediaSource);
                        Log.d("msg","준비완료");
                    }
                };
                executorService.execute(runnable);
            }
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            },1000);

        }

        @Override
        public void onSelectPlayer(String[] url) {
            uiListener.onUserSelect(url);
        }
    };

    SimpleUIPlayer mainPlayer;
    SimpleUIPlayer[] playerList = new SimpleUIPlayer[MAX_PLAYERCOUNT];


    public UIPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        executorService = Executors.newFixedThreadPool(MAX_PLAYERCOUNT);
        mainPlayer = new SimpleUIPlayer(renderersFactory,trackSelector,loadControl,null);
        for(int i = 0; i< MAX_PLAYERCOUNT; i++){
            playerList[i] = new SimpleUIPlayer(renderersFactory,trackSelector,loadControl,null);
        }
    }

    public UIPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, AnalyticsCollector.Factory analyticsCollectorFactory) {
        mainPlayer = new SimpleUIPlayer(renderersFactory,trackSelector,loadControl,null);
        for(int i = 0; i< MAX_PLAYERCOUNT; i++){
            playerList[i] = new SimpleUIPlayer(renderersFactory,trackSelector,loadControl,null);
        }
    }

    public UIPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, AnalyticsCollector.Factory analyticsCollectorFactory, Clock clock) {
        mainPlayer = new SimpleUIPlayer(renderersFactory,trackSelector,loadControl,null);
        for(int i = 0; i< MAX_PLAYERCOUNT; i++){
            playerList[i] = new SimpleUIPlayer(renderersFactory,trackSelector,loadControl,null);
        }
    }

    public SimpleUIPlayer getUIPlayer(){
        return mainPlayer;
    }

    public void setDataSourceFactory(DataSource.Factory dataSourceFactory){
        this.dataSourceFactory = dataSourceFactory;
    }

    // 해당 미디어 소스 mainPlayer에 준비
    public void prepare(UIListener uiListener, HlsMediaSource hlsmediaSource) {
        this.uiListener = uiListener;
        mainPlayer.prepare(playerListener,hlsmediaSource);
    }

    public void connect(Context context, Class<?> name){
        this.context = context;
        this.name = name;
        mainPlayer.connect(context,name);
    }

    public void play(String host, int port){
        mainPlayer.play(host,port);
    }

    private void settingPlayer(int position, String url){
//        for(int i=0;i<currentPlayerCount;i++){
//            Uri uri = Uri.parse(url[i]);
//            HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
//            playerList[i].prepare(playerListener,hlsMediaSource);
//        }
        Uri uri = Uri.parse(url);
        HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        playerList[position].prepare(playerListener,hlsMediaSource);
        Log.d("msg","준비완료");
    }

    private void setUrl(String[] url){
        this.url = url;
    }

    public void decidePlayer(int position){
        Log.d("msg","decidePlayer");
        mainPlayer = playerList[position];
        connect(context,name);
    }


}

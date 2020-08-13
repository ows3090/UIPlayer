package ows.boostcourse.uiplayer2;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class UIService extends Service {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;

    private String host;
    private int port;
    private SocketListener socketListener;
    private final IBinder mbinder = new LocalBinder();

    class LocalBinder extends Binder{
        UIService getService(){
            return UIService.this;
        }
    }

    public UIService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        host = DEFAULT_HOST;
        port = DEFAULT_PORT;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mbinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void init(SocketListener socketListener, String host, int port){
        this.socketListener = socketListener;
        this.host = host;
        this.port = port;
    }

    public void connet(long time){
        if(time==10){
            Log.d("msg","connet");
            String[] url = new String[2];
            url[0]="http://demo.unified-streaming.com/video/tears-of-steel/tears-of-steel.ism/.m3u8";
            url[1]="http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";
            socketListener.onRequestSelect(url);
        }
        else{
            socketListener.onPreceed();
        }
    }

}

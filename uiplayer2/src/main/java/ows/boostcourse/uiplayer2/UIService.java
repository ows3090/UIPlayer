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
    //private ClientThread clientThread;
    private SocketListener socketListener;
    private final IBinder mbinder = new LocalBinder();

    class LocalBinder extends Binder{
        UIService getService(){
            return UIService.this;
        }
    }

//    class ClientThread extends Thread{
//        @Override
//        public void run() {
//            try{
//                Socket socket = new Socket(host,port);

//                while(true){
//                    ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
//                    Object input = inputStream.readObject();
//                    socketListener.onReceive((String)input);
//                }
//                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
//                Object input = inputStream.readObject();
//                socketListener.onReceive((String[])input);
//
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//    }

    public UIService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        host = DEFAULT_HOST;
        port = DEFAULT_PORT;
        //clientThread = new ClientThread();
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

    public void setSocketListener(SocketListener socketListener){
        this.socketListener = socketListener;
    }

    public void setHostAndPort(String host, int port){
        this.host = host;
        this.port = port;
    }

    public void connet(){
       // clientThread.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("msg","connet");
                String[] url = new String[2];
                url[0]="http://demo.unified-streaming.com/video/tears-of-steel/tears-of-steel.ism/.m3u8";
                url[1]="http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";
                socketListener.onReceive(url);
            }
        },10000);
    }

//    public void disconnet(){
////        clientThread.stop();
////    }


}

package ows.boostcourse.uiplayer2;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
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
    private String[] url = new String[2];

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
        Log.d("msg","finish");
        super.onDestroy();
    }

    public void init(SocketListener socketListener, String host, int port){
        this.socketListener = socketListener;
        this.host = host;
        this.port = port;
    }

    public void start(){
        Log.d("msg","time is 0");
        String[] url = new String[2];
        url[0]="https://mnmedias.api.telequebec.tv/m3u8/29880.m3u8";
        url[1]="http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";
        socketListener.onStart(url);
    }

    long TIME=0;
    public void connet(long time){
        Log.d("msg","connect");
        if(time == 10000){
            TIME=time;
            Log.d("msg","time is 10000");
            String[] url = new String[2];
            url[0]="https://mnmedias.api.telequebec.tv/m3u8/29880.m3u8";
            url[1]="http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";
            socketListener.onRequestSelect(url);

//            ClientSocketThread thread = new ClientSocketThread();
//            thread.start();
//            try{
//                thread.join();
//            }catch (Exception e){}
//            socketListener.onRequestSelect(url);
        }
        else{
            socketListener.onPreceed();
        }
    }

    class ClientSocketThread extends Thread{
        @Override
        public void run() {
            String h = "localhost";
            int p = 5001;

            try{
                Socket socket = new Socket(h,p);

                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.writeObject(TIME);
                outputStream.flush();

                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                //UIMessage uiMessage = (UIMessage)inputStream.readObject();

//                long time = uiMessage.getEventTime();
//                String url1 = uiMessage.getUrl()[0];
//                String url2 = uiMessage.getUrl()[1];
                long time = (long)inputStream.readObject();
                String url1 = (String)inputStream.readObject();
                String url2 = (String)inputStream.readObject();
                url[0]=url1;
                url[1]=url2;

                Log.d("msg","출력");
                Log.d("msg",time+", "+url1 + ", "+url2);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}

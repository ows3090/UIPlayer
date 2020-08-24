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

    // DEFAULT HOST, PORT 정보
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5001;

    // HOST, PORT 정보
    private String host;
    private int port;

    // UIPlayer와 통신하기 위한 리스너
    private SocketListener socketListener;

    // 서비스 바인딩 완료시 반환되는 바인더 객체
    private final IBinder mbinder = new LocalBinder();

    private UIMessage uiMessage;

    class LocalBinder extends Binder{
        UIService getService(){
            return UIService.this;
        }
    }

    public UIService() {
        host = DEFAULT_HOST;
        port = DEFAULT_PORT;
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
        uiMessage = new UIMessage();
        ClientSocketThread thread = new ClientSocketThread();
        thread.start();
    }

    public void connet(long time){
        //Log.d("msg","connect is "+time);
        if(time == uiMessage.getEventTime()){
            Log.d("msg","time : "+time +", "+uiMessage.getEventTime());
            socketListener.onRequestSelect(uiMessage);
        }
        else{
            socketListener.onPreceed();
        }
    }

    class ClientSocketThread extends Thread{
        @Override
        public void run() {
            try{
                Log.d("msg","socket thread start");
                Socket socket = new Socket(host,port);

                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.writeObject("전달");
                outputStream.flush();

                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                long eventTime = (long)inputStream.readObject();
                int urlCount = (int)inputStream.readObject();
                String[] url = (String[])inputStream.readObject();

                Log.d("msg",eventTime + ", "+urlCount+", "+url[0]+", "+url[1]);
                uiMessage.setEventTime(eventTime);
                uiMessage.setUrlCount(urlCount);
                uiMessage.setUrl(url);

                socketListener.onStart(uiMessage);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}

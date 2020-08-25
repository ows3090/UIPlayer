package ows.boostcourse.uiplayer2;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketService extends Service {

    private final static String TAG = "TAG";
    private static final String DEFAULT_HOST = "localhost";         // DEFAULT HOST, PORT 정보
    private static final int DEFAULT_PORT = 5001;

    private String host;        // HOST 정보
    private int port;           // PORT 정보

    private SocketListener socketListener;          // UIPlayer와 통신하기 위한 리스너
    private UIMessage uiMessage;             // 소켓통신 후 받게 되는 정보

    // 서비스 바인딩 완료시 반환되는 바인더 클래스
    class LocalBinder extends Binder{
        SocketService getService(){
            return SocketService.this;
        }
    }

    // LocalBinder의 인스턴스
    private final IBinder mbinder = new LocalBinder();

    // 생성자
    public SocketService() {
        host = DEFAULT_HOST;
        port = DEFAULT_PORT;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mbinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // 서비스 통신하기 위한 기본 설정
    public void init(SocketListener socketListener, String host, int port){
        uiMessage = new UIMessage();
        this.socketListener = socketListener;
        this.host = host;
        this.port = port;
    }

    // interaction 정보 (UIMessage)를 얻기 위해 서버소켓과 통신
    public void getIATime(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Socket socket = new Socket(host,port);

                    ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                    outputStream.writeObject("전달");
                    outputStream.flush();

                    ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                    long eventTime = (long)inputStream.readObject();
                    int urlCount = (int)inputStream.readObject();
                    String[] url = (String[])inputStream.readObject();

                    Log.d(TAG,eventTime + ", "+urlCount+", "+url[0]+", "+url[1]);
                    uiMessage.setEventTime(eventTime);
                    uiMessage.setUrlCount(urlCount);
                    uiMessage.setUrl(url);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
//        ClientSocketThread thread = new ClientSocketThread();
//        thread.start();
    }

    public void sendToSocket(long time){
        Log.d(TAG,"connect is "+time);
        if(time == uiMessage.getEventTime()){
            socketListener.onResponse(uiMessage);
        }
        else if(time == uiMessage.getEventTime()-2000){
            Log.d(TAG,"prepare sample streaming");
            socketListener.onGetEvent(uiMessage);
            socketListener.onPreceed();
        }
        else{
            socketListener.onPreceed();
        }
    }

    class ClientSocketThread extends Thread{
        @Override
        public void run() {
            try{
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

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}

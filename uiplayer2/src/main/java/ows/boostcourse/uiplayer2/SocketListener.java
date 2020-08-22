package ows.boostcourse.uiplayer2;

public interface SocketListener {

    void onPreceed();
    void onStart(String[] url);
    void onRequestSelect(String[] url);

}

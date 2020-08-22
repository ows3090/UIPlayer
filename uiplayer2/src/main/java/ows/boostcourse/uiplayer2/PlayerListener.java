package ows.boostcourse.uiplayer2;

public interface PlayerListener {
    void onConnectRespone();
    void onPreparePlayer(String[] url);
    void onSelectPlayer(String[] url);
}

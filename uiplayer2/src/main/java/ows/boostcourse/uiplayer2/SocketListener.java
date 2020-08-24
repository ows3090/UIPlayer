package ows.boostcourse.uiplayer2;

public interface SocketListener {

    void onPreceed();
    void onStart(UIMessage uiMessage);
    void onRequestSelect(UIMessage uiMessage);

}

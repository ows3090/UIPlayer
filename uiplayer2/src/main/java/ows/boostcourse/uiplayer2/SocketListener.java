package ows.boostcourse.uiplayer2;

public interface SocketListener {

    void onGetEvent(UIMessage uiMessage);
    void onPreceed();
    void onResponse(UIMessage uiMessage);

}

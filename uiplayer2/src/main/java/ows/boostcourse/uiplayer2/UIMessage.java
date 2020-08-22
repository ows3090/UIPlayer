package ows.boostcourse.uiplayer2;
import java.io.Serializable;

public class UIMessage implements Serializable {

    private final int MAX_COUNT=4;
    private long eventTime;
    private int urlCount;
    private String[] url;

    public UIMessage(){
        eventTime = 0;
        urlCount = MAX_COUNT;
        url = new String[MAX_COUNT];
    }

    public UIMessage(long eventTime, int urlCount, String[] url) {
        this.eventTime = eventTime;
        this.urlCount = urlCount;
        this.url = url;
    }

    public long getEventTime() {
        return eventTime;
    }

    public int getUrlCount() {
        return urlCount;
    }

    public String[] getUrl() {
        return url;
    }



}

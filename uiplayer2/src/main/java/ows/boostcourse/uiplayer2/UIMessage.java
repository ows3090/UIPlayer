package ows.boostcourse.uiplayer2;
import android.util.Log;

import java.io.Serializable;

public class UIMessage implements Serializable {

    private final int MAX_COUNT=4;
    private long eventTime;
    private int urlCount;
    private String[] url;

    public UIMessage(){
        eventTime = 10000000;
        urlCount = MAX_COUNT;
        url = new String[MAX_COUNT];
    }

    public UIMessage(long eventTime, int urlCount, String[] url) {
        this.eventTime = eventTime;
        this.urlCount = urlCount;
        this.url = url;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public void setUrlCount(int urlCount) {
        this.urlCount = urlCount;
    }

    public void setUrl(String[] url) {
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

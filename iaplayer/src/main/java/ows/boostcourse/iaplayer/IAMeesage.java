package ows.boostcourse.iaplayer;

import java.io.Serializable;

public class IAMeesage implements Serializable {

    private final int MAX_COUNT=3;
    private long eventTime;
    private int urlCount;
    private String[] url;

    public IAMeesage(){
        eventTime = 10000000;
        urlCount = MAX_COUNT;
        url = new String[MAX_COUNT];
    }

    public IAMeesage(long eventTime, int urlCount, String[] url) {
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

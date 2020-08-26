package ows.boostcourse.iaplayer;

        import java.io.Serializable;

public class IAMeesage implements Serializable {

    private final int MAX_COUNT=3;
    private long eventTime;
    private int urlCount;
    private String[] url;
    private String[] title;
    private int[] nextId;

    public IAMeesage(){
        eventTime = 10000000;
        urlCount = MAX_COUNT;
        url = new String[MAX_COUNT];
        title = new String[MAX_COUNT];
        nextId = new int[MAX_COUNT];
    }

    public IAMeesage(long eventTime, int urlCount, String[] url,String[] title, int[] nextId) {
        this.eventTime = eventTime;
        this.urlCount = urlCount;
        this.url = url;
        this.title = title;
        this.nextId = nextId;
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

    public void setTitle(String[] title) {
        this.title = title;
    }

    public void setNextId(int[] nextId) {
        this.nextId = nextId;
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

    public String[] getTitle() {
        return title;
    }

    public int[] getNextId() {
        return nextId;
    }
}

package ows.boostcourse.iaplayer;

public interface ServiceListener {

    void onGetEvent(IAMeesage uiMessage);
    void onPreceed();
    void onResponse(IAMeesage uiMessage);

}

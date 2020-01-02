package nl.sense.rninputkit.service.scheduler;

public interface IScheduler {
    void onCreate();
    void scheduleDaily();
    void scheduleImmediately();
    void cancelSchedules();
    void onDestroy();
}

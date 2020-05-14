package yychen.demo.timingwheel;

/**
 * @Author: siran.yao
 * @time: 2020/5/8:上午11:14
 */
public class TimerTaskEntry {
    private TimerTask timerTask;
    private long expirationMs;
    public TimerTaskList list;
    public TimerTaskEntry prev;
    public TimerTaskEntry next;

    public TimerTaskEntry(TimerTask timerTask, long expirationMs) {
        this.timerTask = timerTask;
        this.expirationMs = expirationMs;
    }

    public TimerTask getTimerTask() {
        return timerTask;
    }

    public boolean cancelled(){
        return timerTask.getTimerTaskEntry() != this;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void remove() {
    }
}

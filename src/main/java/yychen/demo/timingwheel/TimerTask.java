package yychen.demo.timingwheel;

/**
 * @Author: siran.yao
 * @time: 2020/5/8:上午11:13
 */
public class TimerTask implements Runnable{
    private long delayMs;
    private TimerTaskEntry timerTaskEntry;

    public TimerTaskEntry getTimerTaskEntry() {
        return timerTaskEntry;
    }

    public void run() {

    }

    public void setDelayMs(long delayMs) {
        this.delayMs = delayMs;
    }

    public long getDelayMs() {
        return delayMs;
    }
}

package yychen.demo.timingwheel;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author: siran.yao
 * @time: 2020/5/8:上午11:07
 * 时间轮的实现
 */
public class TimingWheel {
    //每一个格子的延迟时间
    private long tickMs;
    //总共有多少个格子
    private int wheelSize;
    //当前时间轮的创建时间
    private long startMs;
    //当前时间轮中的任务总数
    private AtomicLong taskCounter;
    //全局共用，存放时间轮的队列
    private DelayQueue delayQueue;

    //当前时间的总延迟时间 tickMs * wheelSize
    private long interval;
    //时间轮的指针
    private long currentTime;
    //对应当前时间轮，长度就是 wheelSize ，每一项 都对应时间轮中的一个时间格
    private TimerTaskList[] buckets = new TimerTaskList[wheelSize];
    //上层时间轮的引用
    private TimingWheel overflowWheel;

    public TimingWheel(long tickMs, int wheelSize, long startMs, AtomicLong taskCounter, DelayQueue delayQueue) {
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;
        this.startMs = startMs;
        this.taskCounter = taskCounter;
        this.delayQueue = delayQueue;
        this.interval = tickMs * wheelSize;
        this.currentTime = startMs - (startMs % tickMs);
    }

    /**
     * 往当前时间轮添加任务
     * @param timerTaskEntry
     * @return
     */
    public boolean add(TimerTaskEntry timerTaskEntry){
        long expiration = timerTaskEntry.getExpirationMs();
        if (timerTaskEntry.cancelled())
            return false;
        if (expiration <  currentTime + tickMs)
            return false;
        if (expiration < currentTime + interval){
            long virtualId = expiration / tickMs;
            TimerTaskList bucket = buckets[(int) (virtualId % wheelSize)];
            bucket.add(timerTaskEntry);
            if(bucket.setExpiration(virtualId * tickMs)){
                delayQueue.offer(bucket);
            }
        }else {
            if (overflowWheel == null)
                addOverFlowWheel();
            return overflowWheel.add(timerTaskEntry);
        }
        return false;
    }

    /**
     * 推进时间轮指针
     * @param timeMs
     */
    public void advanceClock(long timeMs){
        if(timeMs >= currentTime + timeMs)
            currentTime = timeMs - (timeMs % tickMs);
        if (overflowWheel != null)
            overflowWheel.advanceClock(currentTime);
    }

    /**
     * 创建上层时间轮
     */
    private void addOverFlowWheel(){
        synchronized(this){
            if (overflowWheel == null)
                overflowWheel = new TimingWheel(interval,wheelSize,currentTime,taskCounter,delayQueue);
        }
    }
}

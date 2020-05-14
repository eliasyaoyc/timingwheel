package yychen.demo.timingwheel;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Author: siran.yao
 * @time: 2020/5/8:下午1:13
 * 对时间轮的包装
 */
public class SystemTimer {
    private long startMs;
    private DelayQueue<TimerTaskList> delayQueue = new DelayQueue();
    private AtomicLong taskCounter = new AtomicLong(0);
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
    private ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
    private ThreadPoolExecutor taskExecutor = new ThreadPoolExecutor(
            1,
            1,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
    private TimingWheel timingWheel = new TimingWheel(
            1,
            20,
            LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli(),
            taskCounter,
            delayQueue);

    public void add(TimerTask timerTask){
        readLock.lock();
        try {
            addTimerTaskEntry(new TimerTaskEntry(timerTask,
                    timerTask.getDelayMs() + LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli()));
        }finally {
            readLock.unlock();
        }
    }

    private void addTimerTaskEntry(TimerTaskEntry timerTaskEntry){
        //add failure
        if(!timingWheel.add(timerTaskEntry)){
            //whether cancelled or expired
            if(!timerTaskEntry.cancelled())
                taskExecutor.submit(timerTaskEntry.getTimerTask());
        }
    }

    /**
     * advance timing-wheel
     */
    public boolean advanceClock(long timeoutMs){
        try {
            TimerTaskList bucket = delayQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
            if (bucket != null){
                writeLock.lock();
                while (bucket != null){
                    timingWheel.advanceClock(bucket.getExpiration().longValue());
                    bucket.flush();
                    bucket = delayQueue.poll();
                }
            }else{
                return false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            writeLock.unlock();
        }
        return true;
    }

    /**
     * get current task number
     * @return
     */
    public long taskCounter(){
        return taskCounter.get();
    }

    /**
     * stop the thread pool, avoid memory leak
     */
    public void shutdown(){
        taskExecutor.shutdown();
    }

    public TimingWheel getTimingWheel() {
        return timingWheel;
    }
}
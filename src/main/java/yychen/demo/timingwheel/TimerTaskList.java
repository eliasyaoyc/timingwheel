package yychen.demo.timingwheel;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author: siran.yao
 * @time: 2020/5/8:上午11:13
 */
public class TimerTaskList implements Delayed {
    private AtomicInteger taskCounter = new AtomicInteger(0);
    private AtomicLong expiration = new AtomicLong(-1L);
    private TimerTaskEntry root = new TimerTaskEntry(null,-1);

    public TimerTaskList() {
        root.prev = root;
        root.next = root;
    }

    public boolean setExpiration(long expiration) {
        return this.expiration.getAndSet(expiration) != expiration;
    }

    public AtomicLong getExpiration() {
        return expiration;
    }

    //add a timer task entry to this list
    public void add(TimerTaskEntry timerTaskEntry) {
        boolean done = false;
        while (!done){
            timerTaskEntry.remove();
            synchronized (timerTaskEntry){
                if(timerTaskEntry.list == null){
                    TimerTaskEntry tail = root.prev;
                    timerTaskEntry.next = root;
                    timerTaskEntry.prev = tail;
                    timerTaskEntry.list = this;
                    tail.next = timerTaskEntry;
                    root.prev = timerTaskEntry;
                    taskCounter.incrementAndGet();
                    done = true;
                }
            }
        }
    }

    public void remove(TimerTaskEntry timerTaskEntry){
        synchronized (timerTaskEntry){
            if (timerTaskEntry.list.equals(this)){
                timerTaskEntry.next.prev = timerTaskEntry.prev;
                timerTaskEntry.prev.next = timerTaskEntry.next;
                timerTaskEntry.next = null;
                timerTaskEntry.prev = null;
                timerTaskEntry.list = null;
                taskCounter.decrementAndGet();
            }
        }
    }

    public long getDelay(TimeUnit unit) {
        return 0;
    }

    public int compareTo(Delayed o) {
        return 0;
    }

    public void flush() {
    }
}

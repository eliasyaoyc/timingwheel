package yychen.demo.timingwheel;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: siran.yao
 * @time: 2020/5/8:下午1:39
 */
public abstract class DelayedOperation extends TimerTask {
    private AtomicBoolean completed = new AtomicBoolean(false);

    public Boolean forceComplete() {
        if (completed.compareAndSet(false, true)) {
            onComplete();
            return true;
        }
        return false;
    }

    abstract Boolean tryComplete();

    abstract void onComplete();

    abstract Boolean isCompleted();

    abstract Boolean maybeTryComplete();

    abstract void cancel();
}

class DelayedOperationPurgatory {
    private SystemTimer systemTimer = new SystemTimer();
    private boolean reaperEnabled = true;

    private Thread expirationReaper = new Thread(()-> advanceClock(200));

    public DelayedOperationPurgatory() {
        if(reaperEnabled)
            expirationReaper.start();
    }

    public Boolean tryCompleteElseWatch(DelayedOperation operation) {
        if (operation.tryComplete())
            return true;

        //not completed , add to timingwheel
        if (!operation.isCompleted()) {
                systemTimer.add(operation);
            if (operation.isCompleted())
                operation.cancel();
        }
        return false;
    }

    private void advanceClock(long delayedMs){
        systemTimer.advanceClock(delayedMs);
    }
}

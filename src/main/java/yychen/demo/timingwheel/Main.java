package yychen.demo.timingwheel;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    public static void main(String[] args) {
        DelayedOperationPurgatory purgatory = new DelayedOperationPurgatory();
        MockDelayedOperation r1 = new MockDelayedOperation(10000L);
        MockDelayedOperation r2 = new MockDelayedOperation(10000L);
        purgatory.tryCompleteElseWatch(r1);
        purgatory.tryCompleteElseWatch(r2);
    }

    static class MockDelayedOperation extends DelayedOperation {
        private Long delayedMs;
        private ReentrantLock lock;
        private boolean completable = false;

        public MockDelayedOperation(Long delayedMs) {
            this.delayedMs = delayedMs;
        }

        @Override
        Boolean tryComplete() {
            if (completable)
                forceComplete();
            return false;
        }

        @Override
        void onComplete() {

        }

        @Override
        Boolean isCompleted() {
            return completable;
        }

        @Override
        Boolean maybeTryComplete() {
            return null;
        }

        @Override
        void cancel() {

        }
    }
}
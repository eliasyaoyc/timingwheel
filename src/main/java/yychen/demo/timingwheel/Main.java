package yychen.demo.timingwheel;

import java.util.concurrent.CountDownLatch;

public class Main {
    static int executorCount = 0;
    static int joinCount = 0;

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1000);
        SystemTimer systemTimer = new SystemTimer();
        for (int i = 1; i <= 1000; i++) {
            TimerTask timerTask = new TimerTask(() -> {
                countDownLatch.countDown();
                executorCount++;
                System.out.println(executorCount + "------------------ 开始执行");
            }, i);
            systemTimer.addTask(timerTask);
            System.out.println(i + "---------------------------加入是时间轮");
            joinCount++;
        }
        countDownLatch.await();
        System.out.println("executorCount:-----------" + executorCount);
        System.out.println("joinCount:-----------" + joinCount);
    }
}
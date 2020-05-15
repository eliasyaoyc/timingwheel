package yychen.demo.timingwheel;

import java.util.concurrent.*;

/**
 * @Author: siran.yao
 * @time: 2020/5/8:下午1:13
 * 对时间轮的包装
 */
public class SystemTimer {
    /**
     * 底层时间轮
     */
    private TimingWheel timeWheel;

    /**
     * 一个Timer只有一个delayQueue
     */
    private DelayQueue<TimerTaskList> delayQueue = new DelayQueue<>();

    /**
     * 过期任务执行线程
     */
    private ExecutorService workerThreadPool;

    /**
     * 轮询delayQueue获取过期任务线程
     */
    private ExecutorService bossThreadPool;

    /**
     * 构造函数
     */
    public SystemTimer() {
        timeWheel = new TimingWheel(1, 20, System.currentTimeMillis(), delayQueue);
        workerThreadPool = Executors.newFixedThreadPool(100);
        bossThreadPool = Executors.newFixedThreadPool(1);
        //20ms获取一次过期任务
        bossThreadPool.submit(() -> {
            while (true) {
                this.advanceClock(20);
            }
        });
    }

    /**
     * 添加任务
     */
    public void addTask(TimerTask timerTask) {
        //添加失败任务直接执行
        if (!timeWheel.addTask(timerTask)) {
            workerThreadPool.submit(timerTask.getTask());
        }
    }

    /**
     * 获取过期任务
     */
    private void advanceClock(long timeout) {
        try {
            TimerTaskList timerTaskList = delayQueue.poll(timeout, TimeUnit.MILLISECONDS);
            if (timerTaskList != null) {
                //推进时间
                timeWheel.advanceClock(timerTaskList.getExpiration());
                //执行过期任务（包含降级操作）
                timerTaskList.flush(this::addTask);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
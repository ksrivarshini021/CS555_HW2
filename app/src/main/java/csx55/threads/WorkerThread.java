package csx55.threads;

import java.util.concurrent.BlockingQueue;

public class WorkerThread implements Runnable {
    private final BlockingQueue<Runnable> taskQueue;
    private volatile boolean running = true;

    public WorkerThread(BlockingQueue<Runnable> taskQueue) {
        this.taskQueue = taskQueue;
    }

    @Override
    public void run() {
        try {
            /**
             * start taking next task and block if queue empty
             */
            while (running) {
                Runnable task = taskQueue.take();
                task.run();

            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * to stop worler gracefully
     */
    public void stopThread() {
        running = false;
    }

    /**
     * to return if the thread still is in running
     */
    public boolean isRunning(){
        return running;
    }

}

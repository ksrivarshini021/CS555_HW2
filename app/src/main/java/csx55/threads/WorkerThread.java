package csx55.threads;

import java.util.concurrent.BlockingQueue;

import csx55.hashing.Task;

public class WorkerThread implements Runnable {
    private final BlockingQueue<Task> taskQueue;
    private volatile boolean running = true;

    public WorkerThread(BlockingQueue<Task> taskQueue) {
        this.taskQueue = taskQueue;
    }

    @Override
    public void run() {
        try {
            /**
             * start taking next task and block if queue empty
             */
            while (running) {
                Task task = taskQueue.take();
                if(task != null){
                    executeTasks(task);
                }
            
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

    public void executeTasks(Task task) {
        int nonce = 0;
        int load = task.getPayload();
        for(int j = 0 ; j < 100 ; j++){
            nonce += (load * j + 1) % 13;
        }
        task.setNonce(nonce);
        task.setTimestamp();
        task.setThreadId();
    }

}

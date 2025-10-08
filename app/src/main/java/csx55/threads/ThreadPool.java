package csx55.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import csx55.hashing.Task;

public class ThreadPool {
    private final BlockingQueue<Task> taskQueue;
    private final List<Thread> workerThreads;
    private final List<WorkerThread> workers;
    private volatile boolean running = true;
    private volatile boolean stopped = false;

    /**
     * to create a thread pool bt 2-16
     * 
     * @param numThreads
     * @param k
     * @param j
     */
    public ThreadPool(int numThreads, int j, int k) {
        if (numThreads < 2 || numThreads > 16) {
            throw new IllegalArgumentException("Thread pool size invalid");
        }

        this.taskQueue = new LinkedBlockingQueue<>();
        this.workerThreads = new ArrayList<>();
        this.workers = new ArrayList<>();
        this.running = true;

        /**
         * to create worker threads and start
         */
        for (int i = 0; i < numThreads; i++) {
            WorkerThread worker = new WorkerThread(taskQueue);
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        while (running) {
                            Task task = taskQueue.take();
                            if (task != null) {
                                worker.executeTasks(task);
                            }
                            if (stopped && taskQueue.isEmpty())
                                break;
                        }
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }

                }

            }, "workerThread:" + i);
            workers.add(worker);
            workerThreads.add(thread);
            thread.start();

        }
    }

    /**
     * submiting task to thread pool
     * 
     * @param task
     * @throws InterruptedException
     */
    public void submit(Task task) throws InterruptedException {
        if (!running) {
            throw new IllegalStateException("Thread pool stopped");
        }
        taskQueue.put(task);
    }

    /**
     * to stop all worker threads gracefully
     */
    public void shutdown() {
        running = false;
        for (WorkerThread worker : workers) {
            worker.stopThread();
        }
        for (Thread thread : workerThreads) {
            thread.interrupt();
        }
    }

    public int getThreadsCount() {
        return workers.size();
    }

    /**
     * to retrive tasks from pool
     * 
     * @return
     */
    public Task getTasks() {
        return taskQueue.poll();

    }

    // public void addTask(Task task) {

    // }

    // private boolean add = false;

    public synchronized void stopAdding() {
        stopped = true;
        notifyAll();
    }

}

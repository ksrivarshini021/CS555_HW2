package csx55.wireformats;

import java.util.concurrent.atomic.AtomicInteger;

public class UpdateStats {
    private AtomicInteger task;
    private AtomicInteger generatedCount;
    private AtomicInteger pulledCount;
    private AtomicInteger pushedCount;
    private AtomicInteger completedCount;

    public UpdateStats(){
        this.task = new AtomicInteger(0);
        this.generatedCount = new AtomicInteger(0);
        this.pulledCount = new AtomicInteger(0);
        this.pushedCount = new AtomicInteger(0);
        this.completedCount = new AtomicInteger(0);
    }

    public void updateGeneratedCount(int value){
        this.generatedCount.set(value);
    }

    public void updatePullCount(){
        this.pulledCount.getAndIncrement();
    }

    public void updatePushCount(){
        this.pushedCount.getAndIncrement();
    }

    public void updateCompletedCount(){
        this.completedCount.getAndIncrement();
    }

    public void reset(){
        this.task.set(0);
        this.generatedCount.set(0);
        this.pulledCount.set(0);
        this.pushedCount.set(0);
        this.completedCount.set(0);
    }

    public int getGeneratedTasks() {
        return generatedCount.get();
    }

    public int getPulledTasks() {
        return pulledCount.get();
    }

    public int getPushedTasks() {
        return pushedCount.get();
    }

    public int getCompletedTasks() {
       return completedCount.get();
    }
    public void addTask(){
        this.task.getAndIncrement();
    }

    public void removeTask(){
        this.task.getAndDecrement();
    }
    public void setcompletedTotalTasks(int completed){
        this.completedCount.set(completed);
    }
}

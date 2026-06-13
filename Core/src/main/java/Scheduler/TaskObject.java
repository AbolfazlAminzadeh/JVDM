package Scheduler;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TaskObject {
    private final Task task;
    private volatile Status taskStatus;
    private volatile long lastExecute = System.nanoTime();
    private final boolean repeat;
    private final long interval;
    private final TimeUnit timeUnit;
    private final AtomicLong executionTimes = new AtomicLong(0);

    private String lastExecuteString = "S@LAM";

    public TaskObject(Task task, boolean isOnce, int interval, TimeUnit unit) {
        this.task = task;
        this.repeat = isOnce;
        this.interval = interval;
        this.timeUnit = unit;
        taskStatus = Status.Pending;
    }

    public Task getTask() {
        return task;
    }

    public Status getTaskStatus() {
        return taskStatus;
    }

    public boolean isRepeatable() {
        return repeat;
    }

    public long getInterval() {
        return interval;
    }

    public long getLastExecute() {
        return lastExecute;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public long getExecutionTimes() {
        return executionTimes.get();
    }

    public long getLastTime() {
        return lastExecute;
    }

    public void setLastTime(long nanoTime) {
        lastExecute = nanoTime;
        lastExecuteString = Duration.ofNanos(nanoTime).toString();
    }

    public void setStatus(Status status) {
        this.taskStatus = status;
    }

    @Override
    public String toString() {
        return "["+task.getID()+"]"+" Name: "+task.getName()+", Status: ,"+taskStatus+"Last Executed: "+lastExecuteString;
    }

}

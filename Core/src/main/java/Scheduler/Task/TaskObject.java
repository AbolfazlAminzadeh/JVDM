package Scheduler.Task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TaskObject {

    private final Task task;
    private volatile Status taskStatus;
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private volatile String lastExecuteString = "Not Executed Yet";
    private volatile long lastExecutedNanoTime = -1;
    private final boolean repeat;
    private final int interval;
    private final TimeUnit timeUnit;
    private final AtomicLong executionTimes = new AtomicLong(0);

    public TaskObject(Task task, boolean isOnce, int interval, TimeUnit unit) {
        this.task = task;
        this.repeat = !isOnce;
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


    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public long getExecutionTimes() {
        return executionTimes.get();
    }

    public void setTimeNow() {
        this.lastExecutedNanoTime = System.nanoTime();
        lastExecuteString = LocalDateTime.now().format(formatter);
    }

    public void setStatus(Status status) {
        this.taskStatus = status;
    }

    public void incrementExecutionTime() {
        executionTimes.getAndIncrement();
    }

    // 30NS
    @Override
    public String toString() {
        return "["+task.getID()+"]"+" Name: "+task.getName()+", Status: "+taskStatus+", Last Executed: "+lastExecuteString;
    }

}

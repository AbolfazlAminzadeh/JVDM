package Scheduler;


import Scheduler.Task.Status;
import Scheduler.Task.Task;
import Scheduler.Task.TaskObject;
import Statics.Initializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Scheduler {

    private final ScheduledExecutorService service;
    private final Map<CharSequence, TaskObject> tasks = new ConcurrentHashMap<>();
    private final Map<CharSequence, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();

    private static final Scheduler instance = new Scheduler(Initializer.schedulerThread);

    public static Scheduler getInstance() {
        return instance;
    }

    public Scheduler(byte threads) {
        service = Executors.newScheduledThreadPool(
                threads,
                Initializer.importantFactory
        );
    }

    public void executeOnce(Task task, int delay, TimeUnit unit) {
        TaskObject object = new TaskObject(task,true,delay,unit);
        tasks.put(task.getID(),object);
        futures.put(task.getID(), service.schedule(() -> executeTask(object),delay,unit));
    }

    public void executeOnce(Task task, int delay) {
        executeOnce(task,delay,TimeUnit.MILLISECONDS);
    }

    public void executeLoop(Task task, int delay, int interval, TimeUnit unit) {
        TaskObject object = new TaskObject(task,false,delay,unit);
        tasks.put(task.getID(),object);
        futures.put(task.getID(), service.scheduleWithFixedDelay(() -> executeTask(object),delay,interval,unit));
    }

    public void executeLoop(Task task,int delay, int interval) {
        executeLoop(task,delay,interval,TimeUnit.MILLISECONDS);
    }

    public void executeLoop(Task task,int interval, TimeUnit unit) {
        executeLoop(task,0,interval,unit);
    }

    public void executeLoop(Task task,int interval) {
        executeLoop(task,0,interval,TimeUnit.MILLISECONDS);
    }

    private void executeTask(TaskObject object) {
        Task task = object.getTask();
        try {
            object.setStatus(Status.Running);
            object.setTimeNow();

            task.execute();

            object.incrementExecutionTime();

            if (object.isRepeatable())  {
                object.setStatus(Status.Pending);
            } else {
                object.setStatus(Status.Complete);
                futures.remove(task.getID());
            }

        } catch (Exception e) {
            task.onException(e);
            object.setStatus(Status.Failed);
            if (!object.isRepeatable()) {
                tasks.remove(task.getID());
            }
        }
    }

    public boolean cancel(CharSequence taskID) {
        ScheduledFuture<?> future = futures.get(taskID);
        TaskObject object = tasks.get(taskID);

        if (future == null || future.isDone()) return false;

        future.cancel(true);
        if (object != null) object.setStatus(Status.Cancelled);

        return true;
    }

    public List<TaskObject> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void shutdown() throws InterruptedException {
        service.shutdown();
        if (service.awaitTermination(5,TimeUnit.SECONDS)) {
            service.shutdownNow();
        }
    }

}

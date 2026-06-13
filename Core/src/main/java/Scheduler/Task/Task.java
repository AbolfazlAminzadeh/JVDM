package Scheduler.Task;

public interface Task {

    CharSequence getID();
    CharSequence getName();
    void execute() throws Exception;
    void onException(Exception e);

}

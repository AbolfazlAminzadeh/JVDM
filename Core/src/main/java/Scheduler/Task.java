package Scheduler;

public interface Task {

    int getID();
    CharSequence getName();
    void execute() throws Exception;

}

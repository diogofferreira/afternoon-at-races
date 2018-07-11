package serverStates;

import main.EventVariables;
import messageTypes.StableMessageTypes;

import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Definition of the General Repository states.
 */
public class GeneralRepositoryClientsState {
    /**
     * Instance of a monitor.
     */
    private Lock mutex;

    /**
     * Condition variable where clients' APS will wait in case of another thread
     * is already executing the same method.
     */
    private Condition enter;

    /**
     * Condition variable where clients' APS will wait in case of another thread
     * is already executing the same method.
     */
    private Condition exit;

    private Thread threadInside;

    public GeneralRepositoryClientsState() {
        this.mutex = new ReentrantLock();
        this.enter = this.mutex.newCondition();
        this.exit = this.mutex.newCondition();
        this.threadInside = null;
    }

    public void enterMonitor() {
        mutex.lock();

        while (this.threadInside != null) {
            try {
                enter.await();
            } catch (InterruptedException ignored) {}
        }

        this.threadInside = Thread.currentThread();
        this.exit.signalAll();

        mutex.unlock();
    }

    public void exitMonitor() {
        mutex.lock();

        while (this.threadInside != Thread.currentThread()) {
            try {
                exit.await();
            } catch (InterruptedException ignored) {}
        }

        this.threadInside = null;
        this.enter.signalAll();

        mutex.unlock();
    }

    public Thread getThreadInside() {
        return threadInside;
    }

    public void setThreadInside(Thread threadInside) {
        this.threadInside = threadInside;
    }

}
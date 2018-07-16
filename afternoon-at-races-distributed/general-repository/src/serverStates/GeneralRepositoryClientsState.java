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

    /**
     * Thread inside the monitor.
     */
    private Thread threadInside;

    /**
     * Number of requests made by this entity.
     */
    private int requests;

    public GeneralRepositoryClientsState() {
        this.mutex = new ReentrantLock();
        this.enter = this.mutex.newCondition();
        this.exit = this.mutex.newCondition();
        this.threadInside = null;
        this.requests = 0;
    }

    /**
     * Method that lets a thread enter the monitor. Blocks if not able to.
     */
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

    /**
     * Method that lets a thread exit the monitor waking up all the rest.
     */
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

    /**
     * Method that returns the Thread inside the monitor.
     * @return The Thread inside the monitor.
     */
    public Thread getThreadInside() {
        return threadInside;
    }

    /**
     * Method that sets the Thread inside the monitor.
     * @param threadInside The Thread inside the monitor.
     */
    public void setThreadInside(Thread threadInside) {
        this.threadInside = threadInside;
    }

    /**
     * Method that returns the number of requests made by this entity.
     * @return Number of requests made by this entity.
     */
    public int getRequests() {
        return requests;
    }

    /**
     * Method that increases by 1 unit the number of requests.
     */
    public void increaseRequests() {
        this.requests++;
    }

}
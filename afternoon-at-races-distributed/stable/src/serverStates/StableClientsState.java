package serverStates;

import main.EventVariables;
import messageTypes.ControlCentreMessageTypes;
import messageTypes.StableMessageTypes;

import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Definition of the Stable states.
 */
public class StableClientsState {
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

    private int entityId;

    private StableMessageTypes mType;

    private Thread threadInside;

    private int raceNumber;

    private double[] odds;

    public StableClientsState(int entityId, StableMessageTypes mType, int raceNumber) {
        if (entityId < 0 || raceNumber > EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Illegal arguments");

        this.mutex = new ReentrantLock();
        this.enter = this.mutex.newCondition();
        this.exit = this.mutex.newCondition();
        this.entityId = entityId;
        this.mType = mType;
        this.raceNumber = raceNumber;
        this.threadInside = null;
    }

    public StableClientsState(String info) {
        if (info == null || info.length() < 1)
            throw new IllegalArgumentException("Illegal info string");

        try {
            String[] args = info.split("\\|");

            this.mutex = new ReentrantLock();
            this.enter = this.mutex.newCondition();
            this.exit = this.mutex.newCondition();
            this.entityId = Integer.parseInt(args[0]);
            this.mType = StableMessageTypes.getType(Integer.parseInt(args[1]));
            this.raceNumber = Integer.parseInt(args[2]);
            this.threadInside = null;

            String[] w = args[3].equals("null") ? null :
                    args[3].substring(1, args[4].length()-1).split(",");
            if (w != null) {
                this.odds = new double[w.length];
                for (int i = 0; i < w.length; i++)
                    this.odds[i] = Double.parseDouble(w[i].trim());
            }


        } catch (Exception e) {
            System.err.println("Invalid info arguments");
            System.exit(1);
        }

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

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public StableMessageTypes getmType() {
        return mType;
    }

    public void setmType(StableMessageTypes mType) {
        this.mType = mType;
    }

    public Thread getThreadInside() {
        return threadInside;
    }

    public void setThreadInside(Thread threadInside) {
        this.threadInside = threadInside;
    }

    public int getRaceNumber() {
        return raceNumber;
    }

    public void setRaceNumber(int raceNumber) {
        this.raceNumber = raceNumber;
    }

    public double[] getOdds() {
        return odds;
    }

    public void setOdds(double[] odds) {
        this.odds = odds;
    }

    @Override
    public String toString() {
        int mId = mType == null ? -1 : mType.getId();
        return entityId + "|" + mId + "|" + raceNumber +
                "|" + Arrays.toString(odds);
    }
}
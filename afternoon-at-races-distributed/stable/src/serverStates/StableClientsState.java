package serverStates;

import main.EventVariables;
import messageTypes.ControlCentreMessageTypes;
import messageTypes.StableMessageTypes;

import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Definition of the Stable clients states.
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

    /**
     * Thread inside the monitor.
     */
    private Thread threadInside;

    /**
     * Identifier of the entity which possesses this state log.
     */
    private int entityId;

    /**
     * Last registered message sent.
     */
    private StableMessageTypes mType;

    /**
     * Number of requests made by this entity.
     */
    private int requests;

    /**
     * Current identifier of the race.
     */
    private int raceNumber;

    /**
     * Odds of horses winning.
     */
    private double[] odds;

    /**
     * Constructor (type 1).
     * @param entityId Identifier of the entity which possesses this state log.
     * @param mType Last registered message sent.
     * @param raceNumber Current identifier of the race.
     */
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
        this.requests = 0;
    }

    /**
     * Constructor (type 2).
     * @param info Contents of the log.
     */
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
                    args[3].substring(1, args[3].length()-1).split(",");
            if (w != null) {
                this.odds = new double[w.length];
                for (int i = 0; i < w.length; i++)
                    this.odds[i] = Double.parseDouble(w[i].trim());
            }

            this.requests = Integer.parseInt(args[4]);

        } catch (Exception e) {
            System.err.println("Invalid info arguments");
            System.exit(1);
        }

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
     * Method that returns the identifier of the entity which possesses this state log.
     * @return Identifier of the entity which possesses this state log.
     */
    public int getEntityId() {
        return entityId;
    }

    /**
     * Method that sets the identifier of the entity which possesses this state log.
     * @param entityId Identifier of the entity which possesses this state log.
     */
    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    /**
     * Method that returns the last registered message sent.
     * @return The last registered message sent.
     */
    public StableMessageTypes getmType() {
        return mType;
    }

    /**
     * Method that sets the last registered message sent.
     * @param mType The last registered message sent.
     */
    public void setmType(StableMessageTypes mType) {
        this.mType = mType;
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

    /**
     * Method that returns the current identifier of the race.
     * @return Current identifier of the race.
     */
    public int getRaceNumber() {
        return raceNumber;
    }

    /**
     * Method that sets the current identifier of the race.
     * @param raceNumber Current identifier of the race.
     */
    public void setRaceNumber(int raceNumber) {
        this.raceNumber = raceNumber;
    }

    /**
     * Method that returns the odds of horses winning.
     * @return Odds of horses winning.
     */
    public double[] getOdds() {
        return odds;
    }

    /**
     * Method that sets the odds of horses winning.
     * @param odds Odds of horses winning.
     */
    public void setOdds(double[] odds) {
        this.odds = odds;
    }

    /**
     * Method that returns a textual representation of the logging state.
     * @return Textual representation of the logging state.
     */
    @Override
    public String toString() {
        int mId = mType == null ? -1 : mType.getId();
        return entityId + "|" + mId + "|" + raceNumber +
                "|" + Arrays.toString(odds) + "|" + this.requests;
    }
}
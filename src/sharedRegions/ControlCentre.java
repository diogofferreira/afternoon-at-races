package sharedRegions;

import main.EventVariables;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ControlCentre {

    private Lock mutex;
    private Condition horsesInPaddock, waitForRace, checkHorses, watchingRace, startingRace;

    private int raceNumber;

    public ControlCentre() {
        this.mutex = new ReentrantLock();
        this.horsesInPaddock = this.mutex.newCondition();
        this.waitForRace = this.mutex.newCondition();
        this.checkHorses = this.mutex.newCondition();
        this.watchingRace = this.mutex.newCondition();
        this.startingRace = this.mutex.newCondition();
    }

    public void summonHorsesToPaddock(int raceNumber) {
        mutex.lock();

        this.raceNumber = raceNumber;
        /* broker wait */
        try {
            horsesInPaddock.await();
        } catch (InterruptedException e) {}

        mutex.unlock();
    }

    public boolean waitForNextRace(int spectatorId) {
        mutex.lock();
        /* spectators wait */
        try {
            waitForRace.await();
        } catch (InterruptedException e) {}

        mutex.unlock();
        return raceNumber < EventVariables.NUMBER_OF_RACES;
    }

    public void proceedToPaddock() {
        mutex.lock();

        /* notify all spectators */
        waitForRace.signalAll();

        mutex.unlock();
    }

    public void goCheckHorses() {
        mutex.lock();

        /* notify broker */
        horsesInPaddock.signal();

        mutex.unlock();
    }

    public void goWatchTheRace(int spectatorId) {
        mutex.lock();
        /* spectators wait */
        try {
            watchingRace.await();
        } catch (InterruptedException e) {}

        mutex.unlock();
    }

    public void startTheRace() {
        mutex.lock();
        /* broker wait */
        try {
            startingRace.await();
        } catch (InterruptedException e) {}

        mutex.unlock();
    }

    public void finishTheRace() {
        this.raceFinished = true;
    }

    public void reportResults() {
        /* save horses winners */
        /* notify all spectators */
    }

    public boolean areThereAnyWinners() {
        /* notify all spectators */
        return false;
    }

    public boolean haveIWon(int horseID) {
        /* checks if winner is the one he/she bet */
    }

    public void entertainTheGuests() {
        /* broker just playing host, end the afternoon */
    }

    public void relaxABit(int spectatorID) {
        /* just relax, end the afternoon */
    }

    public int getRaceNumber() {
        return this.raceNumber;
    }

}

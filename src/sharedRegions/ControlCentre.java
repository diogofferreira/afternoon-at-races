package sharedRegions;

import main.EventVariables;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ControlCentre {

    private RacingTrack racingTrack;

    private Lock mutex;
    private Condition horsesInPaddock, waitForRace, watchingRace, startingRace;

    private int raceNumber;
    private boolean raceFinished;

    public ControlCentre(RacingTrack r) {
        this.racingTrack = r;
        this.mutex = new ReentrantLock();
        this.horsesInPaddock = this.mutex.newCondition();
        this.waitForRace = this.mutex.newCondition();
        this.watchingRace = this.mutex.newCondition();
        this.startingRace = this.mutex.newCondition();
        this.raceFinished = false;
    }

    public void summonHorsesToPaddock(int raceNumber) {
        mutex.lock();

        this.raceNumber = raceNumber;
        // broker wait
        try {
            horsesInPaddock.await();
        } catch (InterruptedException ignored) {}

        mutex.unlock();
    }

    public boolean waitForNextRace() {
        mutex.lock();

        // spectators wait
        try {
            waitForRace.await();
        } catch (InterruptedException ignored) {}

        mutex.unlock();

        return raceNumber < EventVariables.NUMBER_OF_RACES;
    }

    public void proceedToPaddock() {
        mutex.lock();

        // notify all spectators
        waitForRace.signalAll();

        mutex.unlock();
    }

    public void goCheckHorses() {
        mutex.lock();

        // notify broker
        horsesInPaddock.signal();

        mutex.unlock();
    }

    public void goWatchTheRace() {
        mutex.lock();

        // spectators wait
        try {
            watchingRace.await();
        } catch (InterruptedException ignored) {}

        mutex.unlock();
    }

    public void startTheRace() {
        mutex.lock();

        // broker wait
        try {
            raceFinished = false;
            startingRace.await();
        } catch (InterruptedException ignored) {}

        mutex.unlock();
    }

    public void finishTheRace() {
        this.raceFinished = true;
    }

    public void reportResults() {
        mutex.lock();

        // notify all spectators
        watchingRace.signalAll();

        mutex.unlock();
    }

    public boolean haveIWon(int horseID) {
        boolean won;

        mutex.lock();

        // checks if winner is the one he/she bet
        won = racingTrack.getWinners().contains(horseID);

        mutex.unlock();

        return won;
    }

    public void entertainTheGuests() {
        /* broker just playing host, end the afternoon */
        // TODO: update state
    }

    public void relaxABit(int spectatorID) {
        /* just relax, end the afternoon */
        // TODO: update state
    }

    public int getRaceNumber() {
        return this.raceNumber;
    }

}

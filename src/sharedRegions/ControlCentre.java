package sharedRegions;

import entities.Broker;
import entities.Horse;
import entities.Spectator;
import main.EventVariables;
import states.BrokerState;
import states.SpectatorState;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class ControlCentre {

    private RacingTrack racingTrack;

    private Lock mutex;
    private Condition horsesInPaddock, waitForRace, watchingRace, startingRace;

    private int raceNumber;

    private int[] winners;

    public ControlCentre(RacingTrack r) {
        this.racingTrack = r;
        this.mutex = new ReentrantLock();
        this.horsesInPaddock = this.mutex.newCondition();
        this.waitForRace = this.mutex.newCondition();
        this.watchingRace = this.mutex.newCondition();
        this.startingRace = this.mutex.newCondition();
    }

    public void summonHorsesToPaddock(int raceNumber) {
        Broker b;
        mutex.lock();

        b = (Broker)Thread.currentThread();
        b.setBrokerState(BrokerState.ANNOUNCING_NEXT_RACE);

        this.raceNumber = raceNumber;
        // broker wait
        try {
            horsesInPaddock.await();
        } catch (InterruptedException ignored) {}

        mutex.unlock();
    }

    public boolean waitForNextRace() {
        Spectator s;
        boolean toRtn;

        mutex.lock();

        s = (Spectator)Thread.currentThread();
        s.setSpectatorState(SpectatorState.WAITING_FOR_A_RACE_TO_START);

        // spectators wait
        try {
            waitForRace.await();
        } catch (InterruptedException ignored) {}

        toRtn = raceNumber < EventVariables.NUMBER_OF_RACES;

        mutex.unlock();

        return toRtn;
    }

    public void proceedToPaddock() {
        mutex.lock();

        // notify all spectators
        waitForRace.signalAll();

        mutex.unlock();
    }

    public void goCheckHorses() {
        Spectator s;
        mutex.lock();

        s = (Spectator)Thread.currentThread();
        s.setSpectatorState(SpectatorState.APPRAISING_THE_HORSES);

        // notify broker
        horsesInPaddock.signal();

        mutex.unlock();
    }

    public void goWatchTheRace() {
        Spectator s;
        mutex.lock();

        s = (Spectator)Thread.currentThread();
        s.setSpectatorState(SpectatorState.WATCHING_A_RACE);

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
            startingRace.await();
        } catch (InterruptedException ignored) {}

        mutex.unlock();
    }

    public void finishTheRace() {
        mutex.lock();

        // notify broker
        startingRace.signalAll();

        mutex.unlock();
    }

    public void reportResults() {
        Broker b;
        mutex.lock();

        b = (Broker)Thread.currentThread();
        b.setBrokerState(BrokerState.SUPERVISING_THE_RACE);

        winners = racingTrack.getWinners();

        // notify all spectators
        watchingRace.signalAll();

        mutex.unlock();
    }

    public boolean haveIWon(int horseID) {
        boolean won;
        Spectator s;
        mutex.lock();

        s = (Spectator)Thread.currentThread();
        s.setSpectatorState(SpectatorState.WATCHING_A_RACE);

        // checks if winner is the one he/she bet
        won = IntStream.of(winners).anyMatch(w -> w == horseID);

        mutex.unlock();

        return won;
    }

    public void entertainTheGuests() {
        Broker b;
        mutex.lock();

        b = (Broker)Thread.currentThread();
        b.setBrokerState(BrokerState.PLAYING_HOST_AT_THE_BAR);
        /* broker just playing host, end the afternoon */

        mutex.unlock();
    }

    public void relaxABit() {
        Spectator s;
        mutex.lock();

        s = (Spectator)Thread.currentThread();
        s.setSpectatorState(SpectatorState.CELEBRATING);
        /* just relax, end the afternoon */

        mutex.unlock();
    }
}

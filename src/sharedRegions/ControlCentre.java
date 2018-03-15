package sharedRegions;

import entities.Broker;
import entities.Horse;
import entities.Spectator;
import generalRepository.GeneralRepository;
import main.EventVariables;
import states.BrokerState;
import states.SpectatorState;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class ControlCentre {

    private RacingTrack racingTrack;
    private GeneralRepository generalRepository;

    private Lock mutex;
    private Condition horsesInPaddock, waitForRace, watchingRace, startingRace;

    private int raceNumber;

    private int[] winners;

    public ControlCentre(RacingTrack r, GeneralRepository gr) {
        if (r == null)
            throw new IllegalArgumentException("Invalid Racing Track.");
        if (gr == null)
            throw new IllegalArgumentException("Invalid General Repository.");

        this.generalRepository = gr;
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

        generalRepository.setRaceNumber(raceNumber);

        b = (Broker)Thread.currentThread();
        b.setBrokerState(BrokerState.ANNOUNCING_NEXT_RACE);
        generalRepository.setBrokerState(BrokerState.ANNOUNCING_NEXT_RACE);

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
        generalRepository.setSpectatorState(s.getID(),
                SpectatorState.WAITING_FOR_A_RACE_TO_START);

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
        mutex.lock();

        // notify broker
        horsesInPaddock.signal();

        mutex.unlock();
    }

    public void goWatchTheRace() {
        Spectator s;
        mutex.lock();

        s = (Spectator)Thread.currentThread();
        s.setSpectatorState(SpectatorState.WATCHING_A_RACE);
        generalRepository.setSpectatorState(s.getID(),
                SpectatorState.WATCHING_A_RACE);

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

        // Notify general repository to clear all horse related info
        generalRepository.resetRace();

        // notify broker
        startingRace.signalAll();

        mutex.unlock();
    }

    public void reportResults() {
        Broker b;
        mutex.lock();

        b = (Broker)Thread.currentThread();
        b.setBrokerState(BrokerState.SUPERVISING_THE_RACE);
        generalRepository.setBrokerState(BrokerState.SUPERVISING_THE_RACE);

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
        generalRepository.setSpectatorState(s.getID(),
                SpectatorState.WATCHING_A_RACE);

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
        generalRepository.setBrokerState(BrokerState.PLAYING_HOST_AT_THE_BAR);
        /* broker just playing host, end the afternoon */

        mutex.unlock();
    }

    public void relaxABit() {
        Spectator s;
        mutex.lock();

        s = (Spectator)Thread.currentThread();
        s.setSpectatorState(SpectatorState.CELEBRATING);
        generalRepository.setSpectatorState(s.getID(),
                SpectatorState.CELEBRATING);
        /* just relax, end the afternoon */

        mutex.unlock();
    }

    public void setRacingTrack(RacingTrack racingTrack) {
        this.racingTrack = racingTrack;
    }
}

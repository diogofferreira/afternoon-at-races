package sharedRegions;

import entities.Broker;
import entities.Spectator;
import main.EventVariables;
import states.BrokerState;
import states.SpectatorState;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class ControlCentre {

    private GeneralRepository generalRepository;

    private Lock mutex;
    private Condition horsesInPaddock, waitForRace, watchingRace, startingRace;

    private Stable stable;

    private boolean spectatorsInPaddock;
    private boolean spectatorsCanProceed;

    private boolean raceFinished;
    private boolean reportsPosted;

    private int spectatorsLeavingRace;

    private int[] winners;

    public ControlCentre(GeneralRepository generalRepository, Stable stable) {
        if (generalRepository == null)
            throw new IllegalArgumentException("Invalid General Repository.");
        if (stable == null)
            throw new IllegalArgumentException("Invalid Stable.");

        this.generalRepository = generalRepository;
        this.stable = stable;
        this.mutex = new ReentrantLock();
        this.horsesInPaddock = this.mutex.newCondition();
        this.waitForRace = this.mutex.newCondition();
        this.watchingRace = this.mutex.newCondition();
        this.startingRace = this.mutex.newCondition();
        this.spectatorsInPaddock = false;

        this.spectatorsCanProceed = false;
        this.raceFinished = false;
        this.reportsPosted = false;
        this.spectatorsLeavingRace = 0;
    }

    public void summonHorsesToPaddock(int raceNumber) {
        Broker b;
        mutex.lock();

        // Restart variables

        reportsPosted = false;

        generalRepository.setRaceNumber(raceNumber);

        b = (Broker)Thread.currentThread();
        b.setBrokerState(BrokerState.ANNOUNCING_NEXT_RACE);
        generalRepository.setBrokerState(BrokerState.ANNOUNCING_NEXT_RACE);

        stable.summonHorsesToPaddock(raceNumber);

        // broker wait
        while (!spectatorsInPaddock) {
            try {
                horsesInPaddock.await();
            } catch (InterruptedException ignored) {}
        }

        spectatorsInPaddock = false;

        mutex.unlock();
    }

    public void waitForNextRace() {
        Spectator s;

        mutex.lock();

        s = (Spectator)Thread.currentThread();
        s.setSpectatorState(SpectatorState.WAITING_FOR_A_RACE_TO_START);
        generalRepository.setSpectatorState(s.getID(),
                SpectatorState.WAITING_FOR_A_RACE_TO_START);

        while (!spectatorsCanProceed) {
            // spectators wait
            try {
                waitForRace.await();
            } catch (InterruptedException ignored) {}
        }

        mutex.unlock();
    }

    public void proceedToPaddock() {
        mutex.lock();

        // notify all spectators
        spectatorsCanProceed = true;
        waitForRace.signalAll();

        mutex.unlock();
    }

    public void goCheckHorses() {
        mutex.lock();

        // notify broker
        spectatorsInPaddock = true;
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
        /*while (!reportsPosted) {
            try {
                watchingRace.await();
            } catch (InterruptedException ignored) { }
        }*/


        if (!reportsPosted) {
            try {
                watchingRace.await();
            } catch (InterruptedException ignored) { }
        }

        if (++spectatorsLeavingRace == EventVariables.NUMBER_OF_SPECTATORS) {
            reportsPosted = false;
            spectatorsLeavingRace = 0;
        }

        mutex.unlock();
    }

    public void startTheRace() {
        mutex.lock();

        // broker wait
        while (!raceFinished) {
            try {
                startingRace.await();
            } catch (InterruptedException ignored) { }
        }

        raceFinished = false;
        mutex.unlock();
    }

    public void finishTheRace(int[] winners) {
        mutex.lock();

        this.winners = winners;

        // Notify general repository to clear all horse related info
        generalRepository.resetRace();

        this.spectatorsCanProceed = false;

        // notify broker
        raceFinished = true;
        startingRace.signal();

        mutex.unlock();
    }

    public int[] reportResults() {
        mutex.lock();

        // notify all spectators
        reportsPosted = true;
        watchingRace.signalAll();

        mutex.unlock();

        return this.winners;
    }

    public boolean haveIWon(int horseID) {
        boolean won;
        //Spectator s;
        mutex.lock();

        // checks if winner is the one he/she bet
        won = IntStream.of(winners).anyMatch(w -> w == horseID);

        mutex.unlock();

        return won;
    }

    public void relaxABit() {
        Spectator s;
        mutex.lock();

        /// just relax, end the afternoon
        s = (Spectator)Thread.currentThread();
        s.setSpectatorState(SpectatorState.CELEBRATING);
        generalRepository.setSpectatorState(s.getID(),
                SpectatorState.CELEBRATING);

        mutex.unlock();
    }
}

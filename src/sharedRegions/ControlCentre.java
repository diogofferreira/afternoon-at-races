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

/**
 * The Control Centre is a shared region where the Broker will supervise the race
 * and where the Spectators will watch the races.
 */
public class ControlCentre {

    /**
     * Instance of a monitor.
     */
    private Lock mutex;

    /**
     * Conditional variable where the Broker will wait while the Spectators are
     * appraising the Horses at the Paddock.
     */
    private Condition horsesInPaddock;

    /**
     * Conditional variable where the Spectators will wait for the next race
     * announced by the Broker.
     */
    private Condition waitForRace;

    /**
     * Conditional variable where the Broker will wait while supervising the race.
     */
    private Condition startingRace;

    /**
     * Conditional variable where the Spectators will wait while watching a race.
     */
    private Condition watchingRace;

    /**
     * Flag that signals if the Spectators are still at the Paddock.
     */
    private boolean spectatorsInPaddock;

    /**
     * Flag that signals if the Spectators can proceed to the Paddock.
     */
    private boolean spectatorsCanProceed;

    /**
     * Flag that signals the Broker if that race has already finished.
     */
    private boolean raceFinished;

    /**
     * Flag that signals the Spectators waiting for the results of the race
     * to be announced.
     */
    private boolean reportsPosted;

    /**
     * Counter that increments each time a spectator is waken up by the announcing
     * of the race results.
     */
    private int spectatorsLeavingRace;

    /**
     * Array that contains the raceIdx of the Horses winners of the race.
     */
    private int[] winners;

    /**
     * Instance of the shared region General Repository.
     */
    private GeneralRepository generalRepository;

    /**
     * Instance of the shared region Stable.
     */
    private Stable stable;

    /**
     * Creates a new instance of Control Centre.
     * @param generalRepository Reference to an instance of the shared region
     *                          General Repository.
     * @param stable Reference to an instance of the shared region Stable.
     */
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

    /**
     * Method invoked by 
     * @param raceNumber
     */
    public void summonHorsesToPaddock(int raceNumber) {
        Broker b;
        mutex.lock();

        // Restart variables
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
        while (!reportsPosted) {
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

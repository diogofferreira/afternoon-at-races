package sharedRegions;

import interfaces.ControlCentreInt;
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
public class ControlCentre implements ControlCentreInt {

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
     * Counter that increments each time a spectator is waken up by the
     * announcing of the race results.
     */
    private int spectatorsLeavingRace;

    /**
     * Flag that signals if the event has already ended;
     */
    private boolean eventEnded;

    /**
     * Array that contains the standings of the Horses in the race.
     */
    private int[] standings;

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
        this.eventEnded = false;
    }

    /**
     * Method invoked by the Broker in order to start the event. It just simply
     * updates the Broker state and updates the General Repository.
     */
    @Override
    public void openTheEvent() {
        mutex.lock();

        generalRepository.setBrokerState(BrokerState.OPENING_THE_EVENT);

        mutex.unlock();
    }

    /**
     * Method invoked by Broker, signaling the start of the event.
     * The Broker updates the current raceID and sets his state to
     * ANNOUNCING_NEXT_RACE, while signalling the Horses to proceed to Paddock.
     * @param raceID The ID of the race that will take place.
     */
    @Override
    public void summonHorsesToPaddock(int raceID) {
        mutex.lock();

        // Restart variables
        // Notify general repository to clear all horse related info
        generalRepository.initRace(raceID);

        generalRepository.setBrokerState(BrokerState.ANNOUNCING_NEXT_RACE);

        stable.summonHorsesToPaddock(raceID);

        // broker wait
        while (!spectatorsInPaddock) {
            try {
                horsesInPaddock.await();
            } catch (InterruptedException ignored) {}
        }

        spectatorsInPaddock = false;

        mutex.unlock();
    }

    /**
     * This method is invoked by every Spectator while they're waiting for
     * a race to start.
     * While waiting here, they update their state to WAITING_FOR_A_RACE_TO_START.
     * @param spectatorID ID of the Spectator arriving to the Control Centre to
     *                    await for the next race.
     * @return True if there's still a race next.
     */
    @Override
    public boolean waitForNextRace(int spectatorID) {
        boolean isThereARace;

        mutex.lock();

        generalRepository.setSpectatorState(spectatorID,
                SpectatorState.WAITING_FOR_A_RACE_TO_START);

        while (!(spectatorsCanProceed || eventEnded)) {
            // spectators wait
            try {
                waitForRace.await();
            } catch (InterruptedException ignored) {}
        }

        isThereARace = !eventEnded;

        mutex.unlock();

        return isThereARace;
    }

    /**
     * Method invoked by the last Horse/Jockey pair of the current race to arrive
     * to the Paddock, thus waking up all the Spectators to proceed to Paddock
     * and appraise the horses.
     */
    @Override
    public void proceedToPaddock() {
        mutex.lock();

        // notify all spectators
        spectatorsCanProceed = true;
        waitForRace.signalAll();

        mutex.unlock();
    }

    /**
     * Method invoked by the last Horse/Jockey pair arriving to Paddock in order
     * to wake up the Broker.
     */
    @Override
    public void goCheckHorses() {
        mutex.lock();

        // notify broker
        spectatorsInPaddock = true;
        horsesInPaddock.signal();

        mutex.unlock();
    }

    /**
     * Method invoked by each Spectator before the start of each race.
     * They will block in WATCHING_A_RACE state until the Broker reports the
     * results of the race.
     * @param spectatorID ID of the Spectator watching the race.
     */
    @Override
    public void goWatchTheRace(int spectatorID) {
        mutex.lock();

        generalRepository.setSpectatorState(spectatorID,
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

    /**
     * Method invoked by the Broker.
     * He'll wait here until the last Horse/Jockey pair to cross the finish line
     * wakes him up.
     */
    @Override
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

    /**
     * Method invoked by the last Horse/Jockey pair to cross the finish line.
     * The Broker will be notified to wake up and to report the results.
     * @param standings An array of standings of the Horses that in the race.
     */
    @Override
    public void finishTheRace(int[] standings) {
        mutex.lock();

        this.standings = standings;

        this.spectatorsCanProceed = false;

        // notify broker
        raceFinished = true;
        startingRace.signal();

        mutex.unlock();
    }

    /**
     * Method invoked by the Broker signalling all Spectators that the results
     * of the race have been reported.
     * @return An array of Horses' raceIdx that won the race.
     */
    @Override
    public int[] reportResults() {
        int w[];
        mutex.lock();

        generalRepository.setHorsesStanding(standings);

        // set winners list
        w = IntStream.range(0, standings.length).
                filter(i -> standings[i] == 1).toArray();

        // notify all spectators
        reportsPosted = true;
        watchingRace.signalAll();

        mutex.unlock();

        return w;
    }

    /**
     * Method invoked by each Spectator to verify if they betted on a winning
     * horse.
     * @param horseIdx The raceIdx of the horse they bet on.
     * @return A boolean indicating if the Spectator invoking the method won
     * his/her bet.
     */
    @Override
    public boolean haveIWon(int horseIdx) {
        boolean won;
        mutex.lock();

        // checks if winner is the one he/she bet
        won = IntStream.range(0, standings.length).
                filter(i -> standings[i] == 1).anyMatch(w -> w == horseIdx);

        mutex.unlock();

        return won;
    }

    /**
     * Method invoked by the Broker in order to signal the spectators that the
     * event has ended.
     * Meanwhile, Broker also sets its state to PLAYING_HOST_AT_THE_BAR.
     */
    @Override
    public void celebrate() {
        mutex.lock();

        // broker just playing host, end the afternoon
        generalRepository.setBrokerState(BrokerState.PLAYING_HOST_AT_THE_BAR);

        eventEnded = true;
        waitForRace.signalAll();

        mutex.unlock();
    }

    /**
     * Last method invoked by the Spectators, changing their state to CELEBRATING.
     * @param spectatorID ID of the Spectator that will celebrate after the
     *                    event has ended.
     */
    @Override
    public void relaxABit(int spectatorID) {
        mutex.lock();

        /// just relax, end the afternoon
        generalRepository.setSpectatorState(spectatorID,
                SpectatorState.CELEBRATING);

        mutex.unlock();
    }
}

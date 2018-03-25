package sharedRegions;

import entities.Horse;
import entities.Spectator;
import main.EventVariables;
import states.HorseState;
import states.SpectatorState;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The Paddock is a shared region where horses are appraised by the spectators
 * before the race takes place.
 */
public class Paddock {

    /**
     * Instance of a monitor.
     */
    private Lock mutex;

    /**
     * Condition variable where Horses will wait before proceeding to the Racing
     * Track.
     */
    private Condition horses;

    /**
     * Condition variable where Spectators will wait before heading to the
     * Betting Centre and placing their bets.
     */
    private Condition spectators;

    /**
     * Counter of the number of the Horses that have already arrived to the
     * Paddock.
     */
    private int horsesInPaddock;

    /**
     * Counter of the number of the Spectators that have already arrived to the
     * Paddock.
     */
    private int spectatorsInPaddock;

    /**
     * Flag that signals if Spectators can proceed to the Betting Centre.
     */
    private boolean spectatorsCanProceed;

    /**
     * Instance of the shared region General Repository.
     */
    private GeneralRepository generalRepository;

    /**
     * Instance of the shared region Control Centre.
     */
    private ControlCentre controlCentre;

    public Paddock(GeneralRepository generalRepository,
                   ControlCentre controlCentre) {
        if (generalRepository == null || controlCentre == null)
            throw new IllegalArgumentException("Invalid shared region reference.");

        this.generalRepository = generalRepository;
        this.controlCentre = controlCentre;
        this.mutex = new ReentrantLock();
        this.horses = this.mutex.newCondition();
        this.spectators = this.mutex.newCondition();
        this.horsesInPaddock = 0;
        this.spectatorsInPaddock = 0;
        this.spectatorsCanProceed = false;
    }

    /**
     * Method invoked by the last Horse leaving to the Racing Track.
     * It will notify all Spectators to proceed to the Betting Centre.
     */
    private void proceedToBettingCentre() {
        // Restart the variables
        this.horsesInPaddock = 0;
        this.spectatorsInPaddock = 0;

        // notify all spectators
        spectatorsCanProceed = true;
        spectators.signalAll();
    }

    /**
     * Method invoked by each one of the Horses. They will change their state
     * to AT_THE_PADDOCK and wait until all Spectators arrive to the Paddock.
     */
    public void proceedToPaddock() {
        Horse h;
        mutex.lock();

        // Reset the variable
        spectatorsCanProceed = false;

        h = (Horse)Thread.currentThread();
        h.setHorseState(HorseState.AT_THE_PADDOCK);
        generalRepository.setHorseState(h.getRaceID(), h.getRaceIdx(),
                HorseState.AT_THE_PADDOCK);

        // last horse notify spectators
        if (++horsesInPaddock == EventVariables.NUMBER_OF_HORSES_PER_RACE)
            controlCentre.proceedToPaddock();

        // horse wait in paddock
        while (spectatorsInPaddock < EventVariables.NUMBER_OF_SPECTATORS) {
            try {
                horses.await();
            } catch (InterruptedException ignored) { }
        }

        // last horse notify all spectators
        if (--horsesInPaddock == 0)
            proceedToBettingCentre();

        mutex.unlock();
    }

    /**
     * Method invoked by each one of the Spectators where they will update their
     * state to APPRAISING_THE_HORSES and will block waiting
     */
    public void goCheckHorses() {
        Spectator s;
        mutex.lock();

        s = (Spectator)Thread.currentThread();
        s.setSpectatorState(SpectatorState.APPRAISING_THE_HORSES);
        generalRepository.setSpectatorState(s.getID(),
                SpectatorState.APPRAISING_THE_HORSES);

        // last spectator notify all horses
        if (++spectatorsInPaddock == EventVariables.NUMBER_OF_SPECTATORS) {
            controlCentre.goCheckHorses();
            horses.signalAll();
        }

        // spectator wait in paddock
        while (!spectatorsCanProceed) {
            try {
                spectators.await();
            } catch (InterruptedException ignored) { }
        }
        mutex.unlock();
    }
}

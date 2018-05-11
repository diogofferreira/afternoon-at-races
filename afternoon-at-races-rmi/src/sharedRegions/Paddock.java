package sharedRegions;

import interfaces.ControlCentreInt;
import interfaces.GeneralRepositoryInt;
import interfaces.PaddockInt;
import main.EventVariables;
import states.HorseState;
import states.SpectatorState;

import java.rmi.RemoteException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The Paddock is a shared region where horses are appraised by the spectators
 * before the race takes place.
 */
public class Paddock implements PaddockInt {

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
    private GeneralRepositoryInt generalRepository;

    /**
     * Instance of the shared region Control Centre.
     */
    private ControlCentreInt controlCentre;

    public Paddock(GeneralRepositoryInt generalRepository,
                   ControlCentreInt controlCentre) {
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
     * @param raceId The race ID in which the horse will run.
     * @param raceIdx The horse's index/position on the race.
     */
    @Override
    public void proceedToPaddock(int raceId, int raceIdx) {
        mutex.lock();

        // Reset the variable
        spectatorsCanProceed = false;

        try {
            generalRepository.setHorseState(raceId, raceIdx,
                    HorseState.AT_THE_PADDOCK);
        } catch (RemoteException e) {
            System.out.println("GeneralRepository remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // last horse notify spectators
        if (++horsesInPaddock == EventVariables.NUMBER_OF_HORSES_PER_RACE) {
            try {
                controlCentre.proceedToPaddock();
            } catch (RemoteException e) {
                System.out.println("ControlCentre remote invocation exception: "
                        + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }

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
     * @param spectatorId ID of the Spectator.
     */
    @Override
    public void goCheckHorses(int spectatorId) {
        mutex.lock();

        try {
            generalRepository.setSpectatorState(spectatorId,
                    SpectatorState.APPRAISING_THE_HORSES);
        } catch (RemoteException e) {
            System.out.println("GeneralRepository remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // last spectator notify all horses
        if (++spectatorsInPaddock == EventVariables.NUMBER_OF_SPECTATORS) {
            try {
                controlCentre.goCheckHorses();
            } catch (RemoteException e) {
                System.out.println("ControlCentre remote invocation exception: "
                        + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
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

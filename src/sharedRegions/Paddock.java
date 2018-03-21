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
 *    General description:
 *       definition of shared region Paddock built in explicitly as a monitor using reference types from the
 *         Java concurrency library.
 */

public class Paddock {

    private GeneralRepository generalRepository;
    private ControlCentre controlCentre;

    private Lock mutex;
    private Condition horses, spectators;

    private int horsesInPaddock;
    private int spectatorsInPaddock;

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
    }

    public void proceedToPaddock() {
        Horse h;
        mutex.lock();

        h = (Horse)Thread.currentThread();
        h.setHorseState(HorseState.AT_THE_PADDOCK);
        generalRepository.setHorseState(h.getRaceIdx(),
                HorseState.AT_THE_PADDOCK);

        // last horse notify spectators
        if (++horsesInPaddock == EventVariables.NUMBER_OF_HORSES_PER_RACE)
            controlCentre.proceedToPaddock();

        // horse wait in paddock
        try {
            horses.await();
        } catch (InterruptedException ignored){}

        mutex.unlock();
    }

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
        try {
            spectators.await();
        } catch (InterruptedException ignored){}

        mutex.unlock();
    }

    public void proceedToStartLine() {
        mutex.lock();

        // Restart the variables
        this.horsesInPaddock = 0;
        this.spectatorsInPaddock = 0;

        // notify all spectators
        spectators.signalAll();

        mutex.unlock();
    }
}

package sharedRegions;

import main.EventVariables;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *    General description:
 *       definition of shared region Paddock built in explicitly as a monitor using reference types from the
 *         Java concurrency library.
 */

public class Paddock {

    private Stable stable;
    private ControlCentre controlCentre;

    private Lock mutex;
    private Condition horses, spectators;

    private int horsesInPaddock;
    private int spectatorsInPaddock;

    public Paddock(Stable s, ControlCentre c) {
        if (s == null || c == null)
            throw new IllegalArgumentException("Invalid shared region reference.");

        this.stable = s;
        this.controlCentre = c;
        this.mutex = new ReentrantLock();
        this.horses = this.mutex.newCondition();
        this.spectators = this.mutex.newCondition();
        this.horsesInPaddock = 0;
        this.spectatorsInPaddock = 0;
    }

    public void proceedToPaddock() {
        mutex.lock();
        // last horse notify spectators
        if (++horsesInPaddock == EventVariables.NUMBER_OF_HORSES_PER_RACE)
            controlCentre.proceedToPaddock();

        // horse wait in paddock
        try {
            horses.await();
        } catch (InterruptedException ignored){}

        mutex.unlock();
    }

    public List<Integer> goCheckHorses() {
        mutex.lock();

        // last spectator notify all horses */
        if (++spectatorsInPaddock == EventVariables.NUMBER_OF_SPECTATORS)
            controlCentre.goCheckHorses();

        // spectator wait in paddock
        try {
            spectators.await();
        } catch (InterruptedException ignored){}

        mutex.unlock();

        return stable.getCurrentLineup(controlCentre.getRaceNumber());
    }

    public void proceedToStartLine() {
        mutex.lock();

        this.horsesInPaddock = 0;
        this.spectatorsInPaddock = 0;

        // notify all spectators
        spectators.signalAll();

        mutex.unlock();
    }
}

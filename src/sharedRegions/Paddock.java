package sharedRegions;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *    General description:
 *       definition of shared region Paddock built in explicitly as a monitor using reference types from the
 *         Java concurrency library.
 */

public class Paddock {

    private int horsesInPaddock;
    private int spectatorsInPaddock;
    private Lock mutex;
    private Condition horses, spectators;

    public Paddock() {
        this.horsesInPaddock = 0;
        this.mutex = new ReentrantLock();
        horses = this.mutex.newCondition();
        spectators = this.mutex.newCondition();
    }

    public void proceedToPaddock(int horseId) {

        // last horse notify spectators
        if (++horsesInPaddock == NUMBER_OF_HORSES)
            ControlCentre.proceedToPaddock();

        // horse wait in paddock
        try {
            horses.wait();
        } catch (InterruptedException ignored){}

    }

    public void goCheckHorses(int spectatorId) {
        // last spectator notify all horses */

        if (++spectatorsInPaddock == NUMBER_OF_SPECTATORS)
            ControlCentre.proceedToPaddock();

        // spectator wait in paddock
        try {
            spectators.wait();
        } catch (InterruptedException ignored){}
    }

    public void proceedToStartLine() {
        // notify all spectators
        spectators.notifyAll();
    }
}

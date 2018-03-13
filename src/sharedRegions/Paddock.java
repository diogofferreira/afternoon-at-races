package sharedRegions;

import main.EventVariables;

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

    private ControlCentre controlCentre;

    public Paddock(ControlCentre c) {
        if (c == null)
            throw new IllegalArgumentException("Invalid Control Centre.");

        this.horsesInPaddock = 0;
        this.mutex = new ReentrantLock();
        horses = this.mutex.newCondition();
        spectators = this.mutex.newCondition();
        this.controlCentre = c;
    }

    public void proceedToPaddock(int horseId) {

        // last horse notify spectators
        if (++horsesInPaddock == EventVariables.NUMBER_OF_HORSES_PER_RACE)
            controlCentre.proceedToPaddock();

        // horse wait in paddock
        try {
            horses.wait();
        } catch (InterruptedException ignored){}

    }

    public void goCheckHorses(int spectatorId) {
        // last spectator notify all horses */

        if (++spectatorsInPaddock == EventVariables.NUMBER_OF_SPECTATORS)
            controlCentre.goCheckHorses();

        // spectator wait in paddock
        try {
            spectators.wait();
        } catch (InterruptedException ignored){}
    }

    public void proceedToStartLine() {
        this.horsesInPaddock = 0;
        this.spectatorsInPaddock = 0;
        // notify all spectators
        spectators.notifyAll();
    }
}

package sharedRegions;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *    General description:
 *       definition of shared region Stable built in explicitly as a monitor using reference types from the
 *         Java concurrency library.
 */

public class Stable {

    private Lock mutex;
    private Condition inStable;

    public Stable() {
        this.mutex = new ReentrantLock();
        this.inStable = this.mutex.newCondition();
    }

    public void summonHorsesToPaddock(int raceNumber) {
        // notify all horses
        inStable.notifyAll();
    }

    public void proceedToStable(int horseId) {
        // horse wait in stable
        try {
            inStable.wait();
        } catch (InterruptedException ignored){}
    }
}

package sharedRegions;

import entities.Horse;
import main.EventVariables;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
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
    private Horse[][] raceLineups =
            new Horse[EventVariables.getNumRaces()][EventVariables.getNumHorsesPerRace()];

    private void generateLineup(Horse[] horses) {
        // Shuffle array
        Random rnd = ThreadLocalRandom.current();
        for (int i = horses.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            Horse h = horses[index];
            horses[index] = horses[i];
            horses[i] = h;
        }

        for (int i = 0; i < horses.length; i++)
            this.raceLineups[i / EventVariables.getNumRaces()]
                    [i % EventVariables.getNumRaces()] = horses[i];
    }

    public Stable(Horse[] horses) {
        if (horses == null || horses.length != EventVariables.getNumHorses())
            throw new IllegalArgumentException("Null or invalid horses array");

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

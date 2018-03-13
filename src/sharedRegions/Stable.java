package sharedRegions;

import entities.Horse;
import main.EventVariables;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
    private List<List<Integer>> raceLineups;

    private ControlCentre controlCentre;

    private void generateLineup(int[] horses) {
        // Shuffle array
        Random rnd = ThreadLocalRandom.current();
        for (int i = horses.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int h = horses[index];
            horses[index] = horses[i];
            horses[i] = h;
        }

        for (int i = 0; i < horses.length; i++)
            this.raceLineups.get(i / EventVariables.NUMBER_OF_RACES).add(horses[i]);
    }

    public Stable(int[] horsesID, ControlCentre c) {
        if (horsesID == null || horsesID.length != EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Null or invalid horses array");
        if (c == null)
            throw new IllegalArgumentException("Invalid Control Centre.");

        this.mutex = new ReentrantLock();
        this.inStable = this.mutex.newCondition();
        this.raceLineups = new ArrayList<List<Integer>>(EventVariables.NUMBER_OF_RACES);
        for (int i = 0; i < EventVariables.NUMBER_OF_RACES; i++)
            this.raceLineups.add(new ArrayList<Integer>(
                    EventVariables.NUMBER_OF_HORSES_PER_RACE));
        this.controlCentre = c;
    }

    public void summonHorsesToPaddock(int raceNumber) {
        // notify all horses
        inStable.notifyAll();
    }

    public void proceedToStable(int horseID) {
        // horse wait in stable
        while (raceLineups.get(controlCentre.getRaceNumber()).contains(horseID)) {
            try {
                inStable.wait();
            } catch (InterruptedException ignored) {
            }
        }
    }
}

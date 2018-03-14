package sharedRegions;

import main.EventVariables;

import java.util.*;
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
    private Map<Integer, Integer> horsesAgility;

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
        this.raceLineups = new ArrayList<>(EventVariables.NUMBER_OF_RACES);
        for (int i = 0; i < EventVariables.NUMBER_OF_RACES; i++)
            this.raceLineups.add(new ArrayList<>(
                    EventVariables.NUMBER_OF_HORSES_PER_RACE));
        this.horsesAgility = new HashMap<>();
        this.controlCentre = c;
    }

    public List<List<Integer>> getRaceLineups() {
        return raceLineups;
    }

    public Map<Integer, Integer> getHorsesAgility() {
        return horsesAgility;
    }

    public void summonHorsesToPaddock(int raceNumber) {
        mutex.lock();
        
        // notify all horses
        inStable.signalAll();
        
        mutex.unlock();
    }

    public void proceedToStable(int horseID, int horseAgility) {
        mutex.lock();
        
        // horse wait in stable
        horsesAgility.put(horseID, horseAgility);
        while (raceLineups.get(controlCentre.getRaceNumber()).contains(horseID)) {
            try {
                inStable.await();
            } catch (InterruptedException ignored) {}
        }
        
        mutex.unlock();
    }

    public List<Integer> getCurrentLineup(int raceNumber) {
        return raceLineups.get(raceNumber-1);
    }
}

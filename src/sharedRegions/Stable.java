package sharedRegions;

import entities.Broker;
import entities.Horse;
import main.EventVariables;
import states.BrokerState;
import states.HorseState;

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
    private Condition allHorsesInStable;
    private Condition[] inStable;

    private int[][] horsesAgility;
    private GeneralRepository generalRepository;
    private int horsesInStable;

    public static int[][] generateLineup(int[] horses) {
        int[][] raceLineups = new int[EventVariables.NUMBER_OF_RACES]
                [EventVariables.NUMBER_OF_HORSES_PER_RACE];

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
            raceLineups[i / EventVariables.NUMBER_OF_HORSES_PER_RACE]
                    [i % EventVariables.NUMBER_OF_HORSES_PER_RACE] = horses[i];

        return raceLineups;
    }

    public Stable(GeneralRepository generalRepository) {
        if (generalRepository == null)
            throw new IllegalArgumentException("Invalid General Repository.");

        this.generalRepository = generalRepository;
        this.mutex = new ReentrantLock();
        this.inStable = new Condition[EventVariables.NUMBER_OF_RACES];
        this.horsesInStable = 0;

        this.allHorsesInStable = this.mutex.newCondition();

        for (int i = 0; i < EventVariables.NUMBER_OF_RACES; i++)
            this.inStable[i] = this.mutex.newCondition();

        this.horsesAgility = new int[EventVariables.NUMBER_OF_RACES]
                [EventVariables.NUMBER_OF_HORSES_PER_RACE];
    }

    public int[][] getHorsesAgility() {
        int[][] toRtn;

        mutex.lock();

        toRtn = this.horsesAgility;

        mutex.unlock();
        return toRtn;
    }

    public void summonHorsesToPaddock(int raceID) {
        Broker b;

        mutex.lock();

        b = (Broker)(Thread.currentThread());
        b.setBrokerState(BrokerState.ANNOUNCING_NEXT_RACE);
        generalRepository.setBrokerState(BrokerState.ANNOUNCING_NEXT_RACE);

        System.out.println("CHAMANDO OS CAVALOS TODOS");
        // notify all horses
        inStable[raceID].signalAll();

        mutex.unlock();
    }

    public void proceedToStable() {
        Horse h;

        mutex.lock();

        h = (Horse)(Thread.currentThread());
        h.setHorseState(HorseState.AT_THE_STABLE);
        generalRepository.setHorseState(h.getRaceIdx(),
                HorseState.AT_THE_STABLE);

        // set horse agility
        if (horsesAgility[h.getRaceID()][h.getRaceIdx()] == 0) {
            horsesAgility[h.getRaceID()][h.getRaceIdx()] = h.getAgility();
            generalRepository.setHorseAgility(h.getRaceIdx(), h.getAgility());
        }

        // notify broker for an horse arrival
        horsesInStable++;
        allHorsesInStable.signal();

        // horse wait in stable
        try {
            System.out.println("RACE " + h.getRaceID() + " CAVALO " + h.getID() + " VAI DORMIR");
            inStable[h.getRaceID()].await();
        } catch (InterruptedException ignored) {}
        System.out.println("INDO EMBORA CAVALO " + h.getID());

        // horse departure
        if (--horsesInStable == 0)
            allHorsesInStable.signal();

        mutex.unlock();
    }

    public void entertainTheGuests() {
        mutex.lock();

        // wait for all horses arrive to stable
        while (horsesInStable < EventVariables.NUMBER_OF_HORSES &&
                horsesInStable > 0) {
            try {
                allHorsesInStable.await();
            } catch (InterruptedException ignored) {}
        }

        // notify all horses
        for (Condition horsesInRace : inStable)
            horsesInRace.signalAll();

        mutex.unlock();
    }
}

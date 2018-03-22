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
    private Condition[] inStable;

    private int[] lineups;
    private int[][] horsesAgility;
    private GeneralRepository generalRepository;
    private boolean canCelebrate;
    private boolean[] canProceed;

    public Stable(GeneralRepository generalRepository, int[] horsesIdx) {
        if (generalRepository == null)
            throw new IllegalArgumentException("Invalid General Repository.");
        if (horsesIdx.length != EventVariables.NUMBER_OF_HORSES)
            throw new IllegalArgumentException("Invalid array of horses' indexes");

        this.generalRepository = generalRepository;
        this.mutex = new ReentrantLock();
        this.inStable = new Condition[EventVariables.NUMBER_OF_RACES];
        this.canCelebrate = false;
        this.canProceed = new boolean[EventVariables.NUMBER_OF_RACES];

        this.lineups = new int[EventVariables.NUMBER_OF_HORSES];

        generateLineup(horsesIdx);

        for (int i = 0; i < EventVariables.NUMBER_OF_RACES; i++)
            this.inStable[i] = this.mutex.newCondition();

        this.horsesAgility = new int[EventVariables.NUMBER_OF_RACES]
                [EventVariables.NUMBER_OF_HORSES_PER_RACE];
    }

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
            lineups[horses[i]] = i;

    }

    public int[][] getHorsesAgility() {
        int[][] toRtn;

        mutex.lock();

        toRtn = this.horsesAgility;

        mutex.unlock();
        return toRtn;
    }

    public void summonHorsesToPaddock(int raceID) {
        mutex.lock();

        // notify all horses
        canProceed[raceID] = true;
        inStable[raceID].signalAll();

        mutex.unlock();
    }

    public void proceedToStable() {
        Horse h;

        mutex.lock();

        h = (Horse) (Thread.currentThread());

        h.setRaceID(lineups[h.getID()] / EventVariables.NUMBER_OF_HORSES_PER_RACE);
        h.setRaceIdx(lineups[h.getID()] % EventVariables.NUMBER_OF_HORSES_PER_RACE);

        h.setHorseState(HorseState.AT_THE_STABLE);
        generalRepository.setHorseState(h.getRaceIdx(),
                HorseState.AT_THE_STABLE);

        // set horse agility in general repository
        if (horsesAgility[h.getRaceID()][h.getRaceIdx()] == 0) {
            horsesAgility[h.getRaceID()][h.getRaceIdx()] = h.getAgility();
            generalRepository.setHorseAgility(
                    h.getRaceID(), h.getRaceIdx(), h.getAgility());
        }

        // only waits if it's not time to celebrate or if broker has not notified
        // that it can proceed to paddock
        while (!(canCelebrate || canProceed[h.getRaceID()])) {
            // horse waits in stable
            try {
                inStable[h.getRaceID()].await();
            } catch (InterruptedException ignored) { }
        }

        mutex.unlock();
    }

    public void entertainTheGuests() {
        Broker b;

        mutex.lock();

        // broker just playing host, end the afternoon
        b = (Broker)Thread.currentThread();
        b.setBrokerState(BrokerState.PLAYING_HOST_AT_THE_BAR);
        generalRepository.setBrokerState(BrokerState.PLAYING_HOST_AT_THE_BAR);

        // notify all horses-jockeys to go celebrate
        canCelebrate = true;
        for (Condition horsesInRace : inStable)
            horsesInRace.signalAll();

        mutex.unlock();
    }
}


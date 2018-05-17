package sharedRegions;

import interfaces.GeneralRepositoryInt;
import interfaces.StableInt;
import main.EventVariables;
import main.StableMain;
import registries.RegProceedToStable;
import states.HorseState;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The Stable is a shared region where horses rest before and after the races.
 */
public class Stable implements StableInt {

    /**
     * Instance of a monitor.
     */
    private Lock mutex;

    /**
     * Array of condition variables, which will store a condition variable to
     * each of one the races. The horses wait in each one of these variables
     * accordingly to the raceID they're associated to.
     */
    private Condition[] inStable;

    /**
     * Array of flags that signal if the horses associated to certain race (the
     * array index) can proceed to paddock or remain blocked at the correspondent
     * inStable condition variable.
     */
    private boolean[] canProceed;

    /**
     * Variable that signals if all horses can unblock from inStable condition
     * variables to end their lifecycle.
     */
    private boolean canCelebrate;

    /**
     * Array of Horse/Jockey pair IDs that will store the lineups for all races.
     */
    private int[] lineups;

    /**
     * Bidimensional array that stores the agility of each one the horses
     * accordingly to its raceID and raceIdx. It's particularly useful to
     * calculate race betting odds.
     */
    private int[][] horsesAgility;

    /**
     * Array that stores the odds of each one of the Horses participating in the
     * current race, indexed by their raceIdx.
     */
    private double[][] raceOdds;

    /**
     * Instance of the shared region General Repository.
     */
    private GeneralRepositoryInt generalRepository;

    /**
     * Counter to check how many requests were made to the Stable
     * in order to end its life cycle.
     */
    private int requests;

    /**
     * Creates a new instance of Stable.
     * @param generalRepository Reference to an instance of the shared region
     *                          General Repository.
     * @param horsesIds Array of all the participant Horse/Jockey pairs ids to
     *                  generate the lineups.
     */
    public Stable(GeneralRepositoryInt generalRepository, int[] horsesIds) {
        if (generalRepository == null)
            throw new IllegalArgumentException("Invalid General Repository.");
        if (horsesIds.length != EventVariables.NUMBER_OF_HORSES)
            throw new IllegalArgumentException("Invalid array of horses' indexes");

        this.generalRepository = generalRepository;
        this.mutex = new ReentrantLock();
        this.inStable = new Condition[EventVariables.NUMBER_OF_RACES];
        this.canCelebrate = false;
        this.canProceed = new boolean[EventVariables.NUMBER_OF_RACES];
        this.requests = 0;

        this.lineups = new int[EventVariables.NUMBER_OF_HORSES];

        generateLineup(horsesIds);

        for (int i = 0; i < EventVariables.NUMBER_OF_RACES; i++)
            this.inStable[i] = this.mutex.newCondition();

        this.horsesAgility = new int[EventVariables.NUMBER_OF_RACES]
                [EventVariables.NUMBER_OF_HORSES_PER_RACE];

        this.raceOdds = new double[EventVariables.NUMBER_OF_RACES]
                [EventVariables.NUMBER_OF_HORSES_PER_RACE];
    }

    /**
     * Method that generates the races lineups given an array of Horse/Jockey
     * pairs ids. It just shuffles the given array and generates one which indexes
     * correspond to the horses ids and the value stored in that position
     * corresponds to the raceID * NUMBER_HORSES_PER_RACE + raceIdx of each horse.
     * @param horses Array of Horse/Jockey pairs Ids.
     */
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

    /**
     * Method that computes the current race odds.
     * @param raceID The ID of the race which odds will be computed.
     */
    private void computeRaceOdds(int raceID) {
        double oddSum;

        //raceOdds = new double[EventVariables.NUMBER_OF_HORSES_PER_RACE];

        oddSum = (double)Arrays.stream(horsesAgility[raceID]).reduce(
                Integer::sum).getAsInt();

        for (int i = 0; i < horsesAgility[raceID].length; i++) {
            raceOdds[raceID][i] = oddSum / horsesAgility[raceID][i];
        }

        try {
            generalRepository.setHorsesOdd(raceID, raceOdds[raceID]);
        } catch (RemoteException e) {
            System.out.println("GeneralRepository remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Method that returns the odd of all horses running on the race with ID
     * passed as argument..
     * @param raceID The ID of the race of the odds.
     * @return Array with horses' odds.
     */
    @Override
    public double[] getRaceOdds(int raceID) {
        double[] toRtn;

        mutex.lock();

        toRtn = this.raceOdds[raceID];

        mutex.unlock();

        return toRtn;
    }

    /**
     * Method invoked by the Broker to notify all horses of the current race
     * to proceed to Paddock.
     * @param raceID The ID of the current race; it determines which horses will
     *               be waken up.
     */
    @Override
    public void summonHorsesToPaddock(int raceID) {
        mutex.lock();

        // notify all horses
        canProceed[raceID] = true;
        inStable[raceID].signalAll();

        mutex.unlock();
    }

    /**
     * Method invoked by an Horse, where usually it gets blocked.
     * It sets the its state to AT_THE_STABLE.
     * It is waken up by the Broker to proceed to Paddock or when the event ends.
     * @param horseId ID of the Horse/Jockey pair.
     * @param agility Agility of the horse, which in practice corresponds to the
     *               maximum step the horse can make in each iteration.
     */
    @Override
    public RegProceedToStable proceedToStable(int horseId, int agility) {
        RegProceedToStable reg;

        mutex.lock();

        int raceId = lineups[horseId] / EventVariables.NUMBER_OF_HORSES_PER_RACE;
        int raceIdx = lineups[horseId] % EventVariables.NUMBER_OF_HORSES_PER_RACE;

        // set horse agility in general repository
        if (horsesAgility[raceId][raceIdx] == 0) {
            horsesAgility[raceId][raceIdx] = agility;
            try {
                generalRepository.setHorseAgility(raceId, raceIdx, agility);
            } catch (RemoteException e) {
                System.out.println("GeneralRepository remote invocation exception: "
                        + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }

        for (int i = 0; i < horsesAgility[raceId].length; i++) {
            if (horsesAgility[raceId][i] == 0)
                break;
            if (i == horsesAgility[raceId].length - 1)
                computeRaceOdds(raceId);
        }

        // update horse state
        try {
            generalRepository.setHorseState(raceId, raceIdx,
                    HorseState.AT_THE_STABLE);
        } catch (RemoteException e) {
            System.out.println("GeneralRepository remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // only waits if it's not time to celebrate or if broker has not notified
        // that it can proceed to paddock
        while (!(canCelebrate || canProceed[raceId])) {
            // horse waits in stable
            try {
                inStable[raceId].await();
            } catch (InterruptedException ignored) { }
        }

        reg = new RegProceedToStable(raceId, raceIdx);

        if (++requests == (EventVariables.NUMBER_OF_HORSES * 2 + 1))
            StableMain.wakeUp();

        mutex.unlock();

        return reg;
    }

    /**
     * Method invoked by the Broker to signal all horses to wake up, ending the
     * event.
     */
    @Override
    public void entertainTheGuests() {
        mutex.lock();

        // notify all horses-jockeys to go celebrate
        canCelebrate = true;
        for (Condition horsesInRace : inStable)
            horsesInRace.signalAll();

        if (++requests == (EventVariables.NUMBER_OF_HORSES * 2 + 1))
            StableMain.wakeUp();

        mutex.unlock();
    }
}


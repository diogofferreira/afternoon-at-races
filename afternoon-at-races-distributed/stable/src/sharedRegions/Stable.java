package sharedRegions;

import communication.HostsInfo;
import entities.HorseInt;
import main.EventVariables;
import serverStates.StableClientsState;
import states.HorseState;
import stubs.GeneralRepositoryStub;

import java.io.*;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The Stable is a shared region where horses rest before and after the races.
 */
public class Stable {

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
    private GeneralRepositoryStub generalRepository;

    private int numExecs;

    /**
     * Creates a new instance of Stable.
     * @param generalRepository Reference to an instance of the shared region
     *                          General Repository.
     * @param horsesIds Array of all the participant Horse/Jockey pairs ids to
     *                  generate the lineups.
     */
    public Stable(GeneralRepositoryStub generalRepository, int[] horsesIds, int numExecs) {
        if (generalRepository == null)
            throw new IllegalArgumentException("Invalid General Repository.");
        if (horsesIds.length != EventVariables.NUMBER_OF_HORSES)
            throw new IllegalArgumentException("Invalid array of horses' indexes");

        this.numExecs = numExecs;

        this.generalRepository = generalRepository;
        this.mutex = new ReentrantLock();
        this.inStable = new Condition[EventVariables.NUMBER_OF_RACES];
        this.canCelebrate = false;
        this.canProceed = new boolean[EventVariables.NUMBER_OF_RACES];

        this.lineups = new int[EventVariables.NUMBER_OF_HORSES];

        generateLineup(horsesIds);

        for (int i = 0; i < EventVariables.NUMBER_OF_RACES; i++)
            this.inStable[i] = this.mutex.newCondition();

        this.horsesAgility = new int[EventVariables.NUMBER_OF_RACES]
                [EventVariables.NUMBER_OF_HORSES_PER_RACE];

        this.raceOdds = new double[EventVariables.NUMBER_OF_RACES]
                [EventVariables.NUMBER_OF_HORSES_PER_RACE];


        /* Check if status file exists, if so, load previous state */
        File statusFile = new File(HostsInfo.STABLE_STATUS_PATH.replace("{}-", ""));
        BufferedReader br = null;

        if (statusFile.isFile()) {
            try {
                br = new BufferedReader(new FileReader(
                        new File(HostsInfo.STABLE_STATUS_PATH.replace(
                                "{}-", ""))));
            } catch (FileNotFoundException e) {
                System.err.format("%s: no such" + " file or directory%n",
                        HostsInfo.STABLE_STATUS_PATH.replace(
                                "{}-", ""));
                System.exit(1);
            }

            try {
                String[] w, k;
                String[] args = br.readLine().trim().split("\\|");

                w = args[0].trim().substring(1, args[0].trim().length()-1).split(",");
                for (int i = 0; i < w.length; i++)
                    this.canProceed[i] = Boolean.parseBoolean(w[i].trim());

                this.canCelebrate = Boolean.parseBoolean(args[1].trim());

                w = args[2].trim().substring(1, args[2].trim().length()-1).split(",");
                for (int i = 0; i < w.length; i++)
                    this.lineups[i] = Integer.parseInt(w[i].trim());

                w = args[3].trim().substring(1, args[3].trim().length()-1).split("],");
                for (int i = 0; i < w.length; i++) {
                    k = w[i].trim().substring(1).split(",");
                    for (int j = 0; j < k.length; j++)
                        this.horsesAgility[i][j] = Integer.parseInt(k[j].trim());
                }

                w = args[4].trim().substring(1, args[4].trim().length()-1).split("],");
                for (int i = 0; i < w.length; i++) {
                    k = w[i].trim().substring(1).split(",");
                    for (int j = 0; j < k.length; j++)
                        this.raceOdds[i][j] = Double.parseDouble(k[j].trim());
                }

            } catch (Exception e) {
                System.err.println(e);
                System.err.println("Invalid Stable status file");
                e.printStackTrace();
                System.exit(1);
            }
        } else
            updateStatusFile();
    }

    /**
     * Updates the file which stores all the entities last state on the current server.
     */
    private void updateStatusFile() {
        PrintWriter pw;

        try {
            pw = new PrintWriter(new FileWriter(
                    HostsInfo.STABLE_STATUS_PATH.replace(
                            "{}-", ""), false));
            pw.println(toString());
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the previously created status file.
     */
    public void deleteStatusFiles() {
        try {
            Files.delete(Paths.get(HostsInfo.STABLE_STATUS_PATH.replace(
                    "{}-", "")));
        } catch (NoSuchFileException x) {
            System.err.format("%s: no such" + " file or directory%n",
                    HostsInfo.STABLE_STATUS_PATH);
            //System.exit(1);
        } catch (DirectoryNotEmptyException x) {
            System.err.format("%s not empty%n",
                    HostsInfo.STABLE_STATUS_PATH);
            System.exit(1);
        } catch (IOException x) {
            // File permission problems are caught here.
            System.err.println(x);
            System.exit(1);
        }
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

        generalRepository.setHorsesOdd(raceID, raceOdds[raceID]);
    }

    /**
     * Method that returns the agility of all horses, indexed by the raceID and
     * raceIdx of each one of them.
     * @return Bidimensional array of horses' agilities.
     */
    public int[][] getHorsesAgility() {
        int[][] toRtn;

        mutex.lock();

        toRtn = this.horsesAgility;

        mutex.unlock();
        return toRtn;
    }

    /**
     * Method that returns the odd of all horses running on the race with ID
     * passed as argument..
     * @param raceID The ID of the race of the odds.
     * @return Array with horses' odds.
     */
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
    public void summonHorsesToPaddock(int raceID) {
        mutex.lock();

        // notify all horses
        canProceed[raceID] = true;
        inStable[raceID].signalAll();
        updateStatusFile();

        mutex.unlock();
    }

    /**
     * Method invoked by an Horse, where usually it gets blocked.
     * It sets the its state to AT_THE_STABLE.
     * It is waken up by the Broker to proceed to Paddock or when the event ends.
     */
    public void proceedToStable() {
        HorseInt h;

        mutex.lock();

        h = (HorseInt) (Thread.currentThread());

        h.setRaceID(lineups[h.getID()] / EventVariables.NUMBER_OF_HORSES_PER_RACE);
        h.setRaceIdx(lineups[h.getID()] % EventVariables.NUMBER_OF_HORSES_PER_RACE);

        // set horse agility in general repository
        if (horsesAgility[h.getRaceID()][h.getRaceIdx()] == 0) {
            horsesAgility[h.getRaceID()][h.getRaceIdx()] = h.getAgility();
            generalRepository.setHorseAgility(
                    h.getRaceID(), h.getRaceIdx(), h.getAgility());
        }

        for (int i = 0; i < horsesAgility[h.getRaceID()].length; i++) {
            if (horsesAgility[h.getRaceID()][i] == 0)
                break;
            if (i == horsesAgility[h.getRaceID()].length - 1)
                computeRaceOdds(h.getRaceID());
        }

        h.setHorseState(HorseState.AT_THE_STABLE);
        generalRepository.setHorseState(h.getRaceID(), h.getRaceIdx(),
                HorseState.AT_THE_STABLE);

        updateStatusFile();

        if (numExecs == 1) {
            //System.out.println("EXIT 2 ON PROCEED TO STABLE");
            Runtime.getRuntime().halt(1);
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

    /**
     * Method invoked by the Broker to signal all horses to wake up, ending the
     * event.
     */
    public void entertainTheGuests() {
        mutex.lock();

        // notify all horses-jockeys to go celebrate
        canCelebrate = true;
        for (Condition horsesInRace : inStable)
            horsesInRace.signalAll();

        updateStatusFile();
        mutex.unlock();
    }

    /**
     * Returns a string representation of the Stable state.
     * @return Textual representation of the Stable state.
     */
    @Override
    public String toString() {
        String ha = "[";
        for (int[] a : horsesAgility)
            ha = ha + Arrays.toString(a) + ",";
        ha = ha + "]";

        String ro = "[";
        for (double[] r : raceOdds)
            ro = ro + Arrays.toString(r) + ",";
        ro = ro + "]";

        return Arrays.toString(canProceed) + "|" + canCelebrate + "|" +
                Arrays.toString(lineups) + "|" + ha + "|" + ro;
    }
}


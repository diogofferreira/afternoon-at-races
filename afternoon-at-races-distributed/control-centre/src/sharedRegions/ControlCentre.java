package sharedRegions;

import communication.HostsInfo;
import entities.BrokerInt;
import entities.SpectatorInt;
import main.EventVariables;
import states.BrokerState;
import states.SpectatorState;
import stubs.GeneralRepositoryStub;
import stubs.StableStub;

import java.io.*;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/**
 * The Control Centre is a shared region where the Broker will supervise the race
 * and where the Spectators will watch the races.
 */
public class ControlCentre {

    /**
     * Instance of a monitor.
     */
    private Lock mutex;

    /**
     * Conditional variable where the Broker will wait while the Spectators are
     * appraising the Horses at the Paddock.
     */
    private Condition horsesInPaddock;

    /**
     * Conditional variable where the Spectators will wait for the next race
     * announced by the Broker.
     */
    private Condition waitForRace;

    /**
     * Conditional variable where the Broker will wait while supervising the race.
     */
    private Condition startingRace;

    /**
     * Conditional variable where the Spectators will wait while watching a race.
     */
    private Condition watchingRace;

    /**
     * Flag that signals if the Spectators are still at the Paddock.
     */
    private boolean spectatorsInPaddock;

    /**
     * Flag that signals if the Spectators can proceed to the Paddock.
     */
    private boolean spectatorsCanProceed;

    /**
     * Flag that signals the Broker if that race has already finished.
     */
    private boolean raceFinished;

    /**
     * Flag that signals the Spectators waiting for the results of the race
     * to be announced.
     */
    private boolean reportsPosted;

    /**
     * Array list that increments each time a spectator is waken up by the
     * announcing of the race results.
     */
    private List<Integer> spectatorsLeavingRace;

    /**
     * Flag that signals if the event has already ended;
     */
    private boolean eventEnded;

    /**
     * Array that contains the standings of the Horses in the race.
     */
    private int[] standings;

    /**
     * Instance of the communication stub for the shared region General Repository.
     */
    private GeneralRepositoryStub generalRepository;

    /**
     * Instance of the communication stub for the shared region Stable.
     */
    private StableStub stable;

    private int numExecs;

    /**
     * Creates a new instance of Control Centre.
     * @param generalRepository Reference to an instance of a communication stub
     *                         for the shared region General Repository.
     * @param stable Reference to an instance of the communication stub
     *               for the shared region Stable.
     */
    public ControlCentre(GeneralRepositoryStub generalRepository, StableStub stable, int numExecs) {
        if (generalRepository == null)
            throw new IllegalArgumentException("Invalid General Repository Stub.");
        if (stable == null)
            throw new IllegalArgumentException("Invalid Stable Stub.");

        this.numExecs = numExecs;

        this.generalRepository = generalRepository;
        this.stable = stable;
        this.mutex = new ReentrantLock();
        this.horsesInPaddock = this.mutex.newCondition();
        this.waitForRace = this.mutex.newCondition();
        this.watchingRace = this.mutex.newCondition();
        this.startingRace = this.mutex.newCondition();

        this.spectatorsInPaddock = false;
        this.spectatorsCanProceed = false;
        this.raceFinished = false;
        this.reportsPosted = false;
        this.spectatorsLeavingRace = new ArrayList<>();
        this.eventEnded = false;


        /* Check if status file exists, if so, load previous state */
        File statusFile = new File(HostsInfo.CONTROL_CENTRE_STATUS_PATH.replace(
                "{}-", ""));
        BufferedReader br = null;

        if (statusFile.isFile()) {
            try {
                br = new BufferedReader(new FileReader(
                        new File(HostsInfo.CONTROL_CENTRE_STATUS_PATH.replace(
                                "{}-", ""))));
            } catch (FileNotFoundException e) {
                System.err.format("%s: no such" + " file or directory%n",
                        HostsInfo.CONTROL_CENTRE_STATUS_PATH.replace(
                                "{}-", ""));
                System.exit(1);
            }

            try {
                String[] w;
                String[] args = br.readLine().trim().split("\\|");

                this.spectatorsInPaddock = Boolean.parseBoolean(args[0].trim());
                this.spectatorsCanProceed = Boolean.parseBoolean(args[1].trim());
                this.raceFinished = Boolean.parseBoolean(args[2].trim());
                this.reportsPosted = Boolean.parseBoolean(args[3].trim());

                if (!args[4].trim().equals("[]")) {
                    w = args[4].trim().substring(1, args[4].trim().length() - 1).split(",");
                    for (int i = 0; i < w.length; i++)
                        this.spectatorsLeavingRace.add(Integer.parseInt(w[i].trim()));
                }

                this.eventEnded = Boolean.parseBoolean(args[5].trim());

                w = args[6].trim().substring(1, args[6].trim().length()-1).split(",");
                this.standings = new int[w.length];
                for (int i = 0; i < w.length; i++)
                    this.standings[i] = Integer.parseInt(w[i].trim());

            } catch (Exception e) {
                System.err.println(e);
                System.err.println("Invalid Control Centre status file");
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
                    HostsInfo.CONTROL_CENTRE_STATUS_PATH.replace(
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
            Files.delete(Paths.get(HostsInfo.CONTROL_CENTRE_STATUS_PATH.replace(
                    "{}-", "")));
        } catch (NoSuchFileException x) {
            System.err.format("%s: no such" + " file or directory%n",
                    HostsInfo.CONTROL_CENTRE_STATUS_PATH);
            //System.exit(1);
        } catch (DirectoryNotEmptyException x) {
            System.err.format("%s not empty%n",
                    HostsInfo.CONTROL_CENTRE_STATUS_PATH);
            System.exit(1);
        } catch (IOException x) {
            // File permission problems are caught here.
            System.err.println(x);
            System.exit(1);
        }
    }

    /**
     * Method invoked by the Broker in order to start the event. It just simply
     * updates the Broker state and updates the General Repository.
     */
    public void openTheEvent() {
        BrokerInt b;
        mutex.lock();

        b = (BrokerInt)Thread.currentThread();
        b.setBrokerState(BrokerState.OPENING_THE_EVENT);
        generalRepository.setBrokerState(BrokerState.OPENING_THE_EVENT);

        mutex.unlock();
    }

    /**
     * Method invoked by Broker, signaling the start of the event.
     * The Broker updates the current raceID and sets his state to
     * ANNOUNCING_NEXT_RACE, while signalling the Horses to proceed to Paddock.
     * @param raceID The ID of the race that will take place.
     */
    public void summonHorsesToPaddock(int raceID) {
        BrokerInt b;
        mutex.lock();

        // Restart variables
        // Notify general repository to clear all horse related info
        generalRepository.initRace(raceID);

        b = (BrokerInt)Thread.currentThread();
        b.setBrokerState(BrokerState.ANNOUNCING_NEXT_RACE);
        generalRepository.setBrokerState(BrokerState.ANNOUNCING_NEXT_RACE);

        stable.summonHorsesToPaddock(raceID);

        // broker wait
        while (!spectatorsInPaddock) {
            try {
                horsesInPaddock.await();
            } catch (InterruptedException ignored) {}
        }

        spectatorsInPaddock = false;
        updateStatusFile();

        mutex.unlock();
    }

    /**
     * This method is invoked by every Spectator while they're waiting for
     * a race to start.
     * While waiting here, they update their state to WAITING_FOR_A_RACE_TO_START.
     * @return True if there's still a race next.
     */
    public boolean waitForNextRace() {
        SpectatorInt s;
        boolean isThereARace;

        mutex.lock();

        s = (SpectatorInt) Thread.currentThread();
        s.setSpectatorState(SpectatorState.WAITING_FOR_A_RACE_TO_START);
        generalRepository.setSpectatorState(s.getID(),
                SpectatorState.WAITING_FOR_A_RACE_TO_START);

        while (!(spectatorsCanProceed || eventEnded)) {
            // spectators wait
            try {
                waitForRace.await();
            } catch (InterruptedException ignored) {}
        }

        isThereARace = !eventEnded;
        updateStatusFile();

        mutex.unlock();

        return isThereARace;
    }

    /**
     * Method invoked by the last Horse/Jockey pair of the current race to arrive
     * to the Paddock, thus waking up all the Spectators to proceed to Paddock
     * and appraise the horses.
     */
    public void proceedToPaddock() {
        mutex.lock();

        // notify all spectators
        spectatorsCanProceed = true;
        waitForRace.signalAll();

        // update internal variables
        reportsPosted = false;
        spectatorsLeavingRace = new ArrayList<>();

        updateStatusFile();

        mutex.unlock();
    }

    /**
     * Method invoked by the last Horse/Jockey pair arriving to Paddock in order
     * to wake up the Broker.
     */
    public void goCheckHorses() {
        mutex.lock();

        // notify broker
        spectatorsInPaddock = true;
        horsesInPaddock.signal();
        updateStatusFile();

        mutex.unlock();
    }

    /**
     * Method invoked by each Spectator before the start of each race.
     * They will block in WATCHING_A_RACE state until the Broker reports the
     * results of the race.
     */
    public void goWatchTheRace() {
        SpectatorInt s;
        mutex.lock();

        s = (SpectatorInt)Thread.currentThread();
        s.setSpectatorState(SpectatorState.WATCHING_A_RACE);
        generalRepository.setSpectatorState(s.getID(),
                SpectatorState.WATCHING_A_RACE);

        // spectators wait
        while (!reportsPosted) {
            try {
                watchingRace.await();
            } catch (InterruptedException ignored) { }
        }

        if (!spectatorsLeavingRace.contains(s.getID()))
            spectatorsLeavingRace.add(s.getID());

        updateStatusFile();

        if (numExecs == 0) {
            //System.out.println("EXIT 2 ON PROCEED TO STABLE");
            Runtime.getRuntime().halt(1);
        }
        mutex.unlock();
    }

    /**
     * Method invoked by the Broker.
     * He'll wait here until the last Horse/Jockey pair to cross the finish line
     * wakes him up.
     */
    public void startTheRace() {
        mutex.lock();

        // broker wait
        while (!raceFinished) {
            try {
                startingRace.await();
            } catch (InterruptedException ignored) { }
        }

        raceFinished = false;
        updateStatusFile();
        mutex.unlock();
    }

    /**
     * Method invoked by the last Horse/Jockey pair to cross the finish line.
     * The Broker will be notified to wake up and to report the results.
     * @param standings An array of standings of the Horses that in the race.
     */
    public void finishTheRace(int[] standings) {
        mutex.lock();

        this.standings = standings;

        this.spectatorsCanProceed = false;

        // notify broker
        raceFinished = true;
        startingRace.signal();
        updateStatusFile();

        mutex.unlock();
    }

    /**
     * Method invoked by the Broker signalling all Spectators that the results
     * of the race have been reported.
     * @return An array of Horses' raceIdx that won the race.
     */
    public int[] reportResults() {
        int w[];
        mutex.lock();

        generalRepository.setHorsesStanding(standings);

        // set winners list
        w = IntStream.range(0, standings.length).
                filter(i -> standings[i] == 1).toArray();

        // notify all spectators
        reportsPosted = true;
        watchingRace.signalAll();
        updateStatusFile();

        mutex.unlock();

        return w;
    }

    /**
     * Method invoked by each Spectator to verify if they betted on a winning
     * horse.
     * @param horseIdx The raceIdx of the horse they bet on.
     * @return A boolean indicating if the Spectator invoking the method won
     * his/her bet.
     */
    public boolean haveIWon(int horseIdx) {
        boolean won;
        mutex.lock();

        // checks if winner is the one he/she bet
        won = IntStream.range(0, standings.length).
                filter(i -> standings[i] == 1).anyMatch(w -> w == horseIdx);

        mutex.unlock();

        return won;
    }

    /**
     * Method invoked by the Broker in order to signal the spectators that the
     * event has ended.
     * Meanwhile, Broker also sets its state to PLAYING_HOST_AT_THE_BAR.
     */
    public void celebrate() {
        BrokerInt b;
        mutex.lock();

        // broker just playing host, end the afternoon
        b = (BrokerInt)Thread.currentThread();
        b.setBrokerState(BrokerState.PLAYING_HOST_AT_THE_BAR);
        generalRepository.setBrokerState(BrokerState.PLAYING_HOST_AT_THE_BAR);

        eventEnded = true;
        waitForRace.signalAll();
        updateStatusFile();

        mutex.unlock();
    }

    /**
     * Last method invoked by the Spectators, changing their state to CELEBRATING.
     */
    public void relaxABit() {
        SpectatorInt s;
        mutex.lock();

        /// just relax, end the afternoon
        s = (SpectatorInt)Thread.currentThread();
        s.setSpectatorState(SpectatorState.CELEBRATING);
        generalRepository.setSpectatorState(s.getID(),
                SpectatorState.CELEBRATING);

        mutex.unlock();
    }

    /**
     * Returns a string representation of the Control Centre state.
     * @return Textual representation of the Control Centre state.
     */
    @Override
    public String toString() {
        return spectatorsInPaddock + "|" + spectatorsCanProceed + "|" +
                raceFinished + "|" + reportsPosted + "|" +
                spectatorsLeavingRace + "|" + eventEnded + "|" +
                Arrays.toString(standings);
    }
}

package sharedRegions;

import main.EventVariables;
import states.BrokerState;
import states.HorseState;
import states.SpectatorState;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

/**
 * The General Repository is a shared region where all the information about the
 * event at the races is centralized. All the active entities access it to
 * update their state and other variables relevant to the event.
 * All of the changes are reported into a file (log).
 */
public class GeneralRepository {

    /**
     * Instance of a monitor.
     */
    private Lock mutex;

    /**
     * Instance of PrintWriter in order to write the contents of the log.
     */
    private PrintWriter pw;

    /**
     * Name of the file of the log.
     */
    private String filename;

    /**
     * Current Broker state.
     */
    private BrokerState brokerState;

    /**
     * Current Spectators state.
     */
    private SpectatorState[] spectatorsState;

    /**
     * Current state of all Horse/Jockey pairs.
     */
    private HorseState[][] horsesState;

    /**
     * Current ID of the race taking place.
     */
    private int raceNumber;

    /**
     * Current amount of money in the Spectators wallet.
     */
    private int[] spectatorsWallet;

    /**
     * RaceIdx of the Horses the Spectators bet on the current race.
     */
    private int[] spectatorsBettedHorse;

    /**
     * Value of the Spectators' current bets.
     */
    private int[] spectatorsBet;

    /**
     * Bidimensional array with the agilities (maximum step per iteration) of
     * all the horses that will race, indexed by the raceID and raceIdx of each.
     */
    private int[][] horsesAgility;

    /**
     * Horse odds of winning.
     */
    private double[][] horsesOdd;

    /**
     * Current number of steps the Horse has already took in the current race.
     */
    private int[] horsesStep;

    /**
     * Current position of the Horse (travelled distance) in the current race.
     */
    private int[] horsesPosition;

    /**
     * Array that indicates if the Horse/Jockey pair has already crossed the
     * finish line (0 - false / 1 - true).
     */
    private int[] horsesStanding;

    /**
     * Creates a new instance of General Repository.
     * It also creates a new file (log) and prints the its header.
     */
    public GeneralRepository() {
        this.mutex = new ReentrantLock();
        this.brokerState = BrokerState.OPENING_THE_EVENT;
        this.spectatorsState = new SpectatorState[EventVariables.NUMBER_OF_SPECTATORS];
        this.horsesState = new HorseState[EventVariables.NUMBER_OF_RACES]
                [EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.raceNumber = -1;
        this.spectatorsWallet = new int[EventVariables.NUMBER_OF_SPECTATORS];
        this.spectatorsBettedHorse = new int[EventVariables.NUMBER_OF_SPECTATORS];
        this.spectatorsBet = new int[EventVariables.NUMBER_OF_SPECTATORS];
        this.horsesAgility = new int[EventVariables.NUMBER_OF_RACES]
                [EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesOdd = new double[EventVariables.NUMBER_OF_RACES]
                [EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesStep = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesPosition = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesStanding = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];

        // Set up initial values for spectators info
        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++) {
            this.spectatorsWallet[i] = -1;
            this.spectatorsBettedHorse[i] = -1;
            this.spectatorsBet[i] = -1;
        }

        // Set up initial values for horses info
        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES_PER_RACE; i++) {
            this.horsesStep[i] = -1;
            this.horsesPosition[i] = -1;
            this.horsesStanding[i] = -1;
        }

        // Log file settings
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMddhhmmss");
        filename = EventVariables.LOG_FILEPATH + "_" +
                date.format(today) + ".log";

        printHeader();
    }

    /**
     * Method that prints into a file the log header.
     */
    private void printHeader() {
        mutex.lock();

        try {
            pw = new PrintWriter(new FileWriter(filename, true));
        } catch (IOException e) {
            e.printStackTrace();
        }

        pw.println("AFTERNOON AT THE RACE TRACK - Description of the " +
                "internal state of the problem\n");
        pw.printf("MAN/BRK SPECTATOR/BETTER HORSE/JOCKEY PAIR at Race %d\n",
                raceNumber + 1);
        pw.print("  Stat ");
        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++)
            pw.printf(" St%d  Am%d", i, i);
        pw.print(" RN");
        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES_PER_RACE; i++)
            pw.printf(" St%d Len%d", i, i);
        pw.println();
        pw.printf("\t\t\t\t\t\t\t\t\t\t\tRace %d Status\n", raceNumber + 1);
        pw.print(" RN Dist");
        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++)
            pw.printf(" BS%d  BA%d", i, i);
        pw.printf(" ");
        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES_PER_RACE; i++)
            pw.printf(" Od%d N%d Ps%d Sd%s", i, i, i, i);
        pw.println();
        pw.close();
        printState();

        mutex.unlock();
    }

    /**
     * Method invoked each time there's a relevant change in state, printing
     * a new entry in the logger.
     */
    private void printState() {
        mutex.lock();

        if (brokerState == BrokerState.OPENING_THE_EVENT
                && Stream.of(spectatorsState).anyMatch(
                        s -> s != SpectatorState.WAITING_FOR_A_RACE_TO_START)) {
            mutex.unlock();
            return;
        }

        try {
            pw = new PrintWriter(new FileWriter(filename, true));
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Uncomment to check who called the current log line */
        /*
        try {
            Horse h = (Horse) (Thread.currentThread());
            pw.println("HORSE ID: " + h.getRaceID() + ", IDX = " + h.getRaceIdx());
        } catch (ClassCastException e) { }
        try {
            Broker h = (Broker) (Thread.currentThread());
            pw.println("BROKER");
        } catch (ClassCastException e) { }
        try {
            Spectator h = (Spectator) (Thread.currentThread());
            pw.println("SPECTATOR ID: " + h.getID());
        } catch (ClassCastException e) { }
        */

        pw.printf("  %4s ", brokerState != null ? brokerState : "----");

        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++)
            pw.printf(" %3s %4s",
                    spectatorsState[i] != null ? spectatorsState[i] : "---",
                    spectatorsWallet[i] != -1 ? spectatorsWallet[i] : "----"
            );

        pw.printf("  %1d", raceNumber + 1);

        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES_PER_RACE; i++)
            pw.printf(" %3s  %2s ",
                    brokerState != BrokerState.OPENING_THE_EVENT &&
                            horsesState[raceNumber][i] != null ?
                            horsesState[raceNumber][i] : "---",
                    brokerState != BrokerState.OPENING_THE_EVENT &&
                            horsesState[raceNumber][i] != null ?
                            horsesAgility[raceNumber][i] : "--"
            );
        pw.println();

        pw.printf("  %1d  %2d ",
                raceNumber + 1, raceNumber < 0 ?
                        0 : EventVariables.RACING_TRACK_LENGTH);

        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++)
            pw.printf("  %1s  %4s",
                    spectatorsBettedHorse[i] != -1 ? spectatorsBettedHorse[i] : "-",
                    spectatorsBet[i] != -1 ? spectatorsBet[i] : "----"
            );

        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES_PER_RACE; i++)
            pw.printf(" %4s %2s  %2s  %1s",
                    brokerState != BrokerState.OPENING_THE_EVENT &&
                            horsesOdd[raceNumber][i] != 0.0 ?
                            String.format("%4.1f", horsesOdd[raceNumber][i]) : "----",
                    horsesStep[i] != -1 ? horsesStep[i] : "--",
                    horsesPosition[i] != -1 ? horsesPosition[i] : "--",
                    horsesStanding[i] != -1 ? horsesStanding[i] : "-"
            );
        pw.println();
        pw.close();

        mutex.unlock();
    }

    /**
     * Method that updates the Broker state.
     * @param brokerState The new Broker state.
     */
    public void setBrokerState(BrokerState brokerState) {
        mutex.lock();

        this.brokerState = brokerState;
        printState();

        mutex.unlock();
    }

    /**
     * Method that updates the state of a Spectator.
     * @param spectatorId The ID of the Spectator whose the state is updated.
     * @param spectatorState The new state of the referenced Spectator.
     */
    public void setSpectatorState(int spectatorId, SpectatorState spectatorState) {
        mutex.lock();

        if (this.spectatorsState[spectatorId] == null)
            this.spectatorsWallet[spectatorId] = EventVariables.INITIAL_WALLET;
        this.spectatorsState[spectatorId] = spectatorState;
        printState();

        mutex.unlock();
    }

    /**
     * Method that updates the referenced Spectator's wallet by adding the amount
     * the amount passed as argument.
     * @param spectatorId The ID of the Spectator whose wallet will be updated.
     * @param amount The amount of money to add to the wallet.
     */
    public void setSpectatorGains(int spectatorId, int amount) {
        mutex.lock();

        this.spectatorsWallet[spectatorId] += amount;

        mutex.unlock();
    }

    /**
     * Method that sets the reference Horse/Jockey pair state.
     * @param raceID The ID of the race which the pair will run.
     * @param horseIdx The raceIdx of the Horse whose state will be updated.
     * @param horseState The next Horse state.
     */
    public void setHorseState(int raceID, int horseIdx, HorseState horseState) {
        mutex.lock();

        this.horsesState[raceID][horseIdx] = horseState;
        if (horseState == HorseState.AT_THE_STARTING_LINE) {
            this.horsesPosition[horseIdx] = 0;
            this.horsesStep[horseIdx] = 0;
        }
        if (raceID == raceNumber)
            printState();

        mutex.unlock();
    }

    /**
     * Method that sets an Horse agility, i.e., the maximum step per iteration
     * it can takes.
     * @param raceID The raceID where the Horse will participate.
     * @param horseIdx The raceIdx of the reference Horse.
     * @param horseAgility The agility of the referenced Horse.
     */
    public void setHorseAgility(int raceID, int horseIdx, int horseAgility) {
        mutex.lock();

        this.horsesAgility[raceID][horseIdx] = horseAgility;

        mutex.unlock();
    }

    /**
     * Method that sets the bet of the referenced Spectator on the current race.
     * @param spectatorId The ID of the Spectator placing the bet.
     * @param spectatorBet The value of the bet placed.
     * @param spectatorBettedHorse The raceIdx of the Horse the Spectator bet on.
     */
    public void setSpectatorsBet(int spectatorId, int spectatorBet,
                                 int spectatorBettedHorse) {
        mutex.lock();

        this.spectatorsBet[spectatorId] = spectatorBet;
        this.spectatorsBettedHorse[spectatorId] = spectatorBettedHorse;
        this.spectatorsWallet[spectatorId] -= spectatorBet;

        printState();

        mutex.unlock();
    }

    /**
     * Method that sets the odds of the Horses running on the current race.
     * @param horsesOdd Array of horses odds, indexed by their raceIdx.
     */
    public void setHorsesOdd(int raceID, double[] horsesOdd) {
        mutex.lock();

        this.horsesOdd[raceID] = horsesOdd;

        mutex.unlock();
    }

    /**
     * Updates the Horse/Jockey pair current position (i.e., the travelled
     * distance).
     * @param horseIdx The raceIdx of the Horse whose position is being updated.
     * @param horsePosition The new Horse position.
     * @param horseStep The number of steps the Horse has already taken.
     */
    public void setHorsePosition(int horseIdx, int horsePosition, int horseStep) {
        mutex.lock();

        this.horsesPosition[horseIdx] = horsePosition;
        this.horsesStep[horseIdx] = horseStep;
        printState();

        mutex.unlock();
    }

    /**
     * Method that signals the horses' position in the race.
     * @param standings horses' position in the race.
     */
    public void setHorsesStanding(int[] standings) {
        mutex.lock();

        this.horsesStanding = standings;
        printState();

        mutex.unlock();
    }

    /**
     * Method that resets all the race related variables, such as the
     * raceNumber (ID), the spectators bets, the horses odds and
     * travelled distances.
     * @param raceNumber The updated race number.
     */
    public void initRace(int raceNumber) {
        this.raceNumber = raceNumber;
        this.spectatorsBettedHorse = new int[EventVariables.NUMBER_OF_SPECTATORS];
        this.spectatorsBet = new int[EventVariables.NUMBER_OF_SPECTATORS];
        this.horsesStep = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesPosition = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesStanding = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];

        // Set up initial values for spectators info
        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++) {
            this.spectatorsBettedHorse[i] = -1;
            this.spectatorsBet[i] = -1;
        }

        // Set up initial values for horses info
        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES_PER_RACE; i++) {
            this.horsesStep[i] = -1;
            this.horsesPosition[i] = -1;
            this.horsesStanding[i] = -1;
        }
    }
}

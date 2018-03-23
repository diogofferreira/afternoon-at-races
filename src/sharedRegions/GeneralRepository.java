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
     * Current Horse/Jockey pairs state.
     */
    private HorseState[] horsesState;

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
     * Horse odds of winning on the current race.
     */
    private int[] horsesOdd;

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
    private int[] horsesEnded;

    /**
     * Creates a new instance of General Repository.
     * It also creates a new file (log) and prints the its header.
     */
    public GeneralRepository() {
        this.mutex = new ReentrantLock();
        this.brokerState = BrokerState.OPENING_THE_EVENT;
        this.spectatorsState = new SpectatorState[EventVariables.NUMBER_OF_SPECTATORS];
        this.horsesState = new HorseState[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.raceNumber = 0;
        this.spectatorsWallet = new int[EventVariables.NUMBER_OF_SPECTATORS];
        this.spectatorsBettedHorse = new int[EventVariables.NUMBER_OF_SPECTATORS];
        this.spectatorsBet = new int[EventVariables.NUMBER_OF_SPECTATORS];
        this.horsesAgility = new int[EventVariables.NUMBER_OF_RACES]
                [EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesOdd = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesStep = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesPosition = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesEnded = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];

        // Initialize spectators state
        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++)
            spectatorsState[i] = SpectatorState.WAITING_FOR_A_RACE_TO_START;

        // Initialize spectators wallet
        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++)
            spectatorsWallet[i] = EventVariables.INITIAL_WALLET;

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

        pw.println("AFTERNOON AT THE RACE TRACK - Description of the internal state of the problem\n");
        pw.printf("MAN/BRK SPECTATOR/BETTER HORSE/JOCKEY PAIR at Race %d\n", raceNumber + 1);
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
            pw.printf(" Od%d N%d Ps%d SD%s", i, i, i, i);
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

        try {
            pw = new PrintWriter(new FileWriter(filename, true));
        } catch (IOException e) {
            e.printStackTrace();
        }

        pw.printf("  %4s ", brokerState);
        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++)
            pw.printf(" %3s %4d", spectatorsState[i], spectatorsWallet[i]);
        pw.printf("  %1d", raceNumber + 1);
        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES_PER_RACE; i++)
            pw.printf(" %3s  %2d ", horsesState[i] != null ? horsesState[i] : "---",
                    horsesAgility[raceNumber][i]);
        pw.println();
        pw.printf("  %1d  %2d ", raceNumber + 1, EventVariables.RACING_TRACK_LENGTH);
        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++)
            pw.printf("  %1d  %4d", spectatorsBettedHorse[i], spectatorsBet[i]);
        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES_PER_RACE; i++)
            pw.printf(" %4d %2d  %2d  %1s", horsesOdd[i], horsesStep[i],
                    horsesPosition[i], horsesEnded[i]);
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
     * Method that sets the current race number (ID).
     * @param raceNumber The updated race number.
     */
    public void setRaceNumber(int raceNumber) {
        mutex.lock();

        this.raceNumber = raceNumber;

        mutex.unlock();
    }

    /**
     * Method that sets the reference Horse/Jockey pair state.
     * @param horseIdx The raceIdx of the Horse whose state will be updated.
     * @param horseState The next Horse state.
     */
    public void setHorseState(int horseIdx, HorseState horseState) {
        mutex.lock();

        this.horsesState[horseIdx] = horseState;
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
    public void setHorsesOdd(int[] horsesOdd) {
        mutex.lock();

        this.horsesOdd = horsesOdd;

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
     * Method that signals that the referenced Horse has crossed the finish line.
     * @param horseIdx raceIdx of the Horse that crossed the finish line.
     */
    public void setHorseEnded(int horseIdx) {
        mutex.lock();

        this.horsesEnded[horseIdx] = 1;
        printState();

        mutex.unlock();
    }

    /**
     * Method that resets the race related variables.
     */
    public void resetRace() {
        this.spectatorsBettedHorse = new int[EventVariables.NUMBER_OF_SPECTATORS];
        this.spectatorsBet = new int[EventVariables.NUMBER_OF_SPECTATORS];
        this.horsesOdd = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesStep = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesPosition = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesEnded = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];
    }
}

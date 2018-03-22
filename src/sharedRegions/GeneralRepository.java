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

public class GeneralRepository {

    private PrintWriter pw;
    private String filename;

    private Lock mutex;

    private BrokerState brokerState;
    private SpectatorState[] spectatorsState;
    private int[] spectatorsWallet;
    private int raceNumber;
    private HorseState[] horsesState;
    private int[][] horsesAgility;
    private int[] spectatorsBettedHorse;
    private int[] spectatorsBet;
    private int[] horsesOdd;
    private int[] horsesStep;
    private int[] horsesPosition;
    private int[] horsesEnded;

    public GeneralRepository() {
        this.mutex = new ReentrantLock();
        this.brokerState = BrokerState.OPENING_THE_EVENT;
        this.spectatorsState = new SpectatorState[EventVariables.NUMBER_OF_SPECTATORS];
        this.spectatorsWallet = new int[EventVariables.NUMBER_OF_SPECTATORS];
        this.raceNumber = 0;
        this.horsesState = new HorseState[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesAgility = new int[EventVariables.NUMBER_OF_RACES]
                [EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.spectatorsBettedHorse = new int[EventVariables.NUMBER_OF_SPECTATORS];
        this.spectatorsBet = new int[EventVariables.NUMBER_OF_SPECTATORS];
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

    public void setBrokerState(BrokerState brokerState) {
        mutex.lock();

        this.brokerState = brokerState;
        printState();

        mutex.unlock();
    }

    public void setSpectatorState(int spectatorId, SpectatorState spectatorState) {
        mutex.lock();

        this.spectatorsState[spectatorId] = spectatorState;
        printState();

        mutex.unlock();
    }

    public void setSpectatorGains(int spectatorId, int amount) {
        mutex.lock();

        this.spectatorsWallet[spectatorId] += amount;

        mutex.unlock();
    }

    public void setRaceNumber(int raceNumber) {
        mutex.lock();

        this.raceNumber = raceNumber;

        mutex.unlock();
    }

    public void setHorseState(int horseIdx, HorseState horseState) {
        mutex.lock();

        this.horsesState[horseIdx] = horseState;
        printState();

        mutex.unlock();
    }

    public void setHorseAgility(int raceID, int horseIdx, int horseAgility) {
        mutex.lock();

        this.horsesAgility[raceID][horseIdx] = horseAgility;

        mutex.unlock();
    }


    public void setSpectatorsBet(int spectatorId, int spectatorBet,
                                 int spectatorBettedHorse) {
        mutex.lock();

        this.spectatorsBet[spectatorId] = spectatorBet;
        this.spectatorsBettedHorse[spectatorId] = spectatorBettedHorse;
        this.spectatorsWallet[spectatorId] -= spectatorBet;

        printState();

        mutex.unlock();
    }

    public void setHorsesOdd(int [] horsesOdd) {
        mutex.lock();

        this.horsesOdd = horsesOdd;

        mutex.unlock();
    }


    public void setHorsePosition(int horseIdx, int horsePosition, int horseStep) {
        mutex.lock();

        this.horsesPosition[horseIdx] = horsePosition;
        this.horsesStep[horseIdx] = horseStep;

        printState();

        mutex.unlock();
    }

    public void setHorseEnded(int horseIdx) {
        mutex.lock();

        this.horsesEnded[horseIdx] = 1;
        printState();

        mutex.unlock();
    }

    public void resetRace() {
        this.spectatorsBettedHorse = new int[EventVariables.NUMBER_OF_SPECTATORS];
        this.spectatorsBet = new int[EventVariables.NUMBER_OF_SPECTATORS];
        this.horsesOdd = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesStep = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesPosition = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesEnded = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];
    }

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
}

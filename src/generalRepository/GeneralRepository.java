package generalRepository;

import main.EventVariables;
import states.BrokerState;
import states.SpectatorState;
import states.State;

import java.io.PrintWriter;


public class GeneralRepository {

    private static PrintWriter pw;

    private State brokerState;
    private State[] spectatorsState;
    private double[] spectatorsWallet;
    private int raceNumber;
    private State[] horsesState;
    private int[] horsesAgility;
    private int trackLength;
    private int[] spectatorsBettedHorse;
    private double[] spectatorsBet;
    private double[] horsesOdd;
    private int[] horsesStep;
    private int[] horsesPosition;
    private boolean[] horsesEnded;

    public GeneralRepository() {
        this.brokerState = BrokerState.OPENING_THE_EVENT;
        this.spectatorsState = new State[EventVariables.NUMBER_OF_SPECTATORS];
        this.spectatorsWallet = new double[EventVariables.NUMBER_OF_SPECTATORS];
        this.raceNumber = 0;
        this.horsesState = new State[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesAgility = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.trackLength = EventVariables.RACING_TRACK_LENGTH;
        this.spectatorsBettedHorse = new int[EventVariables.NUMBER_OF_SPECTATORS];
        this.spectatorsBet = new double[EventVariables.NUMBER_OF_SPECTATORS];
        this.horsesOdd = new double[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesStep = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesPosition = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.horsesEnded = new boolean[EventVariables.NUMBER_OF_HORSES_PER_RACE];

        // Initialize spectators state
        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++)
            spectatorsState[i] = SpectatorState.WAITING_FOR_A_RACE_TO_START;

        // Initialize spectators wallet
        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++)
            spectatorsWallet[i] = EventVariables.INITIAL_WALLET;
    }

    public void setBrokerState(State brokerState) {
        this.brokerState = brokerState;
    }

    public void setSpectatorState(int spectatorId, State spectatorState) {
        this.spectatorsState[spectatorId] = spectatorState;
    }

    public void setSpectatorWallet(int spectatorId, double spectatorWallet) {
        this.spectatorsWallet[spectatorId] = spectatorWallet;
    }

    public void setRaceNumber(int raceNumber) {
        this.raceNumber = raceNumber;
    }

    public void setHorseState(int horseIdx, State horseState) {
        this.horsesState[horseIdx] = horseState;
    }

    public void setHorseAgility(int horseIdx, int horseAgility) {
        this.horsesAgility[horseIdx] = horseAgility;
    }

    public void setSpectatorsBettedHorse(int spectatorId, int spectatorBettedHorse) {
        this.spectatorsBettedHorse[spectatorId] = spectatorBettedHorse;
    }

    public void setSpectatorsBet(int spectatorId, double spectatorBet) {
        this.spectatorsBet[spectatorId] = spectatorBet;
    }

    public void setHorseOdd(int horseIdx, double horseOdd) {
        this.horsesOdd[horseIdx] = horseOdd;
    }

    public void setHorseStep(int horseIdx, int horseStep) {
        this.horsesStep[horseIdx] = horseStep;
    }

    public void setHorsePosition(int horseIdx, int horsePosition) {
        this.horsesPosition[horseIdx] = horsePosition;
    }

    public void setHorseEnded(int horseIdx, boolean horseEnded) {
        this.horsesEnded[horseIdx] = horseEnded;
    }

    public void initialLog() {
        pw = new PrintWriter();
        pw.printf("AFTERNOON AT THE RACE TRACK - Description of the internal state of the problem\n\n");
        pw.printf("MAN/BRK SPECTATOR/BETTER HORSE/JOCKEY PAIR at Race RN\n");
        pw.printf(" Stat St0 Am0 St1 Am1 St2 Am2 St3 Am3 RN St0 Len0 St1 Len1 St2 Len2 St3 Len3\n");
        pw.printf(" Race RN Status\n");
        pw.printf(" RN Dist BS0 BA0 BS1 BA1 BS2 BA2 BS3 BA3 Od0 N0 Ps0 SD0 Od1 N1 Ps1 Sd1 Od2 N2 Ps2 Sd2 Od3 N3 Ps3 St3\n");

        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++)
            pw.printf("%4s %3s", spectatorsState[i], spectatorsWallet[i]);
    }
}

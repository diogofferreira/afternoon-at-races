package entities;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

import communication.HostsInfo;
import main.EventVariables;
import states.BrokerState;
import stubs.BettingCentreStub;
import stubs.ControlCentreStub;
import stubs.RacingTrackStub;
import stubs.StableStub;

/**
 * The Broker is the entity that controls the event, regulates the bets
 * and supervises the races.
 */
public class Broker extends Thread implements BrokerInt {
    /**
     * Current step in broker's lifecycle.
     */
    private int step;

    /**
     * Current state of the broker lifecycle.
     */
    private BrokerState state;

    /**
     * Current race identifier.
     */
    private int raceNumber;

    /**
     * Current race horse winners.
     */
    private int[] winners;

    /**
     * Instance of the shared region Stable.
     */
    private StableStub stable;

    /**
     * Instance of the shared region Racing Track.
     */
    private RacingTrackStub racingTrack;

    /**
     * Instance of the shared region Control Centre.
     */
    private ControlCentreStub controlCentre;

    /**
     * Instance of the shared region Betting Centre.
     */
    private BettingCentreStub bettingCentre;

    private int numExecs;

    /**
     * Creates a new instance of Broker.
     * @param stable Reference to an instance of the shared region Stable.
     * @param racingTrack Reference to an instance of the shared region
     *                    Racing Track.
     * @param controlCentre Reference to an instance of the shared region
     *                     Control Centre.
     * @param bettingCentre Reference to an instance of the shared region
     *                      Betting Centre.
     */
    public Broker(StableStub stable, RacingTrackStub racingTrack,
                  ControlCentreStub controlCentre, BettingCentreStub bettingCentre, int numExecs) {
        if (stable == null || racingTrack == null ||
                controlCentre == null || bettingCentre == null)
            throw new IllegalArgumentException("Invalid shared region reference.");

        this.step = 0;
        this.state = null;
        this.raceNumber = 0;
        this.winners = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.stable = stable;
        this.controlCentre = controlCentre;
        this.bettingCentre = bettingCentre;
        this.racingTrack = racingTrack;
        this.numExecs = numExecs;

        /* Check if status file exists, if so, load previous state */
        File statusFile = new File(HostsInfo.BROKER_STATUS_PATH);
        BufferedReader br = null;

        if (statusFile.isFile()) {
            try {
                br = new BufferedReader(new FileReader(statusFile));
            } catch (FileNotFoundException e) {
                System.err.format("%s: no such" + " file or directory%n",
                        HostsInfo.BROKER_STATUS_PATH);
                System.exit(1);
            }
            String[] w;
            String[] args;

            try {
                args = br.readLine().trim().split("\\|");
                this.step = Integer.parseInt(args[0].trim());
                this.state = BrokerState.getType(Integer.parseInt(args[1].trim()));
                this.raceNumber = Integer.parseInt(args[2].trim());
                w = args[3].trim().substring(1, args[3].trim().length()-1).split(",");
                this.winners = new int[w.length];
                for (int i = 0; i < w.length; i++)
                    this.winners[i] = Integer.parseInt(w[i].trim());
            } catch (Exception e) {
                System.err.println(e);
                System.err.println("Invalid Broker status file");
                System.exit(1);
            }
        } else
            updateStatusFile(-1);
    }

    /**
     * Updates the step of the entity's lifecycle is in and saves all changes to a file.
     * @param step Entity's lifecycle step.
     */
    private void updateStatusFile(int step) {
        PrintWriter pw;

        this.step = step;
        try {
            pw = new PrintWriter(new FileWriter(HostsInfo.BROKER_STATUS_PATH, false));
            pw.println(toString());
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the previously created status file.
     */
    private void deleteStatusFile() {
        try {
            Files.delete(Paths.get(HostsInfo.BROKER_STATUS_PATH));
        } catch (NoSuchFileException x) {
            System.err.format("%s: no such" + " file or directory%n",
                    HostsInfo.BROKER_STATUS_PATH);
            System.exit(1);
        } catch (DirectoryNotEmptyException x) {
            System.err.format("%s not empty%n", HostsInfo.BROKER_STATUS_PATH);
            System.exit(1);
        } catch (IOException x) {
            // File permission problems are caught here.
            System.err.println(x);
            System.exit(1);
        }
    }

    /**
     * Broker lifecycle.
     */
    public void run() {
        if (this.step == -1) {
            controlCentre.openTheEvent();
            updateStatusFile(0);
        }

        for (; raceNumber < EventVariables.NUMBER_OF_RACES; raceNumber++) {
            // summonHorsesToPaddock
            if (this.step == 0 || this.step == 6) {
                controlCentre.summonHorsesToPaddock(raceNumber);
                updateStatusFile(1);
            }

            // acceptsBets
            if (this.step == 1) {
                bettingCentre.acceptTheBets(raceNumber);
                updateStatusFile(2);
            }

            if (numExecs == 0 && raceNumber == 1) {
                System.out.println("EXIT 2");
                System.exit(1);
            }

            // startTheRace
            if (this.step == 2) {
                racingTrack.startTheRace();
                updateStatusFile(3);
            }
            if (this.step == 3) {
                controlCentre.startTheRace();
                updateStatusFile(4);
            }

            // reportResults
            if (this.step == 4) {
                winners = controlCentre.reportResults();
                updateStatusFile(5);
            }

            // if there are any winners, honour those bets
            if (this.step == 5) {
                if (bettingCentre.areThereAnyWinners(winners))
                    bettingCentre.honourTheBets();
                updateStatusFile(6);
            }
        }

        if (this.step == 6) {
            controlCentre.celebrate();
            updateStatusFile(7);
        }
        if (this.step == 7) {
            stable.entertainTheGuests();
            updateStatusFile(8);
        }
        deleteStatusFile();
    }

    /**
     * Method that returns the current Broker state.
     * @return Current Broker state.
     */
    @Override
    public BrokerState getBrokerState() {
        return this.state;
    }

    /**
     * Updates the current Broker state.
     * @param state The new Broker state.
     */
    @Override
    public void setBrokerState(BrokerState state) {
        this.state = state;
    }

    /**
     * Prints the current state of the Broker.
     */
    @Override
    public String toString() {
        int st = this.state == null ? -1 : this.state.getId();
        return this.step + "|" + st + "|" + this.raceNumber + "|" +
                Arrays.toString(this.winners);
    }
}

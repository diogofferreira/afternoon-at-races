package entities;

import communication.HostsInfo;
import main.EventVariables;
import states.BrokerState;
import states.HorseState;
import stubs.PaddockStub;
import stubs.RacingTrackStub;
import stubs.StableStub;

import java.io.*;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

/**
 * The Horse/Jockey pair is the entity that rests on the stable and is invoked
 * one time max in each event to run a race.
 */
public class Horse extends Thread implements HorseInt {
    /**
     * Current step in Horse/Jockey's lifecycle.
     */
    private int step;

    /**
     * Current state of the Horse/Jockey lifecycle.
     */
    private HorseState state;

    /**
     * ID of the Horse/Jockey pair.
     */
    private int id;

    /**
     * Agility of the horse, which in practice corresponds to the maximum step
     * the horse can make in each iteration.
     */
    private int agility;

    /**
     * The race ID in which the horse will run.
     */
    private int raceID;

    /**
     * The horse's index/position on the race.
     */
    private int raceIdx;

    /**
     * The current position of the horse in the race.
     */
    private int currentPosition;

    /**
     * The current step/iteration of the horse in the race.
     */
    private int currentStep;

    /**
     * Indicates if the Horse has already crossed the finish line.
     */
    private boolean finishLineCrossed;

    /**
     * Instance of the shared region Stable.
     */
    private StableStub stable;

    /**
     * Instance of the shared region Paddock.
     */
    private PaddockStub paddock;

    /**
     * Instance of the shared region Racing Track.
     */
    private RacingTrackStub racingTrack;

    /**
     * Creates a new instance of Horse/Jockey pair.
     * @param id ID of the Horse/Jockey pair.
     * @param agility Agility/max step per iteration of the horse.
     * @param stable Reference to an instance of the shared region Stable.
     * @param paddock Reference to an instance of the shared region Paddock.
     * @param racingTrack Reference to an instance of the shared region
     *                   Racing Track.
     */
    public Horse(int id, int agility, StableStub stable, PaddockStub paddock,
                 RacingTrackStub racingTrack) {
        if (id < 0)
            throw new IllegalArgumentException("Invalid Horse ID.");
        if (agility < 0 || agility > EventVariables.HORSE_MAX_STEP)
            throw new IllegalArgumentException("Invalid Horse agility.");
        if (stable == null || paddock == null || racingTrack == null)
            throw new IllegalArgumentException("Invalid shared region reference.");

        this.step = 0;
        this.state = null;
        this.id = id;
        this.agility = agility;
        this.raceID = -1;
        this.raceIdx = -1;
        this.currentPosition = 0;
        this.currentStep = 0;
        this.finishLineCrossed = false;
        this.stable = stable;
        this.paddock = paddock;
        this.racingTrack = racingTrack;

        /* Check if status file exists, if so, load previous state */
        File statusFile = new File(HostsInfo.HORSES_STATUS_PATH.replace(
                "{}", Integer.toString(this.id)));
        BufferedReader br = null;

        if (statusFile.isFile()) {
            try {
                br = new BufferedReader(new FileReader(statusFile));
            } catch (FileNotFoundException e) {
                System.err.format("%s: no such" + " file or directory%n",
                        HostsInfo.HORSES_STATUS_PATH.replace(
                                "{}", Integer.toString(this.id)));
                System.exit(1);
            }
            String[] args;

            try {
                args = br.readLine().trim().split("\\|");

                this.step = Integer.parseInt(args[0].trim());
                this.state = HorseState.getType(Integer.parseInt(args[1].trim()));
                this.agility = Integer.parseInt(args[2].trim());
                this.raceID = Integer.parseInt(args[3].trim());
                this.raceIdx = Integer.parseInt(args[4].trim());
                this.currentPosition = Integer.parseInt(args[5].trim());
                this.currentStep = Integer.parseInt(args[6].trim());
                this.finishLineCrossed = Boolean.parseBoolean(args[7].trim());
            } catch (Exception e) {
                System.err.println(e);
                System.err.println("Invalid Horse status file");
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
            pw = new PrintWriter(new FileWriter(HostsInfo.HORSES_STATUS_PATH.replace(
                    "{}", Integer.toString(this.id)), false));
            pw.println(this.step);
            pw.println(this.state == null ? -1 : this.state.getId());

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
            Files.delete(Paths.get(HostsInfo.HORSES_STATUS_PATH.replace(
                    "{}", Integer.toString(this.id))));
        } catch (NoSuchFileException x) {
            System.err.format("%s: no such" + " file or directory%n",
                    HostsInfo.HORSES_STATUS_PATH.replace(
                            "{}", Integer.toString(this.id)));
            System.exit(1);
        } catch (DirectoryNotEmptyException x) {
            System.err.format("%s not empty%n", HostsInfo.HORSES_STATUS_PATH.replace(
                    "{}", Integer.toString(this.id)));
            System.exit(1);
        } catch (IOException x) {
            // File permission problems are caught here.
            System.err.println(x);
            System.exit(1);
        }
    }

    /**
     * Generates the next step the horse will make.
     * @return the distance of the next step of the horse.
     */
    private int makeAStep() {
        Random rnd = new Random();
        return rnd.nextInt(agility) + 1;
    }

    /**
     * Horse/Jockey pair lifecycle.
     */
    public void run() {

        // Start at the stable
        if (this.step == -1) {
            stable.proceedToStable();
            updateStatusFile(0);
        }

        // when called, proceed to paddock to be appraised
        if (this.step == 0) {
            paddock.proceedToPaddock();
            updateStatusFile(1);
        }

        // proceed to the starting line
        if (this.step == 1) {
            racingTrack.proceedToStartLine();
            updateStatusFile(2);
        }

        // while not crossed the finish line, keep moving
        while (true) {
            if (this.step == 2 || this.step == 4) {
                finishLineCrossed = racingTrack.hasFinishLineBeenCrossed();
                updateStatusFile(3);
            }

            if (finishLineCrossed)
                break;

            if (this.step == 3) {
                racingTrack.makeAMove(makeAStep());
                updateStatusFile(4);
            }
        }

        // wait at the stable until the broker ends the event
        if (this.step == 3) {
            stable.proceedToStable();
            updateStatusFile(5);
        }

        deleteStatusFile();
    }

    /**
     * Method that returns the current Horse/Jockey pair state.
     * @return Current Horse/Jockey pair state.
     */
    @Override
    public HorseState getHorseState() {
        return state;
    }

    /**
     * Updates the current Horse/Jockey pair state.
     * @param state The new Horse/Jockey pair state.
     */
    @Override
    public void setHorseState(HorseState state) {
        if (state == null)
            throw new IllegalArgumentException("Invalid Horse state");
        this.state = state;
    }

    /**
     * Method that returns the ID of the Horse/Jockey pair.
     * @return The ID of the Horse/Jockey pair.
     */
    @Override
    public int getID() {
        return id;
    }

    /**
     * Method that sets the ID of the Horse/Jockey pair.
     * @param id The new ID of the Horse/Jockey pair.
     */
    @Override
    public void setID(int id) {
        this.id = id;
    }

    /**
     * Method that returns the race ID in which the pair will participate.
     * @return ID of the race in which the pair will participate.
     */
    @Override
    public int getRaceID() {
        return raceID;
    }

    /**
     * Sets the race ID in which the pair will participate.
     * @param raceID The race ID in which the horse will run.
     */
    @Override
    public void setRaceID(int raceID) {
        if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid Race ID");
        this.raceID = raceID;
    }

    /**
     * Method that returns the horse's index/position on the race.
     * @return Horse's index/position on the race.
     */
    @Override
    public int getRaceIdx() {
        return raceIdx;
    }

    /**
     * Sets the horse's index/position on the race.
     * @param raceIdx The horse's index/position on the race.
     */
    @Override
    public void setRaceIdx(int raceIdx) {
        if (raceIdx < 0 || raceIdx >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid Horse race index.");
        this.raceIdx = raceIdx;
    }

    /**
     * Method that returns the agility of each horse, which in practice corresponds
     * to the maximum step distance the horse can take in each iteration.
     * @return Agility/max step per iteration of the horse.
     */
    @Override
    public int getAgility() {
        return agility;
    }

    /**
     * Method that sets the agility of each horse, which in practice corresponds
     * to the maximum step distance the horse can take in each iteration.
     * @param agility The new value of agility/max step per iteration of the horse.
     */
    @Override
    public void setAgility(int agility) {
        this.agility = agility;
    }

    /**
     * Method that returns the horse's current position on the racing track, i.e.,
     * the current travelled distance.
     * @return Horse's current position.
     */
    @Override
    public int getCurrentPosition() {
        return this.currentPosition;
    }

    /**
     * Method that sets the current position of Horse/Jockey pair.
     * @param currentPosition The current position of the Horse/Jockey pair in
     *                        the racing track.
     */
    @Override
    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    /**
     * Method that returns the number of steps the horse has already taken.
     * @return The number of steps/iterations of the horse during the race.
     */
    @Override
    public int getCurrentStep() {
        return this.currentStep;
    }

    /**
     * Method that sets the number of steps the horse has already taken.
     * @param currentStep The number of steps/iterations that the horse has
     *                    already taken.
     */
    @Override
    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    /**
     * Method that updates the current position of the pair, i.e., increases the
     * position with step steps and increments the current step by one.
     * @param step The distance of the increment/step.
     */
    @Override
    public void updateCurrentPosition(int step) {
        this.currentPosition += step;
        this.currentStep++;
    }

    /**
     * Prints the current state of the Horse/Jockey pair.
     */
    @Override
    public String toString() {
        int st = this.state == null ? -1 : this.state.getId();
        return this.step + "|" + st + "|" + this.agility + "|" + this.raceID
                + "|" + this.raceIdx + "|" + this.currentPosition
                + "|" + this.currentStep + "|" + this.finishLineCrossed;
    }
}

package entities;

import interfaces.PaddockInt;
import interfaces.RacingTrackInt;
import interfaces.StableInt;
import main.EventVariables;
import registries.RegProceedToStable;
import sharedRegions.Paddock;
import sharedRegions.RacingTrack;
import sharedRegions.Stable;
import states.HorseState;

import java.rmi.RemoteException;
import java.util.Random;

/**
 * The Horse/Jockey pair is the entity that rests on the stable and is invoked
 * one time max in each event to run a race.
 */
public class Horse extends Thread {
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
     * Instance of the shared region Stable.
     */
    private StableInt stable;

    /**
     * Instance of the shared region Paddock.
     */
    private PaddockInt paddock;

    /**
     * Instance of the shared region Racing Track.
     */
    private RacingTrackInt racingTrack;

    /**
     * Creates a new instance of Horse/Jockey pair.
     * @param id ID of the Horse/Jockey pair.
     * @param agility Agility/max step per iteration of the horse.
     * @param stable Reference to an instance of the shared region Stable.
     * @param paddock Reference to an instance of the shared region Paddock.
     * @param racingTrack Reference to an instance of the shared region
     *                   Racing Track.
     */
    public Horse(int id, int agility, StableInt stable, PaddockInt paddock,
                 RacingTrackInt racingTrack) {
        if (id < 0)
            throw new IllegalArgumentException("Invalid Horse ID.");
        if (agility < 0 || agility > EventVariables.HORSE_MAX_STEP)
            throw new IllegalArgumentException("Invalid Horse agility.");
        if (stable == null || paddock == null || racingTrack == null)
            throw new IllegalArgumentException("Invalid shared region reference.");

        this.state = HorseState.AT_THE_STABLE;
        this.id = id;
        this.agility = agility;
        this.raceID = -1;
        this.raceIdx = -1;
        this.currentPosition = 0;
        this.currentStep = 0;
        this.stable = stable;
        this.paddock = paddock;
        this.racingTrack = racingTrack;
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
        RegProceedToStable pts = null;

        // Start at the stable
        try {
            pts = stable.proceedToStable(id, agility);
        } catch (RemoteException e) {
            System.out.println("Stable remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // update internal atributes and state
        raceID = pts.getRaceId();
        raceIdx = pts.getRaceIdx();
        state = HorseState.AT_THE_STABLE;

        // when called, proceed to paddock to be appraised
        try {
            paddock.proceedToPaddock(raceID, raceIdx);
        } catch (RemoteException e) {
            System.out.println("Paddock remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        state = HorseState.AT_THE_PADDOCK;

        // proceed to the starting line
        try {
            racingTrack.proceedToStartLine(raceID, raceIdx);
        } catch (RemoteException e) {
            System.out.println("RacingTrack remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        state = HorseState.AT_THE_STARTING_LINE;

        // while not crossed the finish line, keep moving
        try {
            while (!racingTrack.hasFinishLineBeenCrossed(raceID, raceIdx, currentStep,
                    currentPosition)) {
                int newStep = makeAStep();
                racingTrack.makeAMove(raceID, raceIdx, currentStep, currentPosition,
                        newStep);
                if (currentStep == 0)
                    state = HorseState.RUNNING;

                // update position
                updateCurrentPosition(newStep);
            }
        } catch (RemoteException e) {
            System.out.println("RacingTrack remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        state = HorseState.AT_THE_FINISH_LINE;

        // wait at the stable until the broker ends the event
        try {
            stable.proceedToStable(id, agility);
        } catch (RemoteException e) {
            System.out.println("Stable remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Method that returns the current Horse/Jockey pair state.
     * @return Current Horse/Jockey pair state.
     */
    public HorseState getHorseState() {
        return state;
    }

    /**
     * Updates the current Horse/Jockey pair state.
     * @param state The new Horse/Jockey pair state.
     */
    public void setHorseState(HorseState state) {
        if (state == null)
            throw new IllegalArgumentException("Invalid Horse state");
        this.state = state;
    }

    /**
     * Method that returns the ID of the Horse/Jockey pair.
     * @return The ID of the Horse/Jockey pair.
     */
    public int getID() {
        return id;
    }

    /**
     * Method that sets the ID of the Horse/Jockey pair.
     * @param id The new ID of the Horse/Jockey pair.
     */
    public void setID(int id) {
        this.id = id;
    }

    /**
     * Method that returns the race ID in which the pair will participate.
     * @return ID of the race in which the pair will participate.
     */
    public int getRaceID() {
        return raceID;
    }

    /**
     * Sets the race ID in which the pair will participate.
     * @param raceID The race ID in which the horse will run.
     */
    public void setRaceID(int raceID) {
        if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid Race ID");
        this.raceID = raceID;
    }

    /**
     * Method that returns the horse's index/position on the race.
     * @return Horse's index/position on the race.
     */
    public int getRaceIdx() {
        return raceIdx;
    }

    /**
     * Sets the horse's index/position on the race.
     * @param raceIdx The horse's index/position on the race.
     */
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
    public int getAgility() {
        return agility;
    }

    /**
     * Method that sets the agility of each horse, which in practice corresponds
     * to the maximum step distance the horse can take in each iteration.
     * @param agility The new value of agility/max step per iteration of the horse.
     */
    public void setAgility(int agility) {
        this.agility = agility;
    }

    /**
     * Method that returns the horse's current position on the racing track, i.e.,
     * the current travelled distance.
     * @return Horse's current position.
     */
    public int getCurrentPosition() {
        return this.currentPosition;
    }

    /**
     * Method that sets the current position of Horse/Jockey pair.
     * @param currentPosition The current position of the Horse/Jockey pair in
     *                        the racing track.
     */
    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    /**
     * Method that returns the number of steps the horse has already taken.
     * @return The number of steps/iterations of the horse during the race.
     */
    public int getCurrentStep() {
        return this.currentStep;
    }

    /**
     * Method that sets the number of steps the horse has already taken.
     * @param currentStep The number of steps/iterations that the horse has
     *                    already taken.
     */
    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    /**
     * Method that updates the current position of the pair, i.e., increases the
     * position with step steps and increments the current step by one.
     * @param step The distance of the increment/step.
     */
    public void updateCurrentPosition(int step) {
        this.currentPosition += step;
        this.currentStep++;
    }
}

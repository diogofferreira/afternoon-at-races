package entities;

import main.EventVariables;

import states.HorseState;
import stubs.PaddockStub;
import stubs.RacingTrackStub;
import stubs.StableStub;

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
        // Start at the stable
        stable.proceedToStable();

        // when called, proceed to paddock to be appraised
        paddock.proceedToPaddock();

        // proceed to the starting line
        racingTrack.proceedToStartLine();

        // while not crossed the finish line, keep moving
        while (!racingTrack.hasFinishLineBeenCrossed())
            racingTrack.makeAMove(makeAStep());

        // wait at the stable until the broker ends the event
        stable.proceedToStable();
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
     * Sets the race ID in which the pair will participate.
     * @param raceID The race ID in which the horse will run.
     */
    public void setRaceID(int raceID) {
        if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid Race ID");
        this.raceID = raceID;
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
     * Method that returns the race ID in which the pair will participate.
     * @return ID of the race in which the pair will participate.
     */
    public int getRaceID() {
        return raceID;
    }

    /**
     * Method that returns the horse's index/position on the race.
     * @return Horse's index/position on the race.
     */
    public int getRaceIdx() {
        return raceIdx;
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
     * Method that returns the number of steps the horse has already taken.
     * @return The number of steps/iterations of the horse during the race.
     */
    public int getCurrentStep() {
        return this.currentStep;
    }

    /**
     * Method that updates the current position of the pair.
     * @param step The distance of the increment/step.
     */
    public void setCurrentPosition(int step) {
        this.currentPosition += step;
        this.currentStep++;
    }
}

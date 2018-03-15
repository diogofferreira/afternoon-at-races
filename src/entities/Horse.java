package entities;

import main.EventVariables;
import sharedRegions.Paddock;
import sharedRegions.RacingTrack;
import sharedRegions.Stable;
import states.HorseState;
import states.State;

import java.util.Random;

public class Horse extends Thread {
    private states.State state;
    private int id;
    private int agility;
    private int raceID;
    private int raceIdx;

    private Stable stable;
    private Paddock paddock;
    private RacingTrack racingTrack;

    public Horse(int id, int agility, int raceID, int raceIdx,
                 Stable s, Paddock p, RacingTrack r) {
        if (id < 0)
            throw new IllegalArgumentException("Invalid Horse ID.");
        if (agility < 0 || agility > EventVariables.HORSE_MAX_STEP)
            throw new IllegalArgumentException("Invalid Horse agility.");
        if (raceID >= EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid Race ID.");
        if (raceIdx >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid Horse race index.");
        if (s == null || p == null || r == null)
            throw new IllegalArgumentException("Invalid shared region reference.");

        this.state = HorseState.AT_THE_STABLE;
        this.id = id;
        this.agility = agility;
        this.raceID = raceID;
        this.raceIdx = raceIdx;
        this.stable = s;
        this.paddock = p;
        this.racingTrack = r;
    }

    private int makeAStep() {
        Random rnd = new Random();
        return (int)(rnd.nextGaussian() * 5);
    }

    public void run() {
        stable.proceedToStable();

        paddock.proceedToPaddock();

        racingTrack.proceedToStartLine();

        while (!racingTrack.hasFinishLineBeenCrossed())
            racingTrack.makeAMove(makeAStep());

        stable.proceedToStable();
    }

    public void setHorseState(states.State state) {
        this.state = state;
    }

    public states.State getHorseState() {
        return state;
    }

    public int getID() {
        return id;
    }

    public int getAgility() {
        return agility;
    }

    public int getRaceID() {
        return raceID;
    }

    public int getRaceIdx() {
        return raceIdx;
    }
}

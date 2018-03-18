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

    public Horse(int id, int agility, Stable s, Paddock p, RacingTrack r) {
        if (id < 0)
            throw new IllegalArgumentException("Invalid Horse ID.");
        if (agility < 0 || agility > EventVariables.HORSE_MAX_STEP)
            throw new IllegalArgumentException("Invalid Horse agility.");
        if (s == null || p == null || r == null)
            throw new IllegalArgumentException("Invalid shared region reference.");

        this.state = HorseState.AT_THE_STABLE;
        this.id = id;
        this.agility = agility;
        this.raceID = -1;
        this.raceIdx = -1;
        this.stable = s;
        this.paddock = p;
        this.racingTrack = r;
    }

    private int makeAStep() {
        Random rnd = new Random();
        return rnd.nextInt(agility) + 1;
    }

    public void run() {
        stable.proceedToStable();
        System.out.println("INDO PARA O PADDOCK CAVALO " + id);
        paddock.proceedToPaddock();
        System.out.println("INDO PARA A STARTING LINE " + id);
        racingTrack.proceedToStartLine();

        while (!racingTrack.hasFinishLineBeenCrossed())
            racingTrack.makeAMove(makeAStep());

        stable.proceedToStable();
    }

    public void setHorseState(states.State state) {
        if (state == null)
            throw new IllegalArgumentException("Invalid Horse state");
        this.state = state;
    }

    public states.State getHorseState() {
        return state;
    }

    public void setRaceID(int raceID) {
        if (raceID < 0 || raceID > EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid Race ID");
        this.raceID = raceID;
    }

    public void setRaceIdx(int raceIdx) {
        if (raceIdx < 0 || raceIdx >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid Horse race index.");
        this.raceIdx = raceIdx;
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

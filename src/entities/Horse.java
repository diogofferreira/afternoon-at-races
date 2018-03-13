package entities;

import main.EventVariables;
import sharedRegions.Paddock;
import sharedRegions.RacingTrack;
import sharedRegions.Stable;
import states.HorseState;
import states.State;

public class Horse {
    private State state;
    private int id;
    private double agility;

    private Stable stable;
    private Paddock paddock;
    private RacingTrack racingTrack;

    public Horse(int id, int agility, Stable s, Paddock p, RacingTrack r) {
        if (id < 0)
            throw new IllegalArgumentException("Invalid Horse ID.");
        if (agility < 0)
            throw new IllegalArgumentException("Invalid Horse agility.");
        if (s == null || p == null || r == null)
            throw new IllegalArgumentException("Invalid shared region reference.");

        this.state = HorseState.AT_THE_STABLE;
        this.id = id;
        this.agility = agility;
        this.stable = s;
        this.paddock = p;
        this.racingTrack = r;
    }

    private int makeAStep() {
        return EventVariables.RACING_TRACK_LENGTH / 5;
    }

    public void run() {
        stable.proceedToStable(this.id);

        paddock.proceedToPaddock(this.id);

        racingTrack.proceedToStartLine(this.id);

        while (!racingTrack.hasFinishLineBeenCrossed(this.id))
            racingTrack.makeAMove(this.id, makeAStep());

        stable.proceedToStable(this.id);
    }
}

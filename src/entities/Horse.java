package entities;

import sharedRegions.Paddock;
import sharedRegions.RacingTrack;
import sharedRegions.Stable;

public class Horse {

    private State state;
    private int id;
    private double agility;

    private int makeAStep() {
        // calculate step
    }

    public void run() {
        Stable.proceedToStable(this.id);

        Paddock.proceedToPaddock(this.id);

        RacingTrack.proceedToStartLine(this.id);

        while (!RacingTrack.hasFinishLineBeenCrossed(this.id))
            RacingTrack.makeAMove(this.id, makeAStep());

        Stable.proceedToStable(this.id);
    }
}

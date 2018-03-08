package entities;

import sharedRegions.RacingTrack;

public class Horse {

    private State state;
    private int id;
    private double agility;

    private makeAStep() {
        // calculate step
    }

    public void run() {
        Stable.proceedToStable(self.id);

        Paddock.proceedToPaddock(self.id);

        RacingTrack.proceedToStartLine(self.id);

        while (!RacingTrack.hasFinishLineBeenCrossed(self.id))
            RacingTrack.makeAMove(self.id, self.makeAStep());

        Stable.proceedToStable(self.id);
    }


}

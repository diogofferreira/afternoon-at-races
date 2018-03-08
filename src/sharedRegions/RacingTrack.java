package sharedRegions;

public class RacingTrack {
    /* FIFO with arrival order */
    /* step number */

    public void proceedToStartLine(int horseId) {
        /* add to arrival FIFO */
        /* last horse notify all spectators */
        Paddock.proceedToStartLine();
        /* horse wait */
    }

    public void startTheRace() {
        /* notify all horses */
    }

    public void makeAMove(int horseId, int step) {
        /* notify next horse in FIFO */
        /* if horse is last in FIFO */
            stepNumber++;
    }

    public void hasFinishLineBeenCrossed(int horseId) {
        /* if ! horse has crossed finish line */
            /* horse wait */
        /* if horse is last */
            /* notify broker */

    }
}

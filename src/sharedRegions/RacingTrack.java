package sharedRegions;

import main.EventVariables;
import utils.Racer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *    General description:
 *       definition of shared region RacingTrack built in explicitly as a monitor using reference types from the
 *         Java concurrency library.
 */

public class RacingTrack {

    private Lock mutex;
    private Condition inStartingLine, inFinishLine;

    // list with arrival order
    private List<Racer> horses;

    // step number
    private int stepNumber;

    public RacingTrack() {
        this.mutex = new ReentrantLock();
        this.inStartingLine = this.mutex.newCondition();
        this.inFinishLine = this.mutex.newCondition();
        this.horses = new ArrayList<>();
        this.stepNumber = 0;
    }

    public void proceedToStartLine(int horseId) {
        // add horse to arrival list
        horses.add(new Racer(horseId));
        // last horse notify all spectators
        if (horses.size() == EventVariables.NUMBER_OF_HORSES)
            Paddock.proceedToStartLine();
        // horse wait for race start
        try {
            inStartingLine.wait();
        } catch (InterruptedException ignored){}
    }

    public void startTheRace() {
        // notify all horses for race start
        inStartingLine.notifyAll();
    }

    public void makeAMove(int horseId, int step) {
        // notify next horse in FIFO
        int horseIdx = horses.indexOf(horses.get(horseId));
        // update current position
        horses.get(horseId).setCurrentPosition(step);

        // TODO: how to notify specific horse?

        // last horse increase step number
        if (horseIdx == horses.size() - 1)
            stepNumber++;
    }

    public boolean hasFinishLineBeenCrossed(int horseId) {
        // horse wait if has crossed finish line
        if (horses.get(horseId).getCurrentPosition() >=
                EventVariables.RACING_TRACK_LENGTH) {
            try {
                inFinishLine.wait();
            } catch (InterruptedException ignored){}
        }
        int horseIdx = horses.indexOf(horses.get(horseId));
        // last horse notify broker */
        if (horseIdx == horses.size())
            // TODO: notify broker
    }
}

package sharedRegions;

import main.EventVariables;
import utils.Racer;

import javax.naming.ldap.Control;
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
    private Condition inStartingLine, inMovement;
    private ControlCentre controlCentre;
    private Paddock paddock;

    // list with arrival order
    private List<Racer> horses;
    private int horseTurn;
    private List<Racer> winners;
    private int finishes;

    // step number
    private int stepNumber;



    public RacingTrack(ControlCentre c, Paddock p) {
        if (c == null)
            throw new IllegalArgumentException("Invalid Control Centre.");
        if (p == null)
            throw new IllegalArgumentException("Invalid Paddock.");

        this.mutex = new ReentrantLock();
        this.inStartingLine = this.mutex.newCondition();
        this.inMovement = this.mutex.newCondition();
        this.horses = new ArrayList<>();
        this.horseTurn = 0;
        this.winners = new ArrayList<>();
        this.stepNumber = 0;
        this.finishes = 0;
    }

    public void proceedToStartLine(int horseID) {
        // add horse to arrival list
        horses.add(new Racer(horseID));
        // last horse notify all spectators
        if (horses.size() == EventVariables.NUMBER_OF_HORSES)
            paddock.proceedToStartLine();

        while (horses.get(horseTurn).getId() != horseID) {
            // horse wait for race start
            try {
                inStartingLine.wait();
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void startTheRace() {
        // notify all horses for race start
        inStartingLine.notifyAll();
    }

    public void makeAMove(int horseId, int step) {
        // notify next horse in FIFO
        // update current position
        horses.get(horseTurn).setCurrentPosition(step);

        // last horse increase step number
        if (horseTurn == horses.size() - 1)
            stepNumber++;

        horseTurn = (horseTurn + 1) % EventVariables.NUMBER_OF_HORSES_PER_RACE;
    }

    public boolean hasFinishLineBeenCrossed(int horseId) {
        // horse wait if has crossed finish line
        Racer racer;

        racer = horses.get(horseTurn);
        if (racer.getCurrentPosition() <
                EventVariables.RACING_TRACK_LENGTH) {
            try {
                inMovement.wait();
            } catch (InterruptedException ignored){}
        }

        // Add to winners
        if (winners.isEmpty() ||
                winners.get(0).getCurrentStep() == racer.getCurrentStep())
            winners.add(racer);

        // last horse notify broker */
        if (++finishes == EventVariables.NUMBER_OF_HORSES_PER_RACE)
            controlCentre.finishTheRace();
    }
}

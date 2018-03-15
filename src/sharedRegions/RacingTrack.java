package sharedRegions;

import entities.Broker;
import entities.Horse;
import generalRepository.GeneralRepository;
import main.EventVariables;
import states.BrokerState;
import states.HorseState;
import sun.awt.windows.ThemeReader;
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
    private GeneralRepository generalRepository;

    // list with arrival order
    private List<Racer> horses;
    private int horseTurn;
    private List<Racer> winners;
    private int finishes;

    // step number
    private int stepNumber;

    public RacingTrack(ControlCentre c, Paddock p, GeneralRepository gr) {
        if (c == null)
            throw new IllegalArgumentException("Invalid Control Centre.");
        if (p == null)
            throw new IllegalArgumentException("Invalid Paddock.");
        if (gr == null)
            throw new IllegalArgumentException("Invalid General Repository.");

        this.paddock = p;
        this.controlCentre = c;
        this.generalRepository = gr;
        this.mutex = new ReentrantLock();
        this.inStartingLine = this.mutex.newCondition();
        this.inMovement = this.mutex.newCondition();
        this.horses = new ArrayList<>();
        this.horseTurn = 0;
        this.winners = new ArrayList<>();
        this.stepNumber = 0;
        this.finishes = 0;
    }

    public int[] getWinners() {
        int[] winnerIdxs;
        List<Integer> w = new ArrayList<>();
        for (Racer winner : winners)
            w.add(winner.getIdx());


        winnerIdxs = new int[w.size()];
        for (int i = 0; i < w.size(); i++)
            winnerIdxs[i] = w.get(i);

        return winnerIdxs;
    }

    public void proceedToStartLine() {
        Horse h;
        mutex.lock();

        h = (Horse)Thread.currentThread();
        h.setHorseState(HorseState.AT_THE_START_LINE);
        generalRepository.setHorseState(h.getRaceIdx(),
                HorseState.AT_THE_START_LINE);
        generalRepository.setHorsePosition(h.getRaceIdx(), 0);

        // add horse to arrival list
        horses.add(new Racer(h.getRaceIdx()));

        // last horse notify all spectators
        if (horses.size() == EventVariables.NUMBER_OF_HORSES)
            paddock.proceedToStartLine();

        while (horses.get(horseTurn).getIdx() != h.getRaceIdx()) {
            // horse wait for race start
            try {
                inStartingLine.await();
            } catch (InterruptedException ignored) {
            }
        }

        mutex.unlock();
    }

    public void startTheRace() {
        Broker b;
        mutex.lock();

        b = (Broker) Thread.currentThread();
        b.setBrokerState(BrokerState.SUPERVISING_THE_RACE);
        generalRepository.setBrokerState(BrokerState.SUPERVISING_THE_RACE);

        // notify all horses for race start
        inStartingLine.notifyAll();

        mutex.unlock();
    }

    public void makeAMove(int step) {
        Horse h;
        Racer r;
        mutex.lock();

        h = (Horse)Thread.currentThread();
        h.setHorseState(HorseState.RUNNING);
        generalRepository.setHorseState(h.getRaceIdx(), HorseState.RUNNING);

        // notify next horse in FIFO
        // update current position
        r = horses.get(horseTurn);
        r.setCurrentPosition(step);
        generalRepository.setHorsePosition(h.getRaceIdx(),
                r.getCurrentPosition(), stepNumber);

        // last horse increase step number
        if (horseTurn == horses.size() - 1)
            stepNumber++;

        horseTurn = (horseTurn + 1) % EventVariables.NUMBER_OF_HORSES_PER_RACE;

        mutex.unlock();
    }

    public boolean hasFinishLineBeenCrossed() {
        Horse h;
        mutex.lock();

        h = (Horse)Thread.currentThread();

        // horse wait if has crossed finish line
        Racer racer;

        racer = horses.get(horseTurn);
        if (racer.getCurrentPosition() < EventVariables.RACING_TRACK_LENGTH) {
            try {
                inMovement.await();
                return false;
            } catch (InterruptedException ignored){}
        }

        generalRepository.setHorseEnded(h.getRaceIdx());
        h.setHorseState(HorseState.AT_THE_FINISH_LINE);
        generalRepository.setHorseState(h.getRaceIdx(),
                HorseState.AT_THE_FINISH_LINE);

        // Add to winners
        if (winners.isEmpty() ||
                winners.get(0).getCurrentStep() == racer.getCurrentStep())
            winners.add(racer);

        // last horse notify broker */
        if (++finishes == EventVariables.NUMBER_OF_HORSES_PER_RACE) {
            controlCentre.finishTheRace();

            // reset empty track variables
            this.horses.clear();
            this.horseTurn = 0;
            this.winners.clear();
            this.stepNumber = 0;
            this.finishes = 0;
        }

        mutex.unlock();

        return true;
    }
}

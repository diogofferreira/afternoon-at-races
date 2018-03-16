package sharedRegions;

import entities.Broker;
import entities.Horse;
import main.EventVariables;
import states.BrokerState;
import states.HorseState;
import utils.Racer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
    private Queue<Condition> inMovement;
    private ControlCentre controlCentre;
    private Paddock paddock;
    private GeneralRepository generalRepository;

    // list with arrival order
    private Queue<Racer> racers;
    private List<Racer> winners;
    private int finishes;


    public RacingTrack(GeneralRepository gr, ControlCentre c, Paddock p) {
        if (gr == null)
            throw new IllegalArgumentException("Invalid General Repository.");
        if (c == null)
            throw new IllegalArgumentException("Invalid Control Centre.");
        if (p == null)
            throw new IllegalArgumentException("Invalid Paddock.");

        this.generalRepository = gr;
        this.paddock = p;
        this.controlCentre = c;
        this.mutex = new ReentrantLock();
        this.inMovement = new LinkedList<>();
        this.racers = new LinkedList<>();
        this.winners = new ArrayList<>();
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
        generalRepository.setHorsePosition(h.getRaceIdx(), 0, 0);

        // add horse to arrival list
        Condition cond = mutex.newCondition();
        inMovement.add(cond);
        racers.add(new Racer(h.getRaceIdx()));

        // last horse notify all spectators
        if (inMovement.size() == EventVariables.NUMBER_OF_HORSES_PER_RACE)
            paddock.proceedToStartLine();

        try {
            cond.await();
        } catch (InterruptedException ignored) { }

        mutex.unlock();
    }

    public void startTheRace() {
        Broker b;
        mutex.lock();

        b = (Broker) Thread.currentThread();
        b.setBrokerState(BrokerState.SUPERVISING_THE_RACE);
        generalRepository.setBrokerState(BrokerState.SUPERVISING_THE_RACE);

        // notify all horses for race start
        inMovement.peek().signal();

        mutex.unlock();
    }

    public void makeAMove(int step) {
        Horse h;
        Racer r;
        Condition c;
        mutex.lock();

        h = (Horse)Thread.currentThread();
        h.setHorseState(HorseState.RUNNING);
        generalRepository.setHorseState(h.getRaceIdx(), HorseState.RUNNING);

        r = racers.poll();
        c = inMovement.poll();

        // notify next horse in FIFO
        // update current position
        r.setCurrentPosition(step);
        generalRepository.setHorsePosition(h.getRaceIdx(),
                r.getCurrentPosition(), r.getCurrentStep());


        System.out.println("HORSE IDX = " + h.getRaceIdx());
        System.out.println("HORSE POS = " + r.getCurrentPosition());

        // Signal next horse
        inMovement.peek().signal();

        // Add elements to end of FIFO
        racers.add(r);
        inMovement.add(c);

        // wait for its turn
        try {
            c.await();
        } catch (InterruptedException ignored) { }

        System.out.println("WAKE UP" + h.getRaceIdx());

        mutex.unlock();
    }

    public boolean hasFinishLineBeenCrossed() {
        Horse h;
        Racer racer;
        Condition c;
        mutex.lock();

        h = (Horse)Thread.currentThread();

        // if has crossed finish line
        racer = racers.peek();
        System.out.println("WAKE UP" + racer.getIdx());

        System.out.println(inMovement);
        System.out.println(racers);

        if (racer.getCurrentPosition() < EventVariables.RACING_TRACK_LENGTH)
            return false;

        racer = racers.poll();
        c = inMovement.poll();
        System.out.println("RACER HAS FINISHED? " + racer.getIdx());

        //System.out.println(racers.peek());
        //System.out.println(inMovement.peek());

        generalRepository.setHorseEnded(h.getRaceIdx());
        h.setHorseState(HorseState.AT_THE_FINISH_LINE);
        generalRepository.setHorseState(h.getRaceIdx(),
                HorseState.AT_THE_FINISH_LINE);

        // Add to winners
        if (winners.isEmpty() ||
                winners.get(0).getCurrentStep() == racer.getCurrentStep())
            winners.add(racer);

        System.out.println(winners);

        // last horse notify broker */
        if (++finishes == EventVariables.NUMBER_OF_HORSES_PER_RACE) {
            controlCentre.finishTheRace(getWinners());

            // reset empty track variables
            this.racers.clear();
            this.inMovement.clear();
            this.winners.clear();
            this.finishes = 0;
        }

        inMovement.peek().signalAll();


        mutex.unlock();

        return true;
    }
}

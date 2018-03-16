package sharedRegions;

import entities.Broker;
import entities.Horse;
import main.EventVariables;
import states.BrokerState;
import states.HorseState;
import utils.Racer;

import javax.annotation.processing.SupportedSourceVersion;
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
    private Condition[] inMovement;
    private ControlCentre controlCentre;
    private Paddock paddock;
    private GeneralRepository generalRepository;

    // list with arrival order
    private Racer[] racers;
    private List<Racer> winners;
    private int horseTurn;
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
        this.inMovement = new Condition[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.racers = new Racer[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.winners = new ArrayList<>();
        this.finishes = 0;
        this.horseTurn = 0;
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
        int i;
        mutex.lock();

        h = (Horse)Thread.currentThread();
        h.setHorseState(HorseState.AT_THE_START_LINE);
        generalRepository.setHorseState(h.getRaceIdx(),
                HorseState.AT_THE_START_LINE);
        generalRepository.setHorsePosition(h.getRaceIdx(), 0, 0);

        // add horse to arrival list
        i = 0;
        while (inMovement[i] != null) i++;

        inMovement[i] = mutex.newCondition();
        racers[i] = new Racer(h.getRaceIdx());

        // last horse notify all spectators
        if (i == EventVariables.NUMBER_OF_HORSES_PER_RACE - 1)
            paddock.proceedToStartLine();

        try {
            inMovement[i].await();
        } catch (InterruptedException ignored) { }

        mutex.unlock();
    }

    public void startTheRace() {
        Broker b;
        mutex.lock();

        b = (Broker) Thread.currentThread();
        b.setBrokerState(BrokerState.SUPERVISING_THE_RACE);
        generalRepository.setBrokerState(BrokerState.SUPERVISING_THE_RACE);

        // notify first horse for race start
        inMovement[horseTurn].signal();

        mutex.unlock();
    }

    public void makeAMove(int step) {
        Horse h;
        int currentTurn;
        mutex.lock();

        currentTurn = horseTurn;
        h = (Horse)Thread.currentThread();
        h.setHorseState(HorseState.RUNNING);
        generalRepository.setHorseState(h.getRaceIdx(), HorseState.RUNNING);

        // notify next horse in FIFO
        // update current position
        racers[horseTurn].setCurrentPosition(step);
        generalRepository.setHorsePosition(h.getRaceIdx(),
                racers[horseTurn].getCurrentPosition(),
                racers[horseTurn].getCurrentStep());

        // Signal next horse
        do {
            horseTurn = (horseTurn + 1) % EventVariables.NUMBER_OF_HORSES_PER_RACE;
        } while (racers[horseTurn] == null);

        if (horseTurn != currentTurn) {
            // Signal next horse
            inMovement[horseTurn].signal();

            // wait for next turn
            try {
                inMovement[currentTurn].await();
            } catch (InterruptedException ignored) { }
        }

        mutex.unlock();
    }

    public boolean hasFinishLineBeenCrossed() {
        Horse h;
        Racer racer;
        int currentTurn;
        mutex.lock();

        currentTurn = horseTurn;
        h = (Horse)Thread.currentThread();
        racer = racers[horseTurn];

        if (racer.getCurrentPosition() < EventVariables.RACING_TRACK_LENGTH) {
            mutex.unlock();
            return false;
        }

        System.out.println("RACER HAS FINISHED: " + racer.getIdx());

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

        // last horse notify broker
        System.out.println(finishes);
        if (++finishes == EventVariables.NUMBER_OF_HORSES_PER_RACE) {
            controlCentre.finishTheRace(getWinners());

            // reset empty track variables
            this.racers = new Racer[EventVariables.NUMBER_OF_HORSES_PER_RACE];
            this.inMovement =
                    new Condition[EventVariables.NUMBER_OF_HORSES_PER_RACE];
            this.winners.clear();
            this.finishes = 0;
        } else {
            // Signal next horse
            do {
                horseTurn = (horseTurn + 1) % EventVariables.NUMBER_OF_HORSES_PER_RACE;
            } while (racers[horseTurn] == null);

            // Remove current racer from the race
            racers[currentTurn] = null;
            inMovement[currentTurn] = null;

            // Signal next horse
            if (horseTurn != currentTurn)
                inMovement[horseTurn].signal();
        }
        mutex.unlock();

        return true;
    }
}

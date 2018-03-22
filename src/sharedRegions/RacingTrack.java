package sharedRegions;

import entities.Broker;
import entities.Horse;
import main.EventVariables;
import states.BrokerState;
import states.HorseState;

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
    private Condition[] inMovement;
    private ControlCentre controlCentre;
    private Paddock paddock;
    private GeneralRepository generalRepository;

    // list with arrival order
    private List<Integer> winners;
    private int winnerStep;
    private int horseTurn;
    private int finishes;

    private boolean raceStarted;


    public RacingTrack(GeneralRepository generalRepository,
                       ControlCentre controlCentre, Paddock paddock) {
        if (generalRepository == null)
            throw new IllegalArgumentException("Invalid General Repository.");
        if (controlCentre == null)
            throw new IllegalArgumentException("Invalid Control Centre.");
        if (paddock == null)
            throw new IllegalArgumentException("Invalid Paddock.");

        this.generalRepository = generalRepository;
        this.paddock = paddock;
        this.controlCentre = controlCentre;
        this.mutex = new ReentrantLock();
        this.inMovement = new Condition[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        this.winners = new ArrayList<>();
        this.winnerStep = -1;
        this.finishes = 0;
        this.horseTurn = 0;
        this.raceStarted = false;
    }

    public int[] getWinners() {
        return winners.stream().mapToInt(i->i).toArray();
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

        // last horse notify all spectators
        if (i == EventVariables.NUMBER_OF_HORSES_PER_RACE - 1)
            paddock.proceedToStartLine();

        // Horse waits if race hasn't started and if it isn't its turn
        while (!(raceStarted && horseTurn == i)) {
            try {
                inMovement[i].await();
            } catch (InterruptedException ignored) { }
        }

        h.setHorseState(HorseState.RUNNING);
        generalRepository.setHorseState(h.getRaceIdx(), HorseState.RUNNING);

        mutex.unlock();
    }

    public void startTheRace() {
        Broker b;
        mutex.lock();

        b = (Broker) Thread.currentThread();
        b.setBrokerState(BrokerState.SUPERVISING_THE_RACE);
        generalRepository.setBrokerState(BrokerState.SUPERVISING_THE_RACE);

        // notify first horse for race start
        raceStarted = true;
        inMovement[horseTurn].signal();

        mutex.unlock();
    }

    public void makeAMove(int step) {
        Horse h;
        int currentTurn;
        mutex.lock();

        currentTurn = horseTurn;
        h = (Horse)Thread.currentThread();

        // notify next horse in FIFO
        // update current position
        h.setCurrentPosition(step);
        generalRepository.setHorsePosition(h.getRaceIdx(),
                h.getCurrentPosition(),
                h.getCurrentStep());

        // Signal next horse
        do {
            horseTurn = (horseTurn + 1) % EventVariables.NUMBER_OF_HORSES_PER_RACE;
        } while (inMovement[horseTurn] == null);

        // if it hasn't looped wakes next horse, else continues
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
        int currentTurn;
        mutex.lock();

        currentTurn = horseTurn;
        h = (Horse)Thread.currentThread();

        if (h.getCurrentPosition() < EventVariables.RACING_TRACK_LENGTH) {
            mutex.unlock();
            return false;
        }

        generalRepository.setHorseEnded(h.getRaceIdx());
        h.setHorseState(HorseState.AT_THE_FINISH_LINE);
        generalRepository.setHorseState(h.getRaceIdx(),
                HorseState.AT_THE_FINISH_LINE);

        // Add to winners
        if (winners.isEmpty() || winnerStep == h.getCurrentStep()) {
            winners.add(h.getRaceIdx());
            winnerStep = h.getCurrentStep();
        }

        // last horse notify broker
        if (++finishes == EventVariables.NUMBER_OF_HORSES_PER_RACE) {
            int[] raceWinners = getWinners();

            // reset empty track variables
            this.inMovement =
                    new Condition[EventVariables.NUMBER_OF_HORSES_PER_RACE];
            this.winners.clear();
            this.finishes = 0;
            horseTurn = 0;
            raceStarted = false;

            controlCentre.finishTheRace(raceWinners);


        } else {
            // Signal next horse
            do {
                horseTurn = (horseTurn + 1) % EventVariables.NUMBER_OF_HORSES_PER_RACE;
            } while (inMovement[horseTurn] == null);

            // Remove current racer from the race
            inMovement[currentTurn] = null;

            // Signal next horse
            if (horseTurn != currentTurn)
                inMovement[horseTurn].signal();
        }
        mutex.unlock();

        return true;
    }
}

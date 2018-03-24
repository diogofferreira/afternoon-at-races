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
 * The Racing Track is a shared region where race effectively takes place, i.e.,
 * where the Horses run.
 */
public class RacingTrack {

    /**
     * Instance of a monitor.
     */
    private Lock mutex;

    /**
     * Array of condition variables, which will store a condition variable to
     * each of one the races. The horses wait in each one of these variables
     * accordingly to their raceIdx order.
     */
    private Condition[] inMovement;

    /**
     * Flag that signals if the race has already started.
     */
    private boolean raceStarted;

    /**
     * Counter that indicates how many Horses have already arrived to the
     * Racing Track and are ready to run.
     */
    private int horsesReady;

    /**
     * Number that represents the Horse that will make a step in the current
     * iteration. It can take values between 0 and NUMBER_OF_HORSES_PER_RACE - 1.
     */
    private int horseTurn;

    /**
     * List that saves the horseIdx of the horses that won the race.
     */
    private List<Integer> winners;

    /**
     * Number of steps the winners took to get the victory.
     */
    private int winnerStep;

    /**
     * Number of horses that have already crossed the finish line.
     */
    private int finishes;

    /**
     * Instance of the shared region Control Centre.
     */
    private ControlCentre controlCentre;

    /**
     * Instance of the shared region Paddock.
     */
    private Paddock paddock;

    /**
     * Instance of the shared region General Repository.
     */
    private GeneralRepository generalRepository;

    /**
     * Creates a new instance of Racing Track.
     * @param generalRepository Reference to an instance of the shared region
     *                          General Repository.
     * @param controlCentre Reference to an instance of the shared region
     *                          Control Centre.
     * @param paddock Reference to an instance of the shared region Paddock.
     */
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
        this.horsesReady = 0;
        this.horseTurn = 0;
        this.raceStarted = false;
    }

    /**
     * Method invoked by each one the Horses coming from the Paddock.
     * They will update their state to AT_THE_STARTING_LINE and will block
     * accordingly to the raceIdx of each one of them in the correspondent
     * condition variable.
     * The last Horse/Jockey pair to arrive also wakes up the Spectators so
     * then can place their bets.
     * After being waken up by the Broker to start the race, they update their
     * state to RUNNING.
     */
    public void proceedToStartLine() {
        Horse h;
        mutex.lock();

        h = (Horse)Thread.currentThread();
        h.setHorseState(HorseState.AT_THE_STARTING_LINE);
        generalRepository.setHorseState(h.getRaceIdx(),
                HorseState.AT_THE_STARTING_LINE);

        // add horse to arrival list
        inMovement[h.getRaceIdx()] = mutex.newCondition();

        // last horse notify all spectators
        if (++horsesReady == EventVariables.NUMBER_OF_HORSES_PER_RACE) {
            horsesReady = 0;
            paddock.proceedToBettingCentre();
        }

        // Horse waits if race hasn't started and if it isn't its turn
        while (!(raceStarted && horseTurn == h.getRaceIdx())) {
            try {
                inMovement[h.getRaceIdx()].await();
            } catch (InterruptedException ignored) { }
        }

        h.setHorseState(HorseState.RUNNING);
        generalRepository.setHorseState(h.getRaceIdx(), HorseState.RUNNING);

        mutex.unlock();
    }

    /**
     * Method invoked by the Broker to signal the Horses to start running.
     */
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

    /**
     * Method invoked by every Horse that hasn't still crossed the finish line.
     * It generates a new step and updates its position.
     * Finally, it wakes up the next horse in the arrival order to the Racing
     * Track that hasn't finished the race.
     * @param step The distance of the next step the Horse will take.
     */
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

    /**
     * Method invoked by each one of the participating Horses checking they have
     * already crossed the finish line.
     * If true, they check if they have won the race and add their ID to the
     * corresponding list of winners.
     * If it's the last horse crossing the finish line, it wakes up the Broker
     * at the Control Centre and provides the list of winners.
     * Otherwise, just wakes up the next horse that still hasn't finish the race.
     * @return Boolean indicating whether the Horse/Jockey pair that invoked the
     * method has already crossed the finish line or not.
     */
    public boolean hasFinishLineBeenCrossed() {
        Horse h;
        int currentTurn;
        int[] raceWinners;
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
            raceWinners = winners.stream().mapToInt(i->i).toArray();

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

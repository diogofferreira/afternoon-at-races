package sharedRegions;

import interfaces.ControlCentreInt;
import interfaces.GeneralRepositoryInt;
import interfaces.RacingTrackInt;
import main.EventVariables;
import main.RacingTrackMain;
import states.BrokerState;
import states.HorseState;

import java.rmi.RemoteException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The Racing Track is a shared region where race effectively takes place, i.e.,
 * where the Horses run.
 */
public class RacingTrack implements RacingTrackInt {

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
     * Number that represents the Horse that will make a step in the current
     * iteration. It can take values between 0 and NUMBER_OF_HORSES_PER_RACE - 1.
     */
    private int horseTurn;

    /**
     * List that saves the standing of the horses in the race.
     */
    private int[] raceStandings;

    /**
     * Number of steps the current horse took to get the finish line.
     */
    private int currentStep;

    /**
     * Position of the current horse when he get the finish line.
     */
    private int currentPosition;

    /**
     * Number of horses that have already crossed the finish line.
     */
    private int finishes;

    /**
     * Instance of the shared region Control Centre.
     */
    private ControlCentreInt controlCentre;

    /**
     * Instance of the shared region General Repository.
     */
    private GeneralRepositoryInt generalRepository;

    /**
     * Counter to check how many requests were made to the Racing Track
     * in order to end its life cycle.
     */
    private int requests;

    /**
     * Creates a new instance of Racing Track.
     * @param generalRepository Reference to an instance of the shared region
     *                          General Repository.
     * @param controlCentre Reference to an instance of the shared region
     *                          Control Centre.
     */
    public RacingTrack(GeneralRepositoryInt generalRepository,
                       ControlCentreInt controlCentre) {
        if (generalRepository == null)
            throw new IllegalArgumentException("Invalid General Repository.");
        if (controlCentre == null)
            throw new IllegalArgumentException("Invalid Control Centre.");

        this.generalRepository = generalRepository;
        this.controlCentre = controlCentre;
        this.mutex = new ReentrantLock();
        this.inMovement = new Condition[EventVariables.NUMBER_OF_HORSES_PER_RACE];

        for (int i = 0; i < inMovement.length; i++)
            this.inMovement[i] = this.mutex.newCondition();

        this.currentStep = 0;
        this.finishes = 0;
        this.horseTurn = 0;
        this.raceStarted = false;
        this.requests = 0;
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
     * @param raceId The race ID in which the horse will run.
     * @param raceIdx The horse's index/position on the race.
     */
    public void proceedToStartLine(int raceId, int raceIdx) {
        mutex.lock();

        try {
            generalRepository.setHorseState(raceId, raceIdx,
                    HorseState.AT_THE_STARTING_LINE);
        } catch (RemoteException e) {
            System.out.println("GeneralRepository remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // horse waits if race hasn't started and if it isn't its turn
        while (!(raceStarted && horseTurn == raceIdx)) {
            try {
                inMovement[raceIdx].await();
            } catch (InterruptedException ignored) { }
        }

        mutex.unlock();
    }

    /**
     * Method invoked by the Broker to signal the Horses to start running.
     */
    public void startTheRace() {
        mutex.lock();

        try {
            generalRepository.setBrokerState(BrokerState.SUPERVISING_THE_RACE);
        } catch (RemoteException e) {
            System.out.println("GeneralRepository remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

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
     * @param raceId The race ID in which the horse will run.
     * @param raceIdx The horse's index/position on the race.
     * @param currentHorseStep The current step/iteration of the horse in the race.
     * @param currentPosition The current position of the horse in the race.
     * @param step The distance of the next step the Horse will take.
     */
    public void makeAMove(int raceId, int raceIdx, int currentHorseStep,
                          int currentPosition, int step) {
        int currentTurn;
        mutex.lock();

        currentTurn = horseTurn;

        if (currentHorseStep == 0) {
            try {
                generalRepository.setHorseState(raceId, raceIdx,
                        HorseState.RUNNING);
            } catch (RemoteException e) {
                System.out.println("GeneralRepository remote invocation exception: "
                        + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }

        // update current position
        try {
            generalRepository.setHorsePosition(raceIdx, currentPosition + step,
                    currentHorseStep + 1);
        } catch (RemoteException e) {
            System.out.println("GeneralRepository remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // signal next horse
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
     * @param raceId The race ID in which the horse will run.
     * @param raceIdx The horse's index/position on the race.
     * @param currentHorseStep The current step/iteration of the horse in the race.
     * @param currentPosition The current position of the horse in the race.
     * @return Boolean indicating whether the Horse/Jockey pair that invoked the
     * method has already crossed the finish line or not.
     */
    public boolean hasFinishLineBeenCrossed(int raceId, int raceIdx, int currentHorseStep,
                                            int currentPosition) {
        int currentTurn;
        mutex.lock();

        currentTurn = horseTurn;

        if (currentPosition < EventVariables.RACING_TRACK_LENGTH) {
            mutex.unlock();
            return false;
        }

        try {
            generalRepository.setHorseState(raceId, raceIdx,
                    HorseState.AT_THE_FINISH_LINE);
        } catch (RemoteException e) {
            System.out.println("GeneralRepository remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // add horse to arrival list
        if (raceStandings == null) {
            raceStandings = new int [EventVariables.NUMBER_OF_HORSES_PER_RACE];
            currentPosition = 1;
        } else {
            currentPosition = currentStep != currentHorseStep ?
                    currentPosition + 1 : currentPosition;
        }

        currentStep = currentHorseStep;

        raceStandings[raceIdx] = currentPosition;

        // last horse notify broker
        if (++finishes == EventVariables.NUMBER_OF_HORSES_PER_RACE) {
            // reset empty track variables
            this.inMovement =
                    new Condition[EventVariables.NUMBER_OF_HORSES_PER_RACE];

            for (int i = 0; i < inMovement.length; i++)
                this.inMovement[i] = this.mutex.newCondition();

            this.finishes = 0;
            horseTurn = 0;
            raceStarted = false;

            try {
                controlCentre.finishTheRace(raceStandings);
            } catch (RemoteException e) {
                System.out.println("ControlCentre remote invocation exception: "
                        + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }

            raceStandings = null;

        } else {
            // signal next horse
            do {
                horseTurn = (horseTurn + 1) % EventVariables.NUMBER_OF_HORSES_PER_RACE;
            } while (inMovement[horseTurn] == null);

            // remove current racer from the race
            inMovement[currentTurn] = null;

            // signal next horse
            if (horseTurn != currentTurn)
                inMovement[horseTurn].signal();
        }

        if (++requests == EventVariables.NUMBER_OF_HORSES)
            RacingTrackMain.wakeUp();

        mutex.unlock();

        return true;
    }
}

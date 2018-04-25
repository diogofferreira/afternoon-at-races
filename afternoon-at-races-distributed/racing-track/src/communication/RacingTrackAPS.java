package communication;

import entities.BrokerInt;
import entities.HorseInt;
import main.EventVariables;
import messages.RacingTrackMessage;
import sharedRegions.RacingTrackInterface;
import states.BrokerState;
import states.HorseState;

/**
 * This data type defines the server side thread that effectively provides the
 * service; the communication is based on changing messages with the client
 * via a TCP channel.
 */
public class RacingTrackAPS extends Thread implements BrokerInt, HorseInt {

    /**
     * Instance of a communication socket with the client.
     */
    private ServerCom com;

    /**
     * Service to provide to the client - Racing Track.
     */
    private RacingTrackInterface rtInt;

    /**
     * ID of the Horse/Jockey pair.
     */
    private int id;

    /**
     * Current state of the Horse/Jockey lifecycle.
     */
    private HorseState horseState;

    /**
     * Agility of the horse, which in practice corresponds to the maximum step
     * the horse can make in each iteration.
     */
    private int agility;

    /**
     * The race ID in which the horse will run.
     */
    private int raceID;

    /**
     * The horse's index/position on the race.
     */
    private int raceIdx;

    /**
     * The current position of the horse in the race.
     */
    private int currentPosition;

    /**
     * The current step/iteration of the horse in the race.
     */
    private int currentStep;

    /**
     * Current state of the broker lifecycle.
     */
    private BrokerState brokerState;

    /**
     *  Constructor to initiate variables.
     *
     *     @param com Communication channel.
     *     @param rtInt Instance of RacingTrackInterface to provide the service.
     */
    public RacingTrackAPS (ServerCom com, RacingTrackInterface rtInt) {
        if (com == null)
            throw new IllegalArgumentException("Invalid communication socket.");
        if (rtInt == null)
            throw new IllegalArgumentException("Invalid Paddock interface.");

        this.com = com;
        this.rtInt = rtInt;
    }

    /**
     *  Provide the service.
     */
    @Override
    public void run() {
        RacingTrackMessage inMessage = (RacingTrackMessage)com.readObject();
        RacingTrackMessage outMessage = rtInt.processAndReply(inMessage);
        com.writeObject(outMessage);
    }

    /**
     * Method that returns the current Broker state.
     * @return Current Broker state.
     */
    @Override
    public BrokerState getBrokerState() {
        return this.brokerState;
    }

    /**
     * Updates the current Broker state.
     * @param state The new Broker state.
     */
    @Override
    public void setBrokerState(BrokerState state) {
        this.brokerState = state;
    }

    /**
     * Method that returns the ID of the Horse/Jockey pair.
     * @return The ID of the Horse/Jockey pair.
     */
    @Override
    public int getID() {
        return this.id;
    }

    /**
     * Method that sets the ID of the Horse/Jockey pair.
     * @param id The new ID of the Horse/Jockey pair.
     */
    @Override
    public void setID(int id) {
        this.id = id;
    }

    /**
     * Method that returns the current Horse/Jockey pair state.
     * @return Current Horse/Jockey pair state.
     */
    @Override
    public HorseState getHorseState() {
        return horseState;
    }

    /**
     * Updates the current Horse/Jockey pair state.
     * @param state The new Horse/Jockey pair state.
     */
    @Override
    public void setHorseState(HorseState state) {
        if (state == null)
            throw new IllegalArgumentException("Invalid Horse state");
        this.horseState = state;
    }

    /**
     * Method that returns the race ID in which the pair will participate.
     * @return ID of the race in which the pair will participate.
     */
    @Override
    public int getRaceID() {
        return raceID;
    }

    /**
     * Sets the race ID in which the pair will participate.
     * @param raceID The race ID in which the horse will run.
     */
    @Override
    public void setRaceID(int raceID) {
        if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid Race ID");
        this.raceID = raceID;
    }

    /**
     * Method that returns the horse's index/position on the race.
     * @return Horse's index/position on the race.
     */
    @Override
    public int getRaceIdx() {
        return raceIdx;
    }

    /**
     * Sets the horse's index/position on the race.
     * @param raceIdx The horse's index/position on the race.
     */
    @Override
    public void setRaceIdx(int raceIdx) {
        if (raceIdx < 0 || raceIdx >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid Horse race index.");
        this.raceIdx = raceIdx;
    }

    /**
     * Method that returns the agility of each horse, which in practice corresponds
     * to the maximum step distance the horse can take in each iteration.
     * @return Agility/max step per iteration of the horse.
     */
    @Override
    public int getAgility() {
        return agility;
    }

    /**
     * Method that sets the agility of each horse, which in practice corresponds
     * to the maximum step distance the horse can take in each iteration.
     * @param agility The new value of agility/max step per iteration of the horse.
     */
    @Override
    public void setAgility(int agility) {
        this.agility = agility;
    }

    /**
     * Method that returns the horse's current position on the racing track, i.e.,
     * the current travelled distance.
     * @return Horse's current position.
     */
    @Override
    public int getCurrentPosition() {
        return this.currentPosition;
    }

    /**
     * Method that sets the current position of Horse/Jockey pair.
     * @param currentPosition The current position of the Horse/Jockey pair in
     *                        the racing track.
     */
    @Override
    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    /**
     * Method that returns the number of steps the horse has already taken.
     * @return The number of steps/iterations of the horse during the race.
     */
    @Override
    public int getCurrentStep() {
        return this.currentStep;
    }

    /**
     * Method that sets the number of steps the horse has already taken.
     * @param currentStep The number of steps/iterations that the horse has
     *                    already taken.
     */
    @Override
    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    /**
     * Method that updates the current position of the pair, i.e., increases the
     * position with step steps and increments the current step by one.
     * @param step The distance of the increment/step.
     */
    @Override
    public void updateCurrentPosition(int step) {
        this.currentPosition += step;
        this.currentStep++;
    }
}

package communication;

import entities.BrokerInt;
import entities.HorseInt;
import entities.SpectatorInt;
import main.EventVariables;
import messages.ControlCentreMessage;
import sharedRegions.ControlCentreInterface;
import states.BrokerState;
import states.HorseState;
import states.SpectatorState;

/**
 * This data type defines the server side thread that effectively provides the
 * service; the communication is based on changing messages with the client
 * via a TCP channel.
 */
public class ControlCentreAPS extends Thread
        implements BrokerInt, SpectatorInt, HorseInt {

    /**
     * Instance of a communication socket with the client.
     */
    private ServerCom com;

    /**
     * Service to provide to the client - Control Centre.
     */
    private ControlCentreInterface ccInt;

    /**
     * Current state of the broker lifecycle.
     */
    private BrokerState brokerState;

    /**
     * Current state of the Spectator lifecycle.
     */
    private SpectatorState spectatorState;

    /**
     * ID of the Spectator or Horse/Jockey pair.
     */
    private int id;

    /**
     * Current amount of money in the Spectator's wallet.
     */
    private int wallet;

    /**
     * The betting strategy the Spectator will perform.
     */
    private int strategy;

    /**
     * Id of the current race taking place.
     */
    private int raceNumber;

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
     *  Constructor to initiate variables.
     *
     *     @param com Communication channel.
     *     @param ccInt Instance of ControlCentreInterface to provide the service.
     */
    public ControlCentreAPS(ServerCom com, ControlCentreInterface ccInt) {
        if (com == null)
            throw new IllegalArgumentException("Invalid communication socket.");
        if (ccInt == null)
            throw new IllegalArgumentException("Invalid Control Centre interface.");

        this.com = com;
        this.ccInt = ccInt;
    }

    /**
     *  Provide the service.
     */
    @Override
    public void run() {
        ControlCentreMessage inMessage = (ControlCentreMessage)com.readObject();
        ControlCentreMessage outMessage = ccInt.processAndReply(inMessage);
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
     * Method that returns the current Spectator state.
     * @return Current Spectator state.
     */
    @Override
    public SpectatorState getSpectatorState() {
        return this.spectatorState;
    }

    /**
     * Updates the current Spectator state.
     * @param state The new Spectator state.
     */
    @Override
    public void setSpectatorState(SpectatorState state) {
        this.spectatorState = state;
    }

    /**
     * Method that returns the ID of the Spectator or Horse/Jockey pair.
     * @return The ID of the Spectator or Horse/Jockey pair.
     */
    @Override
    public int getID() {
        return this.id;
    }

    /**
     * Method that sets the ID of the Spectator or Horse/Jockey pair.
     * @param id The new ID of the Spectator or Horse/Jockey pair.
     */
    @Override
    public void setID(int id) {
        this.id = id;
    }

    /**
     * Method that returns the current amount of money in the Spectator's wallet.
     * @return The current amount of money in the Spectator's wallet.
     */
    @Override
    public int getWallet() {
        return wallet;
    }

    /**
     * Method that sets the value at the wallet of the Spectator.
     * @param wallet The new value at the wallet of the Spectator.
     */
    @Override
    public void setWallet(int wallet) {
        this.wallet = wallet;
    }

    /**
     * Method that returns the Spectator's betting strategy.
     * @return The Spectator's betting strategy.
     */
    @Override
    public int getStrategy() {
        return strategy;
    }

    /**
     * Method that sets the betting strategy of the Spectator.
     * @param strategy The strategy used by the Spectator.
     */
    @Override
    public void setStrategy(int strategy) {
        this.strategy = strategy;
    }

    /**
     * Performs a transaction in the Spectator's wallet, summing the argument
     * value to the wallet.
     * @param value The value that will be summed to the wallet (it may add or
     *              subtract depending on its signal)
     */
    @Override
    public void updateWallet(int value) {
        this.wallet += value;
    }

    /**
     * Method that returns the current race identifier.
     * @return The current race identifier.
     */
    @Override
    public int getRaceNumber() {
        return raceNumber;
    }

    /**
     * Method that sets the current race identifier.
     * @param raceNumber The current race identifier.
     */
    @Override
    public void setRaceNumber(int raceNumber) {
        this.raceNumber = raceNumber;
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

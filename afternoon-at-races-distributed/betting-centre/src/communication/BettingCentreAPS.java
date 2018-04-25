package communication;

import entities.BrokerInt;
import entities.SpectatorInt;
import messages.BettingCentreMessage;
import sharedRegions.BettingCentreInterface;
import states.BrokerState;
import states.SpectatorState;

/**
 * This data type defines the server side thread that effectively provides the
 * service; the communication is based on changing messages with the client
 * via a TCP channel.
 */
public class BettingCentreAPS extends Thread
        implements BrokerInt, SpectatorInt {

    /**
     * Instance of a communication socket with the client.
     */
    private ServerCom com;

    /**
     * Service to provide to the client - Betting Centre.
     */
    private BettingCentreInterface bcInt;

    /**
     * Current state of the broker lifecycle.
     */
    private BrokerState brokerState;

    /**
     * Current state of the Spectator lifecycle.
     */
    private SpectatorState spectatorState;

    /**
     * ID of the Spectator.
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
     *  Constructor to initiate variables.
     *
     *     @param com Communication channel.
     *     @param bcInt Instance of BettingCentreInterface to provide the service.
     */
    public BettingCentreAPS (ServerCom com, BettingCentreInterface bcInt) {
        if (com == null)
            throw new IllegalArgumentException("Invalid communication socket.");
        if (bcInt == null)
            throw new IllegalArgumentException("Invalid Betting Centre interface.");

        this.com = com;
        this.bcInt = bcInt;
    }

    /**
     *  Provide the service.
     */
    @Override
    public void run() {
        BettingCentreMessage inMessage = (BettingCentreMessage)com.readObject();
        BettingCentreMessage outMessage = bcInt.processAndReply(inMessage);
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
     * Method that returns the ID of the Spectator.
     * @return The ID of the Spectator.
     */
    @Override
    public int getID() {
        return this.id;
    }

    /**
     * Method that sets the ID of the Spectator.
     * @param id The new ID of the Spectator.
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
}

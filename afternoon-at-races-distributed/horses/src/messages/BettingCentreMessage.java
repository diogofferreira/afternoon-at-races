package messages;

import messageTypes.BettingCentreMessageTypes;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This data type defines a message sent to and from the Betting Centre server.
 * It contains a variety of fields and constructors to adapt to the different
 * entities using it.
 */
public class BettingCentreMessage implements Serializable {

    /**
     * Serial version of the message.
     */
    private static final long serialVersionUID = 1002L;

    // method type
    /**
     * Identifier of the method that the entity pretends to invoke in the shared
     * region server.
     */
    private int method;

    // implicit info
    /**
     * Value currently in a Spectator's wallet.
     */
    private int wallet;

    /**
     * Betting strategy used by a Spectator.
     */
    private int strategy;

    // arguments
    /**
     * Id of the race.
     */
    private int raceId;

    /**
     * Array that contains the indexes of the horses that won a certain race.
     */
    private int[] winners;

    // replies
    /**
     * Boolean value that shows if there any winning bets (true if there are,
     * false otherwise).
     */
    private boolean areThereAnyWinners;

    /**
     * Value won by Spectator with a winning bet.
     */
    private double winningValue;

    /**
     * Horse index that a Spectator betted on.
     */
    private int bettedHorse;

    // entity id
    /**
     * Identifier of the entity that is sending the message.
     */
    private int entityId;

    /**
     * Constructor (for error messages).
     * @param error Type of the message (in this case an error message).
     */
    public BettingCentreMessage(BettingCentreMessageTypes error) {
        if (error != BettingCentreMessageTypes.ERROR)
            throw new IllegalArgumentException("Not an error message!");
        this.method = error.getId();
    }

    /**
     * Constructor (type 1).
     * @param method Method an entity invokes on the shared region server.
     * @param entityId Id of the entity sending the message.
     */
    public BettingCentreMessage(BettingCentreMessageTypes method,
                                int entityId) {
        this.method = method.getId();
        this.entityId = entityId;
    }

    /**
     * Constructor (type 2).
     * @param method Method an entity invokes on the shared region server.
     * @param raceId Identifier of a race.
     * @param entityId Id of the entity sending the message.
     */
    public BettingCentreMessage(BettingCentreMessageTypes method,
                                int raceId, int entityId) {
        this.method = method.getId();
        this.raceId = raceId;
        this.entityId = entityId;
    }

    /**
     * Constructor (type 3).
     * @param method Method an entity invokes on the shared region server.
     * @param winners Array that contains the indexes of the horses
     *                that won a certain race.
     * @param entityId Id of the entity sending the message.
     */
    public BettingCentreMessage(BettingCentreMessageTypes method,
                                int[] winners, int entityId) {
        this.method = method.getId();
        this.winners = winners;
        this.entityId = entityId;
    }

    /**
     * Constructor (type 4).
     * @param method Method an entity invokes on the shared region server.
     * @param areThereAnyWinners Boolean value that shows if there any winning
     *                           bets (true if there are, false otherwise).
     * @param entityId Id of the entity sending the message.
     */
    public BettingCentreMessage(BettingCentreMessageTypes method,
                                boolean areThereAnyWinners, int entityId) {
        this.method = method.getId();
        this.areThereAnyWinners = areThereAnyWinners;
        this.entityId = entityId;
    }

    /**
     * Constructor (type 5).
     * @param method Method an entity invokes on the shared region server.
     * @param winningValue Value won by Spectator with a winning bet.
     * @param entityId Id of the entity sending the message.
     */
    public BettingCentreMessage(BettingCentreMessageTypes method,
                                double winningValue, int entityId) {
        this.method = method.getId();
        this.winningValue = winningValue;
        this.entityId = entityId;
    }

    /**
     * Constructor (type 6).
     * @param method Method an entity invokes on the shared region server.
     * @param response Field indicating if it is a server response or a user
     *                 request (true in case of a server response, false
     *                 otherwise).
     * @param wallet Value currently in a Spectator's wallet.
     * @param strategyOrBettedHorse Field that represents either the betting
     *                              strategy used by the Spectator (in case of
     *                              a request to the server) or the horse he
     *                              bet on (in case of server response).
     * @param entityId Id of the entity sending the message.
     */
    public BettingCentreMessage(BettingCentreMessageTypes method, boolean response,
                                int wallet, int strategyOrBettedHorse, int entityId) {
        this.method = method.getId();
        this.wallet = wallet;
        if (response)
            this.bettedHorse = strategyOrBettedHorse;
        else
            this.strategy = strategyOrBettedHorse;
        this.entityId = entityId;
    }

    /**
     * Method that returns an integer identifier of the method invoked on the
     * shared region.
     * @return The identifier of the method invoked on the shared region.
     */
    public int getMethod() {
        return method;
    }

    /**
     * Method that returns the race identifier which the current
     * message corresponds.
     * @return The race identifier which the current message corresponds.
     */
    public int getRaceId() {
        return raceId;
    }

    /**
     * Method that returns the value in a Spectator's wallet.
     * @return The value in a Spectator's wallet.
     */
    public int getWallet() {
        return wallet;
    }

    /**
     * Method that returns a Spectator's betting strategy.
     * @return The Spectator's betting strategy.
     */
    public int getStrategy() {
        return strategy;
    }

    /**
     * Method that returns the horses indexes that won a certain race.
     * @return The horses indexes that won a certain race.
     */
    public int[] getWinners() {
        return winners;
    }

    /**
     * Method that returns true if there are any winning bets.
     * @return True if there are any winning bets, false otherwise.
     */
    public boolean isAreThereAnyWinners() {
        return areThereAnyWinners;
    }

    /**
     * Method that returns the value that a Spectator with a winning bet won.
     * @return The value that a Spectator with a winning bet won.
     */
    public double getWinningValue() {
        return winningValue;
    }

    /**
     * Method that returns the horse index that a Spectator bet on.
     * @return The horse index that a Spectator bet on.
     */
    public int getBettedHorse() {
        return bettedHorse;
    }

    /**
     * Method that returns the identifier of the entity that intends to invoke
     * a method in the shared region.
     * @return The identifier of the entity that intends to invoke a method
     * in the shared region.
     */
    public int getEntityId() {
        return entityId;
    }

    /**
     * Method that returns a textual representation of the message.
     * @return A textual representation of the message.
     */
    @Override
    public String toString() {
        return "BettingCentreMessage{" +
                "method=" + method +
                ", wallet=" + wallet +
                ", strategy=" + strategy +
                ", raceId=" + raceId +
                ", winners=" + Arrays.toString(winners) +
                ", areThereAnyWinners=" + areThereAnyWinners +
                ", winningValue=" + winningValue +
                ", bettedHorse=" + bettedHorse +
                ", entityId=" + entityId +
                '}';
    }
}

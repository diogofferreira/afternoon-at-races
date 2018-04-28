package messageTypes;

/**
 * Message types exchanged between clients and the Betting Centre server.
 */
public enum BettingCentreMessageTypes {

    /**
     * Corresponds to the method invoked by the Broker to let the Spectators
     * know that he is accepting bets.
     */
    ACCEPT_THE_BETS(0),

    /**
     * Corresponds to the method invoked by the Spectators to place a bet.
     */
    PLACE_A_BET(1),

    /**
     * Corresponds to the method invoked by the Broker to check if there are
     * any winning bets.
     */
    ARE_THERE_ANY_WINNERS(2),

    /**
     * Corresponds to the method invoked by the Broker to pay the winning bets.
     */
    HONOUR_THE_BETS(3),

    /**
     * Corresponds to the method invoked by the Spectators with winning bets to
     * collect their gains.
     */
    GO_COLLECT_THE_GAINS(4),

    /**
     * Message type that is sent in case of an error occurrence.
     */
    ERROR(5);

    /**
     * ID of the message type;
     */
    private final int id;

    /**
     * Constructor to add a new message type.
     * @param id ID of the message type.
     */
    private BettingCentreMessageTypes(int id) {
        this.id = id;
    }

    /**
     * Method that returns an integer id of the message type.
     * @return Integer representation of the message type;
     */
    public int getId() {
        return this.id;
    }

    /**
     * Method that returns an enum given its id.
     * @param id The id correspondent to the enum type.
     * @return The correspondent enum.
     */
    public static BettingCentreMessageTypes getType(int id) {
        switch (id) {
            case 0: return ACCEPT_THE_BETS;
            case 1: return PLACE_A_BET;
            case 2: return ARE_THERE_ANY_WINNERS;
            case 3: return HONOUR_THE_BETS;
            case 4: return GO_COLLECT_THE_GAINS;
            case 5: return ERROR;
            default: return null;
        }
    }
}

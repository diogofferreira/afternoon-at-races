package messageTypes;

public enum BettingCentreMessageTypes {

    ACCEPT_THE_BETS(0),

    PLACE_A_BET(1),

    ARE_THERE_ANY_WINNERS(2),

    HONOUR_THE_BETS(3),

    GO_COLLECT_THE_GAINS(4);

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
}

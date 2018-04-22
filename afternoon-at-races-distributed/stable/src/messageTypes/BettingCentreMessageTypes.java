package messageTypes;

public enum BettingCentreMessageTypes {

    ACCEPT_THE_BETS(0),

    PLACE_A_BET(1),

    ARE_THERE_ANY_WINNERS(2),

    HONOUR_THE_BETS(3),

    GO_COLLECT_THE_GAINS(4),

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

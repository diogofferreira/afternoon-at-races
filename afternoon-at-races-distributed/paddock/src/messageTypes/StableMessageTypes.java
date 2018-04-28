package messageTypes;

/**
 * Message types exchanged between clients and the Stable server.
 */
public enum StableMessageTypes {

    /**
     * Corresponds to the method invoked by the servers to check the Horses' odds.
     */
    GET_RACE_ODDS(0),

    /**
     * Corresponds to the method invoked by the Broker to summon the Horses to
     * the Paddock.
     */
    SUMMON_HORSES_TO_PADDOCK(1),

    /**
     * Corresponds to the method invoked by the Horses to proceed to the stable
     */
    PROCEED_TO_STABLE(2),

    /**
     * Corresponds to the method invoked by the Broker to play host at the bar.
     */
    ENTERTAIN_THE_GUESTS(3),

    /**
     * Message type that is sent in case of an error occurrence.
     */
    ERROR(4);

    /**
     * ID of the message type;
     */
    private final int id;

    /**
     * Constructor to add a new message type.
     * @param id ID of the message type.
     */
    private StableMessageTypes(int id) {
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
    public static StableMessageTypes getType(int id) {
        switch (id) {
            case 0: return GET_RACE_ODDS;
            case 1: return SUMMON_HORSES_TO_PADDOCK;
            case 2: return PROCEED_TO_STABLE;
            case 3: return ENTERTAIN_THE_GUESTS;
            case 4: return ERROR;
            default: return null;
        }
    }
}

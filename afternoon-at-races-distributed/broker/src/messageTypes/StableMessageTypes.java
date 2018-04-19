package messageTypes;

public enum StableMessageTypes {

    GET_RACE_ODDS(0),

    SUMMON_HORSES_TO_PADDOCK(1),

    PROCEED_TO_STABLE(2),

    ENTERTAIN_THE_GUESTS(3);

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
}

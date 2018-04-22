package messageTypes;

public enum RacingTrackMessageTypes {

    PROCEED_TO_START_LINE(0),

    START_THE_RACE(1),

    MAKE_A_MOVE(2),

    HAS_FINISH_LINE_BEEN_CROSSED(3),

    ERROR(4);

    /**
     * ID of the message type;
     */
    private final int id;

    /**
     * Constructor to add a new message type.
     * @param id ID of the message type.
     */
    private RacingTrackMessageTypes(int id) {
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
    public static RacingTrackMessageTypes getType(int id) {
        switch (id) {
            case 0: return PROCEED_TO_START_LINE;
            case 1: return START_THE_RACE;
            case 2: return MAKE_A_MOVE;
            case 3: return HAS_FINISH_LINE_BEEN_CROSSED;
            case 4: return ERROR;
            default: return null;
        }
    }
}

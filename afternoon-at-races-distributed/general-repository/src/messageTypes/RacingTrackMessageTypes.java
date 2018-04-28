package messageTypes;

/**
 * Message types exchanged between clients and the Racing Track server.
 */
public enum RacingTrackMessageTypes {

    /**
     * Corresponds to the method invoked by the Horses to proceed to the start
     * line.
     */
    PROCEED_TO_START_LINE(0),

    /**
     * Corresponds to the method invoked by the Broker to start a new race.
     */
    START_THE_RACE(1),

    /**
     * Corresponds to the method invoked by the Horses to make a move in the race.
     */
    MAKE_A_MOVE(2),

    /**
     * Corresponds to the method invoked by the Horses to check if the finish
     * line has been crossed.
     */
    HAS_FINISH_LINE_BEEN_CROSSED(3),

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

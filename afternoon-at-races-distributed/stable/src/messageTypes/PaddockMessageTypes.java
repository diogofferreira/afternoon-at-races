package messageTypes;

/**
 * Message types exchanged between clients and the Paddock server.
 */
public enum PaddockMessageTypes {

    /**
     * Corresponds to the method invoked by the Horses that reach the Paddock.
     */
    PROCEED_TO_PADDOCK(0),

    /**
     * Corresponds to the method invoked by the Spectators to appraise the horses.
     */
    GO_CHECK_HORSES(1),

    /**
     * Message type that is sent in case of an error occurrence.
     */
    ERROR(2);

    /**
     * ID of the message type;
     */
    private final int id;

    /**
     * Constructor to add a new message type.
     * @param id ID of the message type.
     */
    private PaddockMessageTypes(int id) {
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
    public static PaddockMessageTypes getType(int id) {
        switch (id) {
            case 0: return PROCEED_TO_PADDOCK;
            case 1: return GO_CHECK_HORSES;
            case 2: return ERROR;
            default: return null;
        }
    }
}

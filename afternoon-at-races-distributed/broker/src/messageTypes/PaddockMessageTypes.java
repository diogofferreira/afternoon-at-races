package messageTypes;

public enum PaddockMessageTypes {

    PROCEED_TO_PADDOCK(0),

    GO_CHECK_HORSES(1);

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
}

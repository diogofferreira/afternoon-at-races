package messageTypes;

public enum ControlCentreMessageTypes {

    SUMMON_HORSES_TO_PADDOCK(0),

    WAIT_FOR_NEXT_RACE(1),

    PROCEED_TO_PADDOCK(2),

    GO_CHECK_HORSES(3),

    GO_WATCH_THE_RACE(4),

    START_THE_RACE(5),

    FINISH_THE_RACE(6),

    REPORT_RESULTS(7),

    HAVE_I_WON(8),

    CELEBRATE(9),

    RELAX_A_BIT(10),

    ERROR(11);

    /**
     * ID of the message type;
     */
    private final int id;

    /**
     * Constructor to add a new message type.
     * @param id ID of the message type.
     */
    private ControlCentreMessageTypes(int id) {
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

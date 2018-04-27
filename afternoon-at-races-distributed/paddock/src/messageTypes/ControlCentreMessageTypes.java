package messageTypes;

public enum ControlCentreMessageTypes {

    OPEN_THE_EVENT(0),

    SUMMON_HORSES_TO_PADDOCK(1),

    WAIT_FOR_NEXT_RACE(2),

    PROCEED_TO_PADDOCK(3),

    GO_CHECK_HORSES(4),

    GO_WATCH_THE_RACE(5),

    START_THE_RACE(6),

    FINISH_THE_RACE(7),

    REPORT_RESULTS(8),

    HAVE_I_WON(9),

    CELEBRATE(10),

    RELAX_A_BIT(11),

    ERROR(12);

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

    /**
     * Method that returns an enum given its id.
     * @param id The id correspondent to the enum type.
     * @return The correspondent enum.
     */
    public static ControlCentreMessageTypes getType(int id) {
        switch (id) {
            case 0: return OPEN_THE_EVENT;
            case 1: return SUMMON_HORSES_TO_PADDOCK;
            case 2: return WAIT_FOR_NEXT_RACE;
            case 3: return PROCEED_TO_PADDOCK;
            case 4: return GO_CHECK_HORSES;
            case 5: return GO_WATCH_THE_RACE;
            case 6: return START_THE_RACE;
            case 7: return FINISH_THE_RACE;
            case 8: return REPORT_RESULTS;
            case 9: return HAVE_I_WON;
            case 10: return CELEBRATE;
            case 11: return RELAX_A_BIT;
            default: return null;
        }
    }
}

package messageTypes;

/**
 * Message types exchanged between clients and the Control Centre server.
 */
public enum ControlCentreMessageTypes {

    /**
     * Corresponds to the method invoked by the Broker to start the event.
     */
    OPEN_THE_EVENT(0),

    /**
     * Corresponds to the method invoked by the Broker to call the horses that
     * will participate in a given race to proceed to the Paddock.
     */
    SUMMON_HORSES_TO_PADDOCK(1),

    /**
     * Corresponds to the method invoked by the Spectators while waiting for the
     * Broker to announce the next race.
     */
    WAIT_FOR_NEXT_RACE(2),

    /**
     * Corresponds to the method invoked by the last Horse to reach the Paddock
     * to allow the Spectators proceed to the Paddock.
     */
    PROCEED_TO_PADDOCK(3),

    /**
     * Corresponds to the method invoked by the Spectators to proceed to the
     * Paddock to check the horses that will be running.
     */
    GO_CHECK_HORSES(4),

    /**
     * Corresponds to the method invoked by the Spectators before the start of
     * the race.
     */
    GO_WATCH_THE_RACE(5),

    /**
     * Corresponds to the method invoked by the Broker to start the race.
     */
    START_THE_RACE(6),

    /**
     * Corresponds to the method invoked by the last Horse/Jockey pair to reach
     * the finish line to let the Broker know that the race has ended.
     */
    FINISH_THE_RACE(7),

    /**
     * Corresponds to the method invoked by the Broker to report the standings
     * of the race.
     */
    REPORT_RESULTS(8),

    /**
     * Corresponds to the method invoked by each Spectator to check if he/her
     * has a winning bet.
     */
    HAVE_I_WON(9),

    /**
     * Corresponds to the method invoked by the Broker to signal the Spectators
     * that the event has ended.
     */
    CELEBRATE(10),

    /**
     * Corresponds to the method invoked by the Spectators to go to the bar after
     * the end of the event.
     */
    RELAX_A_BIT(11),

    /**
     * Message type that is sent in case of an error occurrence.
     */
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

package states;

/**
 * Definition of the Broker states.
 */
public enum BrokerState {
    /**
     * Broker is opening the event.
     */
    OPENING_THE_EVENT("OTE", 0),

    /**
     * Broker announcing next race.
     */
    ANNOUNCING_NEXT_RACE("ANR", 1),

    /**
     * Broker is waiting for spectators' bets.
     */
    WAITING_FOR_BETS("WFB", 2),

    /**
     * Broker supervising the race.
     */
    SUPERVISING_THE_RACE("STR", 3),

    /**
     * Broker paying to the spectators who bet on a winning horse.
     */
    SETTLING_ACCOUNTS("SA", 4),

    /**
     * After the event, the broker is playing host at the bar.
     */
    PLAYING_HOST_AT_THE_BAR("PHB", 5);

    /**
     * Textual representation of the states.
     */
    private final String logRepresentation;

    /**
     * ID of the state;
     */
    private final int id;

    /**
     * Constructor to add a new state.
     * @param logRepresentation Textual representation of the state.
     */
    private BrokerState(String logRepresentation, int id) {
        this.logRepresentation = logRepresentation;
        this.id = id;
    }

    /**
     * Method that returns an integer id of the state.
     * @return Integer representation of the id;
     */
    public int getId() {
        return this.id;
    }

    /**
     * Method that returns a String representation of a state.
     * @return String representation of a state.
     */
    @Override
    public String toString() {
        return logRepresentation;
    }
}
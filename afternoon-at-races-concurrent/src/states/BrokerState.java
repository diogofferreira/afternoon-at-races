package states;

/**
 * Definition of the Broker states.
 */
public enum BrokerState {
    /**
     * Broker is opening the event.
     */
    OPENING_THE_EVENT("OTE"),

    /**
     * Broker announcing next race.
     */
    ANNOUNCING_NEXT_RACE("ANR"),

    /**
     * Broker is waiting for spectators' bets.
     */
    WAITING_FOR_BETS("WFB"),

    /**
     * Broker supervising the race.
     */
    SUPERVISING_THE_RACE("STR"),

    /**
     * Broker paying to the spectators who bet on a winning horse.
     */
    SETTLING_ACCOUNTS("SA"),

    /**
     * After the event, the broker is playing host at the bar.
     */
    PLAYING_HOST_AT_THE_BAR("PHB");

    /**
     * Textual representation of the states.
     */
    private final String logRepresentation;

    /**
     * Constructor to add a new state.
     * @param logRepresentation Textual representation of the state.
     */
    private BrokerState(String logRepresentation) {
        this.logRepresentation = logRepresentation;
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
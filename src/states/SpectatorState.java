package states;

/**
 * Definition of the Spectator states.
 */
public enum SpectatorState {
    /**
     * Spectator waiting for a race to start.
     */
    WAITING_FOR_A_RACE_TO_START("WRS"),

    /**
     * Spectator appraising the horses.
     */
    APPRAISING_THE_HORSES("ATH"),

    /**
     * Spectator placing a bet.
     */
    PLACING_A_BET("PAB"),

    /**
     * Spectator watching a race.
     */
    WATCHING_A_RACE("WAR"),

    /**
     * After the outcome of the race, if spectator bet on a winning horse, collects his/her gains.
     */
    COLLECTING_THE_GAINS("CTG"),

    /**
     * Spectator is celebrating at the bar after the event.
     */
    CELEBRATING("CEL");

    /**
     * Textual representation of the states.
     */
    private final String logRepresentation;

    /**
     * Constructor to add a new state.
     * @param logRepresentation Textual representation of the state.
     */
    private SpectatorState(String logRepresentation) {
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
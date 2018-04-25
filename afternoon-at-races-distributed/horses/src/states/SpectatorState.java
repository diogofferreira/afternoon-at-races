package states;

/**
 * Definition of the Spectator states.
 */
public enum SpectatorState {
    /**
     * Spectator waiting for a race to start.
     */
    WAITING_FOR_A_RACE_TO_START("WRS", 0),

    /**
     * Spectator appraising the horses.
     */
    APPRAISING_THE_HORSES("ATH", 1),

    /**
     * Spectator placing a bet.
     */
    PLACING_A_BET("PAB", 2),

    /**
     * Spectator watching a race.
     */
    WATCHING_A_RACE("WAR", 3),

    /**
     * After the outcome of the race, if spectator bet on a winning horse, collects his/her gains.
     */
    COLLECTING_THE_GAINS("CTG", 4),

    /**
     * Spectator is celebrating at the bar after the event.
     */
    CELEBRATING("CEL", 5);

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
    private SpectatorState(String logRepresentation, int id) {
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

    /**
     * Method that returns an enum given its id.
     * @param id The id correspondent to the enum type.
     * @return The correspondent enum.
     */
    public static SpectatorState getType(int id) {
        switch (id) {
            case 0: return WAITING_FOR_A_RACE_TO_START;
            case 1: return APPRAISING_THE_HORSES;
            case 2: return PLACING_A_BET;
            case 3: return WATCHING_A_RACE;
            case 4: return COLLECTING_THE_GAINS;
            case 5: return CELEBRATING;
            default: return null;
        }
    }
}
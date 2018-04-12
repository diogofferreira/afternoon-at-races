package states;

/**
 * Definition of the Horse/Jockey pair states.
 */
public enum HorseState {
    /**
     * Horse/Jockey pair is at the stable.
     */
    AT_THE_STABLE("ATS", 0),

    /**
     * Horse/Jockey pair is at the paddock, being appraised by the spectators.
     */
    AT_THE_PADDOCK("ATP", 1),

    /**
     * Horse/Jockey pair is ready to race, at the starting line.
     */
    AT_THE_STARTING_LINE("ASL", 2),

    /**
     * Horse/Jockey pair is running a race.
     */
    RUNNING("RUN", 3),

    /**
     * Horse/Jockey pair crossed the finish line.
     */
    AT_THE_FINISH_LINE("AFL", 4);

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
    private HorseState(String logRepresentation, int id) {
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
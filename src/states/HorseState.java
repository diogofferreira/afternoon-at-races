package states;

/**
 * Definition of the Horse/Jockey pair states.
 */
public enum HorseState {
    /**
     * Horse/Jockey pair is at the stable.
     */
    AT_THE_STABLE("ATS"),

    /**
     * Horse/Jockey pair is at the paddock, being appraised by the spectators.
     */
    AT_THE_PADDOCK("ATP"),

    /**
     * Horse/Jockey pair is ready to race, at the starting line.
     */
    AT_THE_START_LINE("ASL"),

    /**
     * Horse/Jockey pair is running a race.
     */
    RUNNING("RUN"),

    /**
     * Horse/Jockey pair crossed the finish line.
     */
    AT_THE_FINISH_LINE("AFL");

    /**
     * Textual representation of the states.
     */
    private final String logRepresentation;

    /**
     * Constructor to add a new state.
     * @param logRepresentation Textual representation of the state.
     */
    private HorseState(String logRepresentation) {
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
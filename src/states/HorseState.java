package states;

public enum HorseState implements State {
    AT_THE_STABLE("ATS"),
    AT_THE_PADDOCK("ATP"),
    AT_THE_START_LINE("ATSL"),
    RUNNING("RUN"),
    AT_THE_FINISH_LINE("ATFL");

    private final String logRepresentation;

    private HorseState(String logRepresentation) {
        this.logRepresentation = logRepresentation;
    }

    @Override
    public String toString() {
        return logRepresentation;
    }
}
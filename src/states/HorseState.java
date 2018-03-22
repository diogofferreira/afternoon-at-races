package states;

public enum HorseState implements State {
    AT_THE_STABLE("ATS"),
    AT_THE_PADDOCK("ATP"),
    AT_THE_START_LINE("ASL"),
    RUNNING("RUN"),
    AT_THE_FINISH_LINE("AFL");

    private final String logRepresentation;

    private HorseState(String logRepresentation) {
        this.logRepresentation = logRepresentation;
    }

    @Override
    public String toString() {
        return logRepresentation;
    }
}
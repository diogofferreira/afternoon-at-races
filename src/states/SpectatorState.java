package states;

public enum SpectatorState implements State {
    WAITING_FOR_A_RACE_TO_START("WRS"),
    APPRAISING_THE_HORSES("ATH"),
    PLACING_A_BET("PAB"),
    WATCHING_A_RACE("WAR"),
    COLLECT_THE_GAINS("CTG"),
    CELEBRATING("CEL");

    private final String logRepresentation;

    private SpectatorState(String logRepresentation) {
        this.logRepresentation = logRepresentation;
    }

    @Override
    public String toString() {
        return logRepresentation;
    }
}
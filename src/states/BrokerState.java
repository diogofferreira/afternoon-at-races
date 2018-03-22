package states;

public enum BrokerState implements State {
    OPENING_THE_EVENT("OTE"),
    ANNOUNCING_NEXT_RACE("ANR"),
    WAITING_FOR_BETS("WFB"),
    SUPERVISING_THE_RACE("STR"),
    SETTLING_ACCOUNTS("SA"),
    PLAYING_HOST_AT_THE_BAR("PHB");

    private final String logRepresentation;

    private BrokerState(String logRepresentation) {
        this.logRepresentation = logRepresentation;
    }

    @Override
    public String toString() {
        return logRepresentation;
    }
}
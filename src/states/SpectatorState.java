package states;

public enum SpectatorState implements State {
    WAITING_FOR_A_RACE_TO_START, APPRAISING_THE_HORSES, PLACING_A_BET,
    WATCHING_A_RACE, COLLECT_THE_GAINS, CELEBRATING;
}

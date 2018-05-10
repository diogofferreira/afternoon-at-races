package utils;

/**
 * Definition of the state of a bet.
 */
public enum BetState {
    /**
     * Bet still wasn't verified by the broker.
     */
    PENDING,

    /**
     * The bet was accepted.
     */
    ACCEPTED,

    /**
     * The bet was rejected.
     */
    REJECTED
}

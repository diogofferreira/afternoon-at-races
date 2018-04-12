package utils;

import main.EventVariables;

public class Bet {
    /**
     * Representation of a bet performed by a spectator over a horse.
     */

    /**
     * Id of the spectator.
     */
    private int spectatorID;

    /**
     * Index of the horse in the starting line.
     */
    private int horseIdx;

    /**
     * Value of the bet.
     */
    private int value;

    /**
     * Field indicating if the bet was accepted, rejected or it's still pending.
     */
    private BetState state;

    /**
     * Creates a new instance of a Bet.
     * @param spectatorID Id of the spectator.
     * @param horseIdx Index on the race of the horse the spectator bets on.
     * @param value Value of the bet.
     */
    public Bet(int spectatorID, int horseIdx, int value) {
        if (spectatorID < 0 || spectatorID >= EventVariables.NUMBER_OF_SPECTATORS)
            throw new IllegalArgumentException("Invalid spectator id");
        if (horseIdx < 0 || horseIdx >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid horse index");
        if (value <= 0)
            throw new IllegalArgumentException("Bet must be a positive value");

        this.spectatorID = spectatorID;
        this.horseIdx = horseIdx;
        this.value = value;
        this.state = BetState.PENDING;
    }

    /**
     * Method to get the ID of the spectator who made the bet.
     * @return Id of the spectator.
     */
    public int getSpectatorID() {
        return spectatorID;
    }

    /**
     * Method to get the index in the race of the horse that the spectator bet on.
     * @return Index on the race of the horse the spectator bets on.
     */
    public int getHorseIdx() {
        return horseIdx;
    }

    /**
     * Method to get the value of the bet.
     * @return Value of the bet.
     */
    public int getValue() {
        return value;
    }

    /**
     * Method that returns the current state of the bet.
     * @return the state of the bet.
     */
    public BetState getState() {
        return state;
    }

    /**
     * Updates the current bet state.
     * @param state New state of the bet.
     */
    public void setState(BetState state) {
        if (state == null)
            throw new IllegalArgumentException("Invalid bet state");

        this.state = state;
    }

    /**
     * Method that returns a String representation of the bet.
     * @return String representation of the bet.
     */
    @Override
    public String toString() {
        return "Bet{" +
                "spectatorID=" + spectatorID +
                ", horseIdx=" + horseIdx +
                ", value=" + value +
                ", state=" + state +
                '}';
    }
}

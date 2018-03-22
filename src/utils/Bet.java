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
     *
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
     *
     * @return Id of the spectator.
     */
    public int getSpectatorID() {
        return spectatorID;
    }

    /**
     *
     * @return Index on the race of the horse the spectator bets on.
     */
    public int getHorseIdx() {
        return horseIdx;
    }

    /**
     *
     * @return Value of the bet.
     */
    public int getValue() {
        return value;
    }

    /**
     *
     * @return the state of the bet.
     */
    public BetState getState() {
        return state;
    }

    /**
     * Sets the bet state.
     */
    public void setState(BetState state) {
        if (state == null)
            throw new IllegalArgumentException("Invalid bet state");

        this.state = state;
    }

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

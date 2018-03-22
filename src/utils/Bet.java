package utils;


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
    private int horseID;

    /**
     * Value of the bet.
     */
    private int value;

    /**
     *
     * @param spectatorID Id of the spectator.
     * @param horseID
     * @param value
     */
    public Bet(int spectatorID, int horseID, int value) {
        assert spectatorID >= 0 && horseID >= 0 && value >= 0;
        this.spectatorID = spectatorID;
        this.horseID = horseID;
        this.value = value;
    }

    public int getSpectatorID() {
        return spectatorID;
    }

    public int getHorseID() {
        return horseID;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Bet{" +
                "spectatorID=" + spectatorID +
                ", horseID=" + horseID +
                ", value=" + value +
                '}';
    }
}

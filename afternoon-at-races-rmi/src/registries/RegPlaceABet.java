package registries;

import java.io.Serializable;

/**
 * Data type that implements the return values of the placeABet method.
 */
public class RegPlaceABet implements Serializable {
    /**
     * Index on the race of the horse the Spectator bet on.
     */
    private int horseIdx;

    /**
     * Amount in the Spectator's wallet after the bet was placed.
     */
    private int wallet;

    /**
     * Creates a new instance of RegPlaceABet.
     * @param horseIdx Index on the race of the horse the Spectator bet on.
     * @param wallet Amount in the Spectator's wallet after the bet was placed.
     */
    public RegPlaceABet(int horseIdx, int wallet) {
        this.horseIdx = horseIdx;
        this.wallet = wallet;
    }

    /**
     * Method that returns the index on the race of the horse
     * the Spectator bet on.
     * @return Index on the race of the horse the Spectator bet on.
     */
    public int getHorseIdx() {
        return horseIdx;
    }

    /**
     * Method that returns the mount in the Spectator's wallet after
     * the bet was placed.
     * @return Amount in the Spectator's wallet after the bet was placed.
     */
    public int getWallet() {
        return wallet;
    }
}

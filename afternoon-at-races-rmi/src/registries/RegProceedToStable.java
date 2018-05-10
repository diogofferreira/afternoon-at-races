package registries;

import java.io.Serializable;

/**
 * Data type that implements the return values of the proceedToStable method.
 */
public class RegProceedToStable implements Serializable {
    /**
     * The race ID in which the horse will run.
     */
    private int raceId;

    /**
     * The horse's index/position on the race.
     */
    private int raceIdx;

    /**
     * Creates a new instance of RegPlaceABet.
     * @param raceId The race ID in which the horse will run.
     * @param raceIdx The horse's index/position on the race
     */
    public RegProceedToStable(int raceId, int raceIdx) {
        this.raceId = raceId;
        this.raceIdx = raceIdx;
    }

    /**
     * Method that returns the race ID in which the horse will run.
     * @return The race ID in which the horse will run..
     */
    public int getRaceId() {
        return raceId;
    }

    /**
     * Method that returns the horse's index/position on the race
     * @return The horse's index/position on the race
     */
    public int getRaceIdx() {
        return raceIdx;
    }
}

package sharedRegions;

public class BettingCentre {

    /* FIFO with pending bets */
    /* FIFO with accepted bets */

    /* FIFO with pending collections */
    /* FIFO with accepted collections */

    public void acceptBets(int raceID) {
        /* broker wait */
    }

    public void placeABet(Bet bet) {
        /* add to waiting FIFO */
        /* notify broker */
        /* spectator wait */
    }

    public void existPendingBets() {
        // returns true if there pending bets in FIFO
    }

    public void validateBet() {
        /* validate pending FIFO's head bet */
        /* notify spectator */
        if (!acceptedBets.isFull())
            /* broker wait */
    }

    public void areThereAnyWinners(int[] raceWinners) {
        /* save horses winners */
        /* creates FIFO for the number of winning bets */
        if (FIFO.length) // if there are winners
            /* broker wait */
            /* waits for the winners */
    }

    public void goCollectTheGains(int spectatorID) {
        /* add to pending collections FIFO */
        /* notify broker */
        /* spectator waits */
    }

    public void honorTheBets() {
        /* honor pending FIFO's head gains */
        /* notify spectator */
        if (!acceptedCollections.isFull())
            /* broker wait */
    }
}

package sharedRegions;

import utils.Bet;

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

    public boolean existPendingBets() {
        // returns true if there pending bets in FIFO
        return false;
    }

    public void validateBet() {
        /* validate pending FIFO's head bet */
        /* notify spectator */
        if (!acceptedBets.isFull())
            /* broker wait */
    }

    public boolean areThereAnyWinners() {
        /* save horses winners */
        /* creates FIFO for the number of winning bets */
        if (FIFO.length){} // if there are winners
            /* broker wait */
            /* waits for the winners */
        return false;
    }

    public void goCollectTheGains(int spectatorID) {
        /* add to pending collections FIFO */
        /* notify broker */
        /* spectator waits */
    }

    public void honourTheBets() {
        /* honor pending FIFO's head gains */
        /* notify spectator */
        if (!acceptedCollections.isFull())
            /* broker wait */
    }
}

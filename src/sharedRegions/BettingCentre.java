package sharedRegions;

public class BettingCentre {

    /* FIFO with waiting bets */
    /* FIFO with accepted bets */

    public void acceptBets(int raceId) {
        /* broker wait */
    }

    public void placeABet(int spectatorId, int betValue) {
        Bet bet = new Bet(spectatorId, betValue);
        /* add to waiting FIFO */
        /* notify broker */
        /* spectator wait */
    }

    public void validateBet() {
        /* validate waiting FIFO's head bet */
        /* notify spectator */
        if (!acceptedBets.isFull())
            /* broker wait */

    }

    public void reportResults() {
        /* save horse winners */
    }

    public void areThereAnyWinners() {
        /* save horse winners */
    }
}

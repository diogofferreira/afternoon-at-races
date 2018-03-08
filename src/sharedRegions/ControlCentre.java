package sharedRegions;

public class ControlCentre {

    public void summonHorsesToPaddock(int raceNumber) {
        /* broker wait */
    }

    public void waitForNextRace(int spectatorId) {
        /* spectators wait */
    }

    public void proceedToPaddock() {
        /* notify all spectators */
    }

    public void goCheckHorses(int raceNumber) {
        /* notify broker */
    }

    public void goWatchTheRace(int spectatorId) {
        /* spectator wait */
    }

    public void startTheRace() {
        /* broker wait */
    }

    public void reportResulst() {
        /* save horses winners */
        /* notify all spectators */
    }

    public void areThereAnyWinners() {
        /* notify all spectators */
    }

    public void haveIWon(int spectatorID) {
        /* checks if winner is the one he/she bet */
    }

    public void entertainTheGuests() {
        /* broker just playing host, end the afternoon */
    }

    public void relaxABit(int spectatorID) {
        /* just relax, end the afternoon */
    }

}

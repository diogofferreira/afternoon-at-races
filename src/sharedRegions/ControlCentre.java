package sharedRegions;

public class ControlCentre {

    private int raceNumber;

    public void summonHorsesToPaddock(int raceNumber) {
        this.raceNumber = raceNumber;
        /* broker wait */
    }

    public boolean waitForNextRace(int spectatorId) {
        /* spectators wait */
        return false;
    }

    public void proceedToPaddock() {
        /* notify all spectators */
    }

    public void goCheckHorses() {
        /* notify broker */
    }

    public void goWatchTheRace(int spectatorId) {
        /* spectator wait */
    }

    public void startTheRace() {
        /* broker wait */
    }

    public void reportResults() {
        /* save horses winners */
        /* notify all spectators */
    }

    public boolean areThereAnyWinners() {
        /* notify all spectators */
        return false;
    }

    public boolean haveIWon(int horseID) {
        /* checks if winner is the one he/she bet */
    }

    public void entertainTheGuests() {
        /* broker just playing host, end the afternoon */
    }

    public void relaxABit(int spectatorID) {
        /* just relax, end the afternoon */
    }

    public int getRaceNumber() {
        return this.raceNumber;
    }

}

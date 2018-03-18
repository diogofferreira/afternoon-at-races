package entities;

import main.EventVariables;
import sharedRegions.BettingCentre;
import sharedRegions.ControlCentre;
import sharedRegions.RacingTrack;
import sharedRegions.Stable;
import states.BrokerState;

public class Broker extends Thread {

    private states.State state;

    private Stable stable;
    private RacingTrack racingTrack;
    private ControlCentre controlCentre;
    private BettingCentre bettingCentre;

    private int[] winners;

    public Broker(Stable s, RacingTrack r, ControlCentre c, BettingCentre b) {
        if (s == null || r == null || c == null || b == null)
            throw new IllegalArgumentException("Invalid shared region reference.");

        this.state = BrokerState.OPENING_THE_EVENT;
        this.stable = s;
        this.controlCentre = c;
        this.bettingCentre = b;
        this.racingTrack = r;
    }

    public void run() {
        for (int i = 0; i < EventVariables.NUMBER_OF_RACES; i++) {
            // summonHorsesToPaddock()
            stable.summonHorsesToPaddock(i);
            controlCentre.summonHorsesToPaddock(i);

            // acceptsBets
            bettingCentre.acceptTheBets(i);

            // startTheRace
            racingTrack.startTheRace();
            controlCentre.startTheRace();

            // reportResults
            winners = controlCentre.reportResults();

            if (bettingCentre.areThereAnyWinners(winners))
                bettingCentre.honourTheBets();
        }

        controlCentre.entertainTheGuests();
        stable.entertainTheGuests();

    }

    public states.State getBrokerState() {
        return this.state;
    }

    public void setBrokerState(states.State state) {
        this.state = state;
    }
}

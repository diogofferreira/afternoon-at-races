package entities;

import main.EventVariables;
import sharedRegions.BettingCentre;
import sharedRegions.ControlCentre;
import sharedRegions.RacingTrack;
import sharedRegions.Stable;
import states.BrokerState;
import utils.State;

public class Broker {

    private State state;

    private Stable stable;
    private RacingTrack racingTrack;
    private ControlCentre controlCentre;
    private BettingCentre bettingCentre;

    public Broker(Stable stable, RacingTrack racingTrack,
                  ControlCentre controlCentre, BettingCentre bettingCentre) {
        this.state = BrokerState.OPENING_THE_EVENT;
        this.stable = stable;
        this.controlCentre = controlCentre;
        this.bettingCentre = bettingCentre;
        this.racingTrack = racingTrack;
    }

    public void run() {
        for (int i = 0; i < EventVariables.NUMBER_OF_RACES; i++) {

            // summonHorsesToPaddock()
            stable.summonHorsesToPaddock(i);
            controlCentre.summonHorsesToPaddock(i);

            // acceptsBets
            bettingCentre.acceptBets(i);
            while (bettingCentre.existPendingBets())
                bettingCentre.validateBet();

            // startTheRace
            racingTrack.startTheRace();
            controlCentre.startTheRace();

            // reportResults
            controlCentre.reportResults();

            if (bettingCentre.areThereAnyWinners())
                bettingCentre.honourTheBets();
        }

        controlCentre.entertainTheGuests();
    }
}

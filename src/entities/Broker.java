package entities;

import sharedRegions.BettingCentre;
import sharedRegions.ControlCentre;
import sharedRegions.RacingTrack;

public class Broker {

    private State state;

    public void run() {
        for (int i = 0; i < NUMBER_RACES; i++) {

            // summonHorsesToPaddock()
            Stable.summonHorsesToPaddock(i);
            ControlCentre.summonHorsesToPaddock(i);

            // acceptsBets
            BettingCentre.acceptBets();
            while (BettingCentre.existPendingBets())
                BettingCentre.validateBet();

            // startTheRace
            RacingTrack.startTheRace();
            ControlCentre.startTheRace();

            // reportResults
            ControlCentre.reportResults();

            if (BettingCentre.areThereAnyWinners())
                BettingCentre.honorBets();
        }

        ControlCentre.entertainTheGuests();
    }
}

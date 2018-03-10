package entities;

import sharedRegions.BettingCentre;
import sharedRegions.ControlCentre;
import sharedRegions.Paddock;


public class Spectator {

    private State state;
    private int id;
    private double wallet;

    private Bet getBet() {
        // return a bet, value and horse
    }

    public void run() {
        while(ControlCentre.waitForNextRace(this.id)) {
            // goCheckHorses
            ControlCentre.goCheckHorses();
            Paddock.goCheckHorses();

            BettingCentre.placeABet(getBet());

            ControlCentre.goWatchTheRace(self.id);

            if (ControlCentre.haveIWon(bettedHorse))
                BettingCentre.goCollectGains(self.id);
        }

        ControlCentre.relaxABit(this.id);
    }
}

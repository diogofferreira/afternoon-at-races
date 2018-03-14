package entities;

import sharedRegions.*;
import states.SpectatorState;
import states.State;
import utils.Bet;


public class Spectator {

    private State state;
    private int id;
    private double wallet;

    private int bettedHorse;

    private Paddock paddock;
    private ControlCentre controlCentre;
    private BettingCentre bettingCentre;

    public Spectator(int id, double wallet, Paddock paddock,
                     ControlCentre controlCentre, BettingCentre bettingCentre) {
        this.state = SpectatorState.WAITING_FOR_A_RACE_TO_START;
        this.id = id;
        this.wallet = wallet;
        this.bettedHorse = -1;
        this.paddock = paddock;
        this.controlCentre = controlCentre;
        this.bettingCentre = bettingCentre;
    }

    private Bet getBet() {
        // return a bet, value and horse
        return null;
    }

    public void run() {
        while(controlCentre.waitForNextRace(this.id)) {
            // goCheckHorses
            controlCentre.goCheckHorses();
            paddock.goCheckHorses(controlCentre.getRaceNumber());

            while(!bettingCentre.placeABet(getBet()));

            controlCentre.goWatchTheRace(this.id);

            if (controlCentre.haveIWon(this.bettedHorse))
                bettingCentre.goCollectTheGains(this.id);
        }

        controlCentre.relaxABit(this.id);
    }
}

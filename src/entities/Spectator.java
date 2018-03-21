package entities;

import sharedRegions.GeneralRepository;
import main.EventVariables;
import sharedRegions.*;
import states.SpectatorState;
import utils.Bet;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class Spectator extends Thread {

    private states.State state;
    private int id;
    private int wallet;

    private int bettedHorse;

    private Paddock paddock;
    private ControlCentre controlCentre;
    private BettingCentre bettingCentre;
    private GeneralRepository generalRepository;

    public Spectator(int id, int wallet, Paddock paddock,
                     ControlCentre controlCentre, BettingCentre bettingCentre,
                     GeneralRepository generalRepository) {
        if (paddock == null)
            throw new IllegalArgumentException("Invalid Paddock.");
        if (controlCentre == null)
            throw new IllegalArgumentException("Invalid Control Centre.");
        if (bettingCentre == null)
            throw new IllegalArgumentException("Invalid Betting Centre.");
        if (generalRepository == null)
            throw new IllegalArgumentException("Invalid General Repository.");

        this.state = SpectatorState.WAITING_FOR_A_RACE_TO_START;
        this.id = id;
        this.wallet = wallet;
        this.bettedHorse = -1;
        this.paddock = paddock;
        this.controlCentre = controlCentre;
        this.bettingCentre = bettingCentre;
        this.generalRepository = generalRepository;
    }
    
    public void run() {
        Bet b;

        //while(controlCentre.waitForNextRace()) {
        for (int i = 0; i < EventVariables.NUMBER_OF_RACES; i++) {
            controlCentre.waitForNextRace();

            // goCheckHorses
            paddock.goCheckHorses();

            do {
                b = getBet();
            } while(!bettingCentre.placeABet(
                    b.getSpectatorID(), b.getHorseID(), b.getValue()));

            // update wallet
            wallet -= b.getValue();
            controlCentre.goWatchTheRace();

            if (controlCentre.haveIWon(this.bettedHorse))
                wallet += bettingCentre.goCollectTheGains(this.id);
        }

        controlCentre.relaxABit();
    }

    private Bet getBet() {
        int betValue;

        Random rnd = ThreadLocalRandom.current();

        // pick random horse to bet
        bettedHorse = rnd.nextInt(EventVariables.NUMBER_OF_HORSES_PER_RACE);

        // pick a random bet value, with a max of (wallet * number_of_races)
        // to avoid bankruptcy
        betValue = rnd.nextInt((wallet / EventVariables.NUMBER_OF_RACES - 1)) + 1;

        return new Bet(this.id, bettedHorse, betValue);
    }

    public void setSpectatorState(states.State state) {
        this.state = state;
    }

    public states.State getSpectatorState() {
        return state;
    }

    public int getID() {
        return id;
    }

    public double getWallet() {
        return wallet;
    }
}

package entities;

import main.EventVariables;
import sharedRegions.*;
import states.SpectatorState;
import utils.Bet;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class Spectator extends Thread {

    private states.State state;
    private int id;
    private double wallet;

    private List<Integer> horsesToBet;
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
    
    public void run() {
        while(controlCentre.waitForNextRace()) {
            // goCheckHorses
            controlCentre.goCheckHorses();
            horsesToBet = paddock.goCheckHorses();

            while(!bettingCentre.placeABet(getBet()));

            controlCentre.goWatchTheRace();

            if (controlCentre.haveIWon(this.bettedHorse))
                wallet += bettingCentre.goCollectTheGains(this.id);
        }

        controlCentre.relaxABit(this.id);
    }

    private Bet getBet() {
        double betValue;

        Random rnd = ThreadLocalRandom.current();

        // pick random horse to bet
        bettedHorse = horsesToBet.get(rnd.nextInt(horsesToBet.size()));

        // pick a random bet value, with a max of (wallet * number_of_races)
        // to avoid bankruptcy
        betValue = rnd.nextDouble() *
                (wallet / EventVariables.NUMBER_OF_RACES - 1) + 1;

        // update wallet
        wallet -= betValue;

        return new Bet(this.id, bettedHorse, betValue);
    }

    public states.State getCurrentState() {
        return state;
    }

    public int getID() {
        return id;
    }

    public double getWallet() {
        return wallet;
    }
}

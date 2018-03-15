package entities;

import generalRepository.GeneralRepository;
import main.EventVariables;
import sharedRegions.*;
import states.SpectatorState;
import states.State;
import utils.Bet;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class Spectator extends Thread {

    private states.State state;
    private int id;
    private double wallet;

    private int bettedHorse;

    private Paddock paddock;
    private ControlCentre controlCentre;
    private BettingCentre bettingCentre;
    private GeneralRepository generalRepository;

    public Spectator(int id, double wallet, Paddock p, ControlCentre c,
                     BettingCentre b, GeneralRepository gr) {
        if (p == null)
            throw new IllegalArgumentException("Invalid Paddock.");
        if (c == null)
            throw new IllegalArgumentException("Invalid Control Centre.");
        if (b == null)
            throw new IllegalArgumentException("Invalid Betting Centre.");
        if (gr == null)
            throw new IllegalArgumentException("Invalid General Repository.");

        this.state = SpectatorState.WAITING_FOR_A_RACE_TO_START;
        this.id = id;
        this.wallet = wallet;
        this.bettedHorse = -1;
        this.paddock = p;
        this.controlCentre = c;
        this.bettingCentre = b;
        this.generalRepository = gr;
    }
    
    public void run() {
        Bet b;

        generalRepository.setSpectatorWallet(id, wallet);
        while(controlCentre.waitForNextRace()) {

            // goCheckHorses
            paddock.goCheckHorses();

            do {
                b = getBet();
            } while(!bettingCentre.placeABet(b));

            // Update wallet
            wallet -= b.getValue();

            controlCentre.goWatchTheRace();

            if (controlCentre.haveIWon(this.bettedHorse))
                wallet += bettingCentre.goCollectTheGains(this.id);

        }

        controlCentre.relaxABit();
    }

    private Bet getBet() {
        double betValue;

        Random rnd = ThreadLocalRandom.current();

        // pick random horse to bet
        bettedHorse = rnd.nextInt(EventVariables.NUMBER_OF_HORSES_PER_RACE);

        // pick a random bet value, with a max of (wallet * number_of_races)
        // to avoid bankruptcy
        betValue = rnd.nextDouble() *
                (wallet / EventVariables.NUMBER_OF_RACES - 1) + 1;

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

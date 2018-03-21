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
    private int strategy;

    private Paddock paddock;
    private ControlCentre controlCentre;
    private BettingCentre bettingCentre;
    private GeneralRepository generalRepository;

    public Spectator(int id, int wallet, int strategy, Paddock paddock,
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
        this.strategy = strategy;
        this.paddock = paddock;
        this.controlCentre = controlCentre;
        this.bettingCentre = bettingCentre;
        this.generalRepository = generalRepository;
    }
    
    public void run() {
        int bettedHorse;

        //while(controlCentre.waitForNextRace()) {
        for (int i = 0; i < EventVariables.NUMBER_OF_RACES; i++) {
            controlCentre.waitForNextRace();

            // goCheckHorses
            paddock.goCheckHorses();

            bettedHorse = bettingCentre.placeABet();

            // update wallet
            controlCentre.goWatchTheRace();

            if (controlCentre.haveIWon(bettedHorse))
                wallet += bettingCentre.goCollectTheGains(this.id);
        }

        controlCentre.relaxABit();
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

    public int getWallet() {
        return wallet;
    }

    public void updateWallet(int value) {
        this.wallet += value;
    }

    public int getStrategy() {
        return strategy;
    }


}

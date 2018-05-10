package entities;

import registries.RegPlaceABet;
import sharedRegions.*;
import states.SpectatorState;

/**
 * The Spectator is the entity which attends the event and bets on horses to win
 * the races.
 */
public class Spectator extends Thread {
    /**
     * Current state of the Spectator lifecycle.
     */
    private SpectatorState state;

    /**
     * ID of the Spectator.
     */
    private int id;

    /**
     * Current amount of money in the Spectator's wallet.
     */
    private int wallet;

    /**
     * The betting strategy the Spectator will perform.
     */
    private int strategy;

    /**
     * Instance of the shared region Paddock.
     */
    private Paddock paddock;

    /**
     * Instance of the shared region Control Centre.
     */
    private ControlCentre controlCentre;

    /**
     * Instance of the shared region Betting Centre.
     */
    private BettingCentre bettingCentre;

    /**
     * Creates a new instance of Spectator.
     * @param id ID of the Spectator.
     * @param wallet Initial amount of money in the Spectator's wallet.
     * @param strategy The betting strategy the Spectator will perform.
     * @param paddock Reference to an instance of the shared region Paddock.
     * @param controlCentre Reference to an instance of the shared region
     *                      Control Centre.
     * @param bettingCentre Reference to an instance of the shared region
     *                      Betting Centre.
     */
    public Spectator(int id, int wallet, int strategy, Paddock paddock,
                     ControlCentre controlCentre, BettingCentre bettingCentre) {
        if (paddock == null)
            throw new IllegalArgumentException("Invalid Paddock.");
        if (controlCentre == null)
            throw new IllegalArgumentException("Invalid Control Centre.");
        if (bettingCentre == null)
            throw new IllegalArgumentException("Invalid Betting Centre.");

        this.state = SpectatorState.WAITING_FOR_A_RACE_TO_START;
        this.id = id;
        this.wallet = wallet;
        this.strategy = strategy;
        this.paddock = paddock;
        this.controlCentre = controlCentre;
        this.bettingCentre = bettingCentre;
    }

    /**
     * Spectator lifecycle.
     */
    public void run() {
        RegPlaceABet pab;
        int bettedHorse;

        while(controlCentre.waitForNextRace(id)) {
            state = SpectatorState.WAITING_FOR_A_RACE_TO_START;

            // goCheckHorses
            paddock.goCheckHorses();

            // Place a bet and return the horse the spectator chose
            pab = bettingCentre.placeABet(id, strategy, wallet);
            state = SpectatorState.PLACING_A_BET;
            wallet = pab.getWallet();
            bettedHorse = pab.getHorseIdx();


            // update wallet
            controlCentre.goWatchTheRace(id);
            state = SpectatorState.WATCHING_A_RACE;

            // Check if won the bet and collect the gains if so
            if (controlCentre.haveIWon(bettedHorse)) {
                wallet += bettingCentre.goCollectTheGains(id);
                state = SpectatorState.COLLECTING_THE_GAINS;
            }
        }
        state = SpectatorState.WAITING_FOR_A_RACE_TO_START;

        controlCentre.relaxABit(id);
        state = SpectatorState.CELEBRATING;
    }

    /**
     * Method that returns the current Spectator state.
     * @return Current Spectator state.
     */
    public SpectatorState getSpectatorState() {
        return state;
    }

    /**
     * Updates the current Spectator state.
     * @param state The new Spectator state.
     */
    public void setSpectatorState(SpectatorState state) {
        this.state = state;
    }

    /**
     * Method that returns the ID of the Spectator.
     * @return The ID of the Spectator.
     */
    public int getID() {
        return id;
    }

    /**
     * Method that sets the ID of the Spectator.
     * @param id The new ID of the Spectator.
     */
    public void setID(int id) {
        this.id = id;
    }

    /**
     * Method that returns the current amount of money in the Spectator's wallet.
     * @return The current amount of money in the Spectator's wallet.
     */
    public int getWallet() {
        return wallet;
    }

    /**
     * Method that sets the value at the wallet of the Spectator.
     * @param wallet The new value at the wallet of the Spectator.
     */
    public void setWallet(int wallet) {
        this.wallet = wallet;
    }

    /**
     * Performs a transaction in the Spectator's wallet, summing the argument
     * value to the wallet.
     * @param value The value that will be summed to the wallet (it may add or
     *              subtract depending on its signal)
     */
    public void updateWallet(int value) {
        this.wallet += value;
    }

    /**
     * Method that returns the Spectator's betting strategy.
     * @return The Spectator's betting strategy.
     */
    public int getStrategy() {
        return strategy;
    }

    /**
     * Method that sets the betting strategy of the Spectator.
     * @param strategy The strategy used by the Spectator.
     */
    public void setStrategy(int strategy) {
        this.strategy = strategy;
    }

}

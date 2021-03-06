package entities;

import interfaces.BettingCentreInt;
import interfaces.ControlCentreInt;
import interfaces.PaddockInt;
import registries.RegPlaceABet;
import sharedRegions.*;
import states.SpectatorState;

import java.rmi.RemoteException;

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
    private PaddockInt paddock;

    /**
     * Instance of the shared region Control Centre.
     */
    private ControlCentreInt controlCentre;

    /**
     * Instance of the shared region Betting Centre.
     */
    private BettingCentreInt bettingCentre;

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
    public Spectator(int id, int wallet, int strategy, PaddockInt paddock,
                     ControlCentreInt controlCentre, BettingCentreInt bettingCentre) {
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
        RegPlaceABet pab = null;
        int bettedHorse;

        while(true) {
            try {
                if (!controlCentre.waitForNextRace(id))
                    break;
            } catch (RemoteException e) {
                System.out.println("ControlCentre remote invocation exception: "
                        + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
            ;
            state = SpectatorState.WAITING_FOR_A_RACE_TO_START;

            // goCheckHorses
            try {
                paddock.goCheckHorses(id);
            } catch (RemoteException e) {
                System.out.println("Paddock remote invocation exception: "
                        + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
            setSpectatorState(SpectatorState.APPRAISING_THE_HORSES);

            // Place a bet and return the horse the spectator chose
            try {
                pab = bettingCentre.placeABet(id, strategy, wallet);
            } catch (RemoteException e) {
                System.out.println("BettingCentre remote invocation exception: "
                        + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
            state = SpectatorState.PLACING_A_BET;
            wallet = pab.getWallet();
            bettedHorse = pab.getHorseIdx();


            // update wallet
            try {
                controlCentre.goWatchTheRace(id);
            } catch (RemoteException e) {
                System.out.println("ControlCentre remote invocation exception: "
                        + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
            state = SpectatorState.WATCHING_A_RACE;

            // Check if won the bet and collect the gains if so
            try {
                if (controlCentre.haveIWon(bettedHorse)) {
                    wallet += bettingCentre.goCollectTheGains(id);
                    state = SpectatorState.COLLECTING_THE_GAINS;
                }
            } catch (RemoteException e) {
                System.out.println("ControlCentre and/or BettingCentre remote " +
                        "invocation exception: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }
        state = SpectatorState.WAITING_FOR_A_RACE_TO_START;

        try {
            controlCentre.relaxABit(id);
        } catch (RemoteException e) {
            System.out.println("ControlCentre remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
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

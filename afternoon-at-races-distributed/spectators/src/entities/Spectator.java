package entities;

import states.SpectatorState;
import stubs.BettingCentreStub;
import stubs.ControlCentreStub;
import stubs.PaddockStub;

/**
 * The Spectator is the entity which attends the event and bets on horses to win
 * the races.
 */
public class Spectator extends Thread implements SpectatorInt {
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
    private PaddockStub paddock;

    /**
     * Instance of the shared region Control Centre.
     */
    private ControlCentreStub controlCentre;

    /**
     * Instance of the shared region Betting Centre.
     */
    private BettingCentreStub bettingCentre;

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
    public Spectator(int id, int wallet, int strategy, PaddockStub paddock,
                     ControlCentreStub controlCentre, BettingCentreStub bettingCentre) {
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
        int bettedHorse;

        while(controlCentre.waitForNextRace()) {
            // goCheckHorses
            System.out.println("CHECK HORSES");
            paddock.goCheckHorses();

            System.out.println("PLACING BET");
            // Place a bet and return the horse the spectator chose
            bettedHorse = bettingCentre.placeABet();

            // update wallet
            controlCentre.goWatchTheRace();

            // Check if won the bet and collect the gains if so
            if (controlCentre.haveIWon(bettedHorse))
                wallet += bettingCentre.goCollectTheGains();
        }

        controlCentre.relaxABit();
    }

    /**
     * Method that returns the current Spectator state.
     * @return Current Spectator state.
     */
    @Override
    public SpectatorState getSpectatorState() {
        return state;
    }

    /**
     * Updates the current Spectator state.
     * @param state The new Spectator state.
     */
    @Override
    public void setSpectatorState(SpectatorState state) {
        this.state = state;
    }

    /**
     * Method that returns the ID of the Spectator.
     * @return The ID of the Spectator.
     */
    @Override
    public int getID() {
        return id;
    }

    /**
     * Method that sets the ID of the Spectator.
     * @param id The new ID of the Spectator.
     */
    @Override
    public void setID(int id) {
        this.id = id;
    }

    /**
     * Method that returns the current amount of money in the Spectator's wallet.
     * @return The current amount of money in the Spectator's wallet.
     */
    @Override
    public int getWallet() {
        return wallet;
    }

    /**
     * Method that sets the value at the wallet of the Spectator.
     * @param wallet The new value at the wallet of the Spectator.
     */
    @Override
    public void setWallet(int wallet) {
        this.wallet = wallet;
    }

    /**
     * Performs a transaction in the Spectator's wallet, summing the argument
     * value to the wallet.
     * @param value The value that will be summed to the wallet (it may add or
     *              subtract depending on its signal)
     */
    @Override
    public void updateWallet(int value) {
        this.wallet += value;
    }

    /**
     * Method that returns the Spectator's betting strategy.
     * @return The Spectator's betting strategy.
     */
    @Override
    public int getStrategy() {
        return strategy;
    }

    /**
     * Method that sets the betting strategy of the Spectator.
     * @param strategy The strategy used by the Spectator.
     */
    @Override
    public void setStrategy(int strategy) {
        this.strategy = strategy;
    }
}

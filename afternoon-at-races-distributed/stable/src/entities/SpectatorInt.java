package entities;

import states.SpectatorState;

/**
 * Interface that defines the entity Spectator.
 */
public interface SpectatorInt {
    /**
     * Method that returns the current Spectator state.
     * @return Current Spectator state.
     */
    SpectatorState getSpectatorState();

    /**
     * Updates the current Spectator state.
     * @param state The new Spectator state.
     */
    void setSpectatorState(SpectatorState state);

    /**
     * Method that returns the ID of the Spectator.
     * @return The ID of the Spectator.
     */
    int getID();

    /**
     * Method that sets the ID of the Spectator.
     * @param id The new ID of the Spectator.
     */
    void setID(int id);

    /**
     * Method that returns the current amount of money in the Spectator's wallet.
     * @return The current amount of money in the Spectator's wallet.
     */
    int getWallet();

    /**
     * Method that sets the value at the wallet of the Spectator.
     * @param wallet The new value at the wallet of the Spectator.
     */
    void setWallet(int wallet);

    /**
     * Performs a transaction in the Spectator's wallet, summing the argument
     * value to the wallet.
     * @param value The value that will be summed to the wallet (it may add or
     *              subtract depending on its signal)
     */
    void updateWallet(int value);

    /**
     * Method that returns the Spectator's betting strategy.
     * @return The Spectator's betting strategy.
     */
    int getStrategy();

    /**
     * Method that sets the betting strategy of the Spectator.
     * @param strategy The strategy used by the Spectator.
     */
    void setStrategy(int strategy);

    /**
     * Method that returns the current race identifier.
     * @return The current race identifier.
     */
    public int getRaceNumber();

    /**
     * Method that sets the current race identifier.
     * @param raceNumber The current race identifier.
     */
    public void setRaceNumber(int raceNumber);
}

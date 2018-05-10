package interfaces;

import registries.RegPlaceABet;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This data type defines the operational interface of a remote object
 * of type/shared region Betting Centre.
 */
public interface BettingCentreInt extends Remote {

    /**
     * Execution of remote method acceptTheBets.
     * Method invoked by the Broker.
     * The broker changes its state to WAITING_FOR_BETS, signals the Spectators
     * that is accepting bets.
     * It blocks each time it validates a new bet and wakes up the Spectators
     * that still have pending bets.
     * @param raceID The current raceID.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void acceptTheBets(int raceID) throws RemoteException;

    /**
     * Execution of remote method placeABet.
     * Method invoked by each one of the Spectators to place their bet, effectively
     * changing their state to PLACING_A_BET.
     * They blocked in queue waiting for the Broker to validate their bet.
     * If their bet is not accepted a new bet is generated.
     * @param spectatorID ID of the Spectator placing a bet.
     * @param strategy Betting strategy used by the Spectator.
     * @param wallet Amount in Spectator's wallet.
     * @return A registry which contains the Horse's index on the current race
     * that the Spectator bet on and the updated value of his/her wallet after
     * placing the bet.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    RegPlaceABet placeABet(int spectatorID, int strategy, int wallet)
            throws RemoteException;

    /**
     * Execution of remote method areThereAnyWinners.
     * Method invoked by the Broker to check if there are any winning bets.
     * @param winners An array of horseIdxs that contains the race winners.
     * @return True if there are any winners, false otherwise.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    boolean areThereAnyWinners(int[] winners) throws RemoteException;

    /**
     * Execution of remote method honourTheBets.
     * Method invoked by the Broker if there were any winning bets.
     * The Broker changes its state to SETTLING_ACCOUNTS and signals the
     * Spectators waiting for collecting their gains that it's open for settling
     * accounts.
     * He blocks each time he pays a winning bet and wakes up Spectators still
     * waiting to collect their rewards.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void honourTheBets() throws RemoteException;

    /**
     * Execution of remote method goCollectTheGains.
     * Method invoked by each one of the winning Spectators.
     * They change their state to COLLECTING_THE_GAINS and block in queue waiting
     * for their rewards.
     * @param spectatorID ID of the Spectator that had a winning bet and now
     *                    requests his/her gains.
     * @return The value the Spectator won.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    double goCollectTheGains(int spectatorID) throws RemoteException;
}
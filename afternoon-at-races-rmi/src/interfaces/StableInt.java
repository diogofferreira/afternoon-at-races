package interfaces;

import registries.RegProceedToStable;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This data type defines the operational interface of a remote object
 * of type/shared region Racing Track.
 */
public interface StableInt extends Remote {

    /**
     * Execution of remote method getRaceOdds.
     * Method that returns the odd of all horses running on the race with ID
     * passed as argument..
     * @param raceID The ID of the race of the odds.
     * @return Array with horses' odds.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    double[] getRaceOdds(int raceID) throws RemoteException;

    /**
     * Execution of remote method summonHorsesToPaddock.
     * Method invoked by the Broker to notify all horses of the current race
     * to proceed to Paddock.
     * @param raceID The ID of the current race; it determines which horses will
     *               be waken up.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void summonHorsesToPaddock(int raceID) throws RemoteException;

    /**
     * Execution of remote method proceedToStable.
     * Method invoked by an Horse, where usually it gets blocked.
     * It sets the its state to AT_THE_STABLE.
     * It is waken up by the Broker to proceed to Paddock or when the event ends.
     * @param horseId ID of the Horse/Jockey pair.
     * @param agility Agility of the horse, which in practice corresponds to the
     *               maximum step the horse can make in each iteration.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    RegProceedToStable proceedToStable(int horseId, int agility) throws RemoteException;

    /**
     * Execution of remote method entertainTheGuests.
     * Method invoked by the Broker to signal all horses to wake up, ending the
     * event.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void entertainTheGuests() throws RemoteException;
}
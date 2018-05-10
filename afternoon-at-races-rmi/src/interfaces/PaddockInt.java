package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This data type defines the operational interface of a remote object
 * of type/shared region Paddock.
 */
public interface PaddockInt extends Remote {

    /**
     * Execution of remote method proceedToPaddock.
     * Method invoked by each one of the Horses. They will change their state
     * to AT_THE_PADDOCK and wait until all Spectators arrive to the Paddock.
     * @param raceId The race ID in which the horse will run.
     * @param raceIdx The horse's index/position on the race.
     * @throws RemoteException if the invocation of the remote method fails.
     */
     void proceedToPaddock(int raceId, int raceIdx) throws RemoteException;

    /**
     * Execution of remote method goCheckHorses.
     * Method invoked by each one of the Spectators where they will update their
     * state to APPRAISING_THE_HORSES and will block waiting
     * @param spectatorId ID of the Spectator.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void goCheckHorses(int spectatorId) throws RemoteException;
}

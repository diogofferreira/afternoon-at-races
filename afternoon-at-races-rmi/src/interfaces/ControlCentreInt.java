package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This data type defines the operational interface of a remote object
 * of type/shared region Control Centre.
 */
public interface ControlCentreInt extends Remote {

    /**
     * Execution of remote method openTheEvent.
     * Method invoked by the Broker in order to start the event. It just simply
     * updates the Broker state and updates the General Repository.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void openTheEvent() throws RemoteException;

    /**
     * Execution of remote method summonHorsesToPaddock.
     * Method invoked by Broker, signaling the start of the event.
     * The Broker updates the current raceID and sets his state to
     * ANNOUNCING_NEXT_RACE, while signalling the Horses to proceed to Paddock.
     * @param raceID The ID of the race that will take place.
     *               @throws RemoteException if the invocation of the remote method fails.
     */
    void summonHorsesToPaddock(int raceID) throws RemoteException;

    /**
     * Execution of remote method waitForNextRace.
     * This method is invoked by every Spectator while they're waiting for
     * a race to start.
     * While waiting here, they update their state to WAITING_FOR_A_RACE_TO_START.
     * @param spectatorID ID of the Spectator arriving to the Control Centre to
     *                    await for the next race.
     * @return True if there's still a race next.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    boolean waitForNextRace(int spectatorID) throws RemoteException;

    /**
     * Execution of remote method proceedToPaddock.
     * Method invoked by the last Horse/Jockey pair of the current race to arrive
     * to the Paddock, thus waking up all the Spectators to proceed to Paddock
     * and appraise the horses.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void proceedToPaddock() throws RemoteException;

    /**
     * Execution of remote method goCheckHorses.
     * Method invoked by the last Horse/Jockey pair arriving to Paddock in order
     * to wake up the Broker.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void goCheckHorses() throws RemoteException;

    /**
     * Execution of remote method goWatchTheRace.
     * Method invoked by each Spectator before the start of each race.
     * They will block in WATCHING_A_RACE state until the Broker reports the
     * results of the race.
     * @param spectatorID ID of the Spectator watching the race.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void goWatchTheRace(int spectatorID) throws RemoteException;

    /**
     * Execution of remote method startTheRace.
     * Method invoked by the Broker.
     * He'll wait here until the last Horse/Jockey pair to cross the finish line
     * wakes him up.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void startTheRace() throws RemoteException;

    /**
     * Execution of remote method finishTheRace.
     * Method invoked by the last Horse/Jockey pair to cross the finish line.
     * The Broker will be notified to wake up and to report the results.
     * @param standings An array of standings of the Horses that in the race.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void finishTheRace(int[] standings) throws RemoteException;

    /**
     * Execution of remote method reportResults.
     * Method invoked by the Broker signalling all Spectators that the results
     * of the race have been reported.
     * @return An array of Horses' raceIdx that won the race.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    int[] reportResults() throws RemoteException;

    /**
     * Execution of remote method haveIWon.
     * Method invoked by each Spectator to verify if they betted on a winning
     * horse.
     * @param horseIdx The raceIdx of the horse they bet on.
     * @return A boolean indicating if the Spectator invoking the method won
     * his/her bet.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    boolean haveIWon(int horseIdx) throws RemoteException;

    /**
     * Execution of remote method celebreate.
     * Method invoked by the Broker in order to signal the spectators that the
     * event has ended.
     * Meanwhile, Broker also sets its state to PLAYING_HOST_AT_THE_BAR.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void celebrate() throws RemoteException;

    /**
     * Execution of remote method relaxABit.
     * Last method invoked by the Spectators, changing their state to CELEBRATING.
     * @param spectatorID ID of the Spectator that will celebrate after the
     *                    event has ended.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void relaxABit(int spectatorID) throws RemoteException;

}

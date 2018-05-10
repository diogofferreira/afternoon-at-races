package interfaces;

import states.BrokerState;
import states.HorseState;
import states.SpectatorState;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This data type defines the operational interface of a remote object
 * of type/shared region General Repository.
 */
public interface GeneralRepositoryInt extends Remote {

    /**
     * Execution of remote method setBrokerState.
     * Method that updates the Broker state.
     * @param brokerState The new Broker state.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void setBrokerState(BrokerState brokerState) throws RemoteException;

    /**
     * Execution of remote method setSpectatorState.
     * Method that updates the state of a Spectator.
     * @param spectatorId The ID of the Spectator whose the state is updated.
     * @param spectatorState The new state of the referenced Spectator.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void setSpectatorState(int spectatorId, SpectatorState spectatorState)
            throws RemoteException;

    /**
     * Execution of remote method setSpectatorGains.
     * Method that updates the referenced Spectator's wallet by adding the amount
     * the amount passed as argument.
     * @param spectatorId The ID of the Spectator whose wallet will be updated.
     * @param amount The amount of money to add to the wallet.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void setSpectatorGains(int spectatorId, int amount) throws RemoteException;
    /**
     * Execution of remote method setHorseState.
     * Method that sets the reference Horse/Jockey pair state.
     * @param raceID The ID of the race which the pair will run.
     * @param horseIdx The raceIdx of the Horse whose state will be updated.
     * @param horseState The next Horse state.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void setHorseState(int raceID, int horseIdx, HorseState horseState)
            throws RemoteException;

    /**
     * Execution of remote method setHorseAgility.
     * Method that sets an Horse agility, i.e., the maximum step per iteration
     * it can takes.
     * @param raceID The raceID where the Horse will participate.
     * @param horseIdx The raceIdx of the reference Horse.
     * @param horseAgility The agility of the referenced Horse.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void setHorseAgility(int raceID, int horseIdx, int horseAgility)
            throws RemoteException;

    /**
     * Execution of remote method setSpectatorsBet.
     * Method that sets the bet of the referenced Spectator on the current race.
     * @param spectatorId The ID of the Spectator placing the bet.
     * @param spectatorBet The value of the bet placed.
     * @param spectatorBettedHorse The raceIdx of the Horse the Spectator bet on.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void setSpectatorsBet(int spectatorId, int spectatorBet,
                          int spectatorBettedHorse) throws RemoteException;

    /**
     * Execution of remote method setHorsesOdd.
     * Method that sets the odds of the Horses running on the current race.
     * @param raceID The ID of the race where these odds are applied.
     * @param horsesOdd Array of horses odds, indexed by their raceIdx.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void setHorsesOdd(int raceID, double[] horsesOdd) throws RemoteException;

    /**
     * Execution of remote method setHorsePosition.
     * Updates the Horse/Jockey pair current position (i.e., the travelled
     * distance).
     * @param horseIdx The raceIdx of the Horse whose position is being updated.
     * @param horsePosition The new Horse position.
     * @param horseStep The number of steps the Horse has already taken.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void setHorsePosition(int horseIdx, int horsePosition, int horseStep)
            throws RemoteException;

    /**
     * Execution of remote method setHorsesStanding.
     * Method that signals the horses' position in the race.
     * @param standings horses' position in the race.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void setHorsesStanding(int[] standings) throws RemoteException;

    /**
     * Execution of remote method initRace.
     * Method that resets all the race related variables, such as the
     * raceNumber (ID), the spectators bets, the horses odds and
     * travelled distances.
     * @param raceNumber The updated race number.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void initRace(int raceNumber) throws RemoteException;
}

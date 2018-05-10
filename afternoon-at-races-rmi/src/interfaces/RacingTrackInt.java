package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This data type defines the operational interface of a remote object
 * of type/shared region Racing Track.
 */
public interface RacingTrackInt extends Remote {

    /**
     * Execution of remote method proceedToStartLine.
     * Method invoked by each one the Horses coming from the Paddock.
     * They will update their state to AT_THE_STARTING_LINE and will block
     * accordingly to the raceIdx of each one of them in the correspondent
     * condition variable.
     * The last Horse/Jockey pair to arrive also wakes up the Spectators so
     * then can place their bets.
     * After being waken up by the Broker to start the race, they update their
     * state to RUNNING.
     * @param raceId The race ID in which the horse will run.
     * @param raceIdx The horse's index/position on the race.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void proceedToStartLine(int raceId, int raceIdx) throws RemoteException;

    /**
     * Execution of remote method startTheRace.
     * Method invoked by the Broker to signal the Horses to start running.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void startTheRace() throws RemoteException;

    /**
     * Execution of remote method makeAMove.
     * Method invoked by every Horse that hasn't still crossed the finish line.
     * It generates a new step and updates its position.
     * Finally, it wakes up the next horse in the arrival order to the Racing
     * Track that hasn't finished the race.
     * @param raceId The race ID in which the horse will run.
     * @param raceIdx The horse's index/position on the race.
     * @param currentHorseStep The current step/iteration of the horse in the race.
     * @param currentPosition The current position of the horse in the race.
     * @param step The distance of the next step the Horse will take.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    void makeAMove(int raceId, int raceIdx, int currentHorseStep,
                   int currentPosition, int step) throws RemoteException;

    /**
     * Execution of remote method hasFinishLineBeenCrossed.
     * Method invoked by each one of the participating Horses checking they have
     * already crossed the finish line.
     * If true, they check if they have won the race and add their ID to the
     * corresponding list of winners.
     * If it's the last horse crossing the finish line, it wakes up the Broker
     * at the Control Centre and provides the list of winners.
     * Otherwise, just wakes up the next horse that still hasn't finish the race.
     * @param raceId The race ID in which the horse will run.
     * @param raceIdx The horse's index/position on the race.
     * @param currentHorseStep The current step/iteration of the horse in the race.
     * @param currentPosition The current position of the horse in the race.
     * @return Boolean indicating whether the Horse/Jockey pair that invoked the
     * method has already crossed the finish line or not.
     * @throws RemoteException if the invocation of the remote method fails.
     */
    boolean hasFinishLineBeenCrossed(int raceId, int raceIdx, int currentHorseStep,
                                     int currentPosition) throws RemoteException;
}
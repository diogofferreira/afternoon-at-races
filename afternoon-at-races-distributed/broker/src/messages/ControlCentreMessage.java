package messages;

import messageTypes.ControlCentreMessageTypes;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This data type defines a message sent to and from the Control Centre server.
 * It contains a variety of fields and constructors to adapt to the different
 * entities using it.
 */
public class ControlCentreMessage implements Serializable {

    /**
     * Serial version of the message.
     */
    private static final long serialVersionUID = 1001L;

    // method type
    /**
     * Identifier of the method that the entity pretends to invoke in the shared
     * region server.
     */
    private int method;

    // arguments
    /**
     * Id of the race.
     */
    private int raceId;

    /**
     * Final standings of the horses on a race.
     */
    private int[] standings;

    /**
     * Index of the horse the Spectator bet on.
     */
    private int horseIdx;

    // replies
    /**
     * Boolean value that is true if there is a new race next or false if the
     * event is ending.
     */
    private boolean isThereARace;

    /**
     * Boolean that informs a Spectator if he has a winning bet (true)
     * or not (false).
     */
    private boolean haveIWon;

    /**
     * Array that contains the indexes of the horses that won a certain race.
     */
    private int[] winners;

    // entity id
    /**
     * Identifier of the entity that is sending the message.
     */
    private int entityId;

    /**
     * Textual representation of an error message;
     */
    private String errorMessage;

    /**
     * Constructor (for error messages).
     * @param error Type of the message (in this case an error message).
     */
    public ControlCentreMessage(ControlCentreMessageTypes error) {
        if (error != ControlCentreMessageTypes.ERROR)
            throw new IllegalArgumentException("Not an error message!");
        this.method = error.getId();
    }

    /**
     * Constructor (type 1).
     * @param method Method an entity invokes on the shared region server.
     * @param entityId Id of the entity sending the message.
     */
    public ControlCentreMessage(ControlCentreMessageTypes method,
                                int entityId) {
        this.method = method.getId();
        this.entityId = entityId;
    }

    /**
     * Constructor (type 2).
     * @param method Method an entity invokes on the shared region server.
     * @param raceIdOrHorseIdx Field that represents the race identifier when
     *                         the invoked method is SUMMON_HORSES_TO_PADDOCK
     *                         or the horse index a Spectator bet on when the
     *                         server is responding to a HAVE_I_WON invocation.
     * @param entityId Id of the entity sending the message.
     */
    public ControlCentreMessage(ControlCentreMessageTypes method,
                                int raceIdOrHorseIdx, int entityId) {
        this.method = method.getId();
        if (method == ControlCentreMessageTypes.SUMMON_HORSES_TO_PADDOCK)
            this.raceId = raceIdOrHorseIdx;
        else
            this.horseIdx = raceIdOrHorseIdx;
        this.entityId = entityId;
    }

    /**
     * Constructor (type 3).
     * @param method Method an entity invokes on the shared region server.
     * @param isThereARaceOrHaveIWon Field that represents if there is still a
     *                               race next (true) or if the event is ending
     *                               (false) when the invoked method is
     *                               WAIT_FOR_NEXT_RACE, or represents if a user
     *                               has a winning bet (true) or not (false)
     *                               when the invoked method is HAVE_I_WON.
     * @param entityId Id of the entity sending the message.
     */
    public ControlCentreMessage(ControlCentreMessageTypes method,
                                boolean isThereARaceOrHaveIWon, int entityId) {
        this.method = method.getId();
        if (method == ControlCentreMessageTypes.WAIT_FOR_NEXT_RACE)
            this.isThereARace = isThereARaceOrHaveIWon;
        else
            this.haveIWon = isThereARaceOrHaveIWon;
        this.entityId = entityId;
    }

    /**
     * Constructor (type 4).
     * @param method Method an entity invokes on the shared region server.
     * @param standingsOrWinners Array the contains the horses final standings
     *                           in a race when the invoked method is
     *                           FINISH_THE_RACE or the winner horses indexes
     *                           of a race when the invoked method is
     *                           REPORT_RESULTS.
     * @param entityId Id of the entity sending the message.
     */
    public ControlCentreMessage(ControlCentreMessageTypes method,
                                int[] standingsOrWinners, int entityId) {
        this.method = method.getId();
        if (method == ControlCentreMessageTypes.FINISH_THE_RACE)
            this.standings = standingsOrWinners;
        else
            this.winners = standingsOrWinners;
        this.entityId = entityId;
    }

    /**
     * Method that returns an integer identifier of the method invoked on the
     * shared region.
     * @return The identifier of the method invoked on the shared region.
     */
    public int getMethod() {
        return method;
    }

    /**
     * Method that returns an integer identifier of the method invoked on the
     * shared region.
     * @param method The method invoked on the shared region.
     */
    public void setMethod(ControlCentreMessageTypes method) {
        this.method = method.getId();
    }

    /**
     * Method that returns the race identifier which the current
     * message corresponds.
     * @return The race identifier which the current message corresponds.
     */
    public int getRaceId() {
        return raceId;
    }

    /**
     * Method that sets the race identifier which the current
     * message corresponds.
     * @param raceId The race identifier which the current message corresponds.
     */
    public void setRaceId(int raceId) {
        this.raceId = raceId;
    }

    /**
     * Method that returns the final standings of the horses on a race.
     * @return The final standings of the horses on a race.
     */
    public int[] getStandings() {
        return standings;
    }

    /**
     * Method that sets the final standings of the horses on a race.
     * @param standings The final standings of the horses on a race.
     */
    public void setStandings(int[] standings) {
        this.standings = standings;
    }

    /**
     * Method that returns the index of the horse the Spectator bet on.
     * @return The index of the horse the Spectator bet on.
     */
    public int getHorseIdx() {
        return horseIdx;
    }

    /**
     * Method that sets the index of the horse the Spectator bet on.
     * @param horseIdx The index of the horse the Spectator bet on.
     */
    public void setHorseIdx(int horseIdx) {
        this.horseIdx = horseIdx;
    }

    /**
     * Method that returns a boolean value that is true if there is a new race
     * next or false if the event is ending.
     * @return True if there is a new race next or false if the event is ending.
     */
    public boolean isThereARace() {
        return isThereARace;
    }

    /**
     * Method that sets a boolean value that is true if there is a new race
     * next or false if the event is ending.
     * @param thereARace A boolean that is true if there is a new race next
     *                   or false if the event is ending.
     */
    public void setThereARace(boolean thereARace) {
        isThereARace = thereARace;
    }

    /**
     * Method that returns a boolean that informs a Spectator if he has
     * a winning bet (true) or not (false).
     * @return True if the Spectator has a winning bet, false otherwise.
     */
    public boolean isHaveIWon() {
        return haveIWon;
    }

    /**
     * Method that sets a boolean that informs a Spectator if he has
     * a winning bet (true) or not (false).
     * @param haveIWon A boolean that is true if the Spectator has a winning bet,
     *                false otherwise.
     */
    public void setHaveIWon(boolean haveIWon) {
        this.haveIWon = haveIWon;
    }

    /**
     * Method that returns the array that contains the indexes of the horses
     * that won a certain race.
     * @return the array that contains the indexes of the horses
     * that won a certain race.
     */
    public int[] getWinners() {
        return winners;
    }

    /**
     * Method that sets the array that contains the indexes of the horses
     * that won a certain race.
     * @param winners An array that contains the indexes of the horses
     * that won a certain race.
     */
    public void setWinners(int[] winners) {
        this.winners = winners;
    }

    /**
     * Method that returns the race identifier which the current
     * message corresponds.
     * @return The race identifier which the current message corresponds.
     */
    public int getEntityId() {
        return entityId;
    }

    /**
     * Method that sets the race identifier which the current
     * message corresponds.
     * @param entityId The race identifier which the current message corresponds.
     */
    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    /**
     * Method that returns the textual representation of an error message.
     * @return The textual representation of an error message.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Method that sets the textual representation of an error message.
     * @param errorMessage  The textual representation of an error message.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Method that returns a textual representation of the message.
     * @return A textual representation of the message.
     */
    @Override
    public String toString() {
        return "ControlCentreMessage{" +
                "method=" + method +
                ", raceId=" + raceId +
                ", standings=" + Arrays.toString(standings) +
                ", horseIdx=" + horseIdx +
                ", isThereARace=" + isThereARace +
                ", haveIWon=" + haveIWon +
                ", winners=" + Arrays.toString(winners) +
                ", entityId=" + entityId +
                '}';
    }
}

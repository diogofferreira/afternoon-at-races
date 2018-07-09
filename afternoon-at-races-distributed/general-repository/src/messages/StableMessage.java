package messages;

import messageTypes.StableMessageTypes;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This data type defines a message sent to and from the Stable server.
 * It contains a variety of fields and constructors to adapt to the different
 * entities using it.
 */
public class StableMessage implements Serializable {

    /**
     * Serial version of the message.
     */
    private static final long serialVersionUID = 1006L;

    // method type
    /**
     * Identifier of the method that the entity pretends to invoke in the shared
     * region server.
     */
    private int method;

    // implicit info
    /**
     * Id of the race.
     */
    private int raceId;

    /**
     * Horses' index on the race.
     */
    private int raceIdx;

    /**
     * Horse's agility value.
     */
    private int agility;

    /**
     * Indicates if an Horse as already run.
     */
    private boolean alreadyRun;

    // arguments
    /**
     * Array of Horse/Jockey pair IDs that will store the lineups for all races.
     */
    private int[] horsesId;

    // replies
    /**
     * Array that stores the odds of each one of the Horses participating in the
     * current race, indexed by their raceIdx.
     */
    private double[] raceOdds;

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
     * @param inMessage Message which contains an error.
     * @param errorMessage Textual representation of the error.
     */
    public StableMessage(StableMessage inMessage, String errorMessage) {
        if (inMessage == null)
            throw new IllegalArgumentException("Invalid inMessage");
        if (errorMessage == null)
            throw new IllegalArgumentException("Invalid error description");

        this.method = StableMessageTypes.ERROR.getId();
        this.errorMessage = errorMessage;
        this.raceId = inMessage.getRaceId();
        this.raceIdx = inMessage.getRaceIdx();
        this.agility = inMessage.getAgility();
        this.horsesId = inMessage.getHorsesId();
        this.raceOdds = inMessage.getRaceOdds();
        this.entityId = inMessage.getEntityId();
    }

    /**
     * Constructor (type 1).
     * @param method Method an entity invokes on the shared region server.
     * @param entityId Id of the entity sending the message.
     */
    public StableMessage(StableMessageTypes method,
                         int entityId) {
        this.method = method.getId();
        this.entityId = entityId;
    }

    /**
     * Constructor (type 2).
     * @param method Method an entity invokes on the shared region server.
     * @param horsesId Array of Horse/Jockey pair IDs that will store the lineups
     *                for all races.
     * @param entityId Id of the entity sending the message.
     */
    public StableMessage(StableMessageTypes method,
                         int[] horsesId, int entityId) {
        this.method = method.getId();
        this.horsesId = horsesId;
        this.entityId = entityId;
    }

    /**
     * Constructor (type 3).
     * @param method Method an entity invokes on the shared region server.
     * @param raceIdOrAgility Field containing either the id of the race or the
     *                        Horses' agility value.
     * @param entityId Id of the entity sending the message.
     */
    public StableMessage(StableMessageTypes method,
                         int raceIdOrAgility, int entityId) {
        this.method = method.getId();
        if (method == StableMessageTypes.PROCEED_TO_STABLE)
            this.agility = raceIdOrAgility;
        else
            this.raceId = raceIdOrAgility;
        this.entityId = entityId;
    }

    /**
     * Constructor (type 4).
     * @param method Method an entity invokes on the shared region server.
     * @param raceOdds Array containing the odds of each one of the Horses
     *                participating in the current race, indexed by their raceIdx.
     * @param entityId Id of the entity sending the message.
     */
    public StableMessage(StableMessageTypes method,
                         double[] raceOdds, int entityId) {
        this.method = method.getId();
        this.raceOdds = raceOdds;
        this.entityId = entityId;
    }

    /**
     * Constructor (type 5).
     * @param method Method an entity invokes on the shared region server.
     * @param raceID Id of the race.
     * @param raceIdx Horses' index on the race.
     * @param entityId Id of the entity sending the message.
     */
    public StableMessage(StableMessageTypes method, int raceID, int raceIdx,
                         int entityId) {
        this.method = method.getId();
        this.raceId = raceID;
        this.raceIdx = raceIdx;
        this.entityId = entityId;
    }

    /**
     * Constructor (type 6).
     * @param method Method an entity invokes on the shared region server.
     * @param raceID Id of the race.
     * @param agility Horses' agility value.
     * @param alreadyRun Indicates if the horse has already finished a race.
     * @param entityId Id of the entity sending the message.
     */
    public StableMessage(StableMessageTypes method, int raceID, int agility,
                         boolean alreadyRun, int entityId) {
        this.method = method.getId();
        this.raceId = raceID;
        this.agility = agility;
        this.alreadyRun = alreadyRun;
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
    public void setMethod(StableMessageTypes method) {
        this.method = method.getId();
    }

    /**
     * Method that returns an array of Horse/Jockey pair IDs that will
     * store the lineups for all races.
     * @return An array of Horse/Jockey pair IDs that will
     * store the lineups for all races.
     */
    public int[] getHorsesId() {
        return horsesId;
    }

    /**
     * Method that returns an array that stores the odds of each one of the
     * Horses participating in the current race, indexed by their raceIdx.
     * @return An array that stores the odds of each one of the Horses
     * participating in the current race, indexed by their raceIdx.
     */
    public double[] getRaceOdds() {
        return raceOdds;
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
     * Method that returns the Horses' index on the race which the current
     * message corresponds.
     * @return The Horses' index on the race which the current message corresponds.
     */
    public int getRaceIdx() {
        return raceIdx;
    }

    /**
     * Method that returns the Horses' agility value.
     * @return The Horses' agility value.
     */
    public int getAgility() {
        return agility;
    }

    /**
     * Method that returns true if Horse has already run a race.
     * @return True if Horse has already run a race.
     */
    public boolean isAlreadyRun() {
        return alreadyRun;
    }

    /**
     * Method that returns the identifier of the entity that intends to invoke
     * a method in the shared region.
     * @return The identifier of the entity that intends to invoke a method
     * in the shared region.
     */
    public int getEntityId() {
        return entityId;
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
        return "StableMessage{" +
                "method=" + method +
                ", raceIdx=" + raceIdx +
                ", agility=" + agility +
                ", raceId=" + raceId +
                ", horsesId=" + Arrays.toString(horsesId) +
                ", raceOdds=" + Arrays.toString(raceOdds) +
                ", entityId=" + entityId +
                '}';
    }
}

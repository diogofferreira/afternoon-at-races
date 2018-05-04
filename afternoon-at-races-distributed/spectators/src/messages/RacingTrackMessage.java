package messages;

import messageTypes.RacingTrackMessageTypes;

import java.io.Serializable;

/**
 * This data type defines a message sent to and from the Racing Track server.
 * It contains a variety of fields and constructors to adapt to the different
 * entities using it.
 */
public class RacingTrackMessage implements Serializable {

    /**
     * Serial version of the message.
     */
    private static final long serialVersionUID = 1005L;

    // method type
    /**
     * Identifier of the method that the entity pretends to invoke in the shared
     * region server.
     */
    private int method;

    // implicit info
    /**
     * Identifier of the current race.
     */
    private int raceID;

    /**
     * Horses' index on the race.
     */
    private int raceIdx;

    /**
     * Horse's agility value.
     */
    private int agility;

    /**
     * Position of the current Horse when he gets the finish line.
     */
    private int currentPosition;

    /**
     * Current Horses' position in the track.
     */
    private int currentStep;

    // arguments
    /**
     * Current Horses' step number.
     */
    private int step;

    // replies
    /**
     * Indicates if the Horses' has already crossed the finish line.
     */
    private boolean hasFinishLineBeenCrossed;

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
    public RacingTrackMessage(RacingTrackMessage inMessage, String errorMessage) {
        if (inMessage == null)
            throw new IllegalArgumentException("Invalid inMessage");
        if (errorMessage == null)
            throw new IllegalArgumentException("Invalid error description");

        this.method = RacingTrackMessageTypes.ERROR.getId();
        this.errorMessage = errorMessage;
        this.raceID = inMessage.getRaceID();
        this.raceIdx = inMessage.getRaceIdx();
        this.agility = inMessage.getAgility();
        this.currentPosition = inMessage.getCurrentPosition();
        this.currentStep = inMessage.getCurrentStep();
        this.step = inMessage.getStep();
        this.hasFinishLineBeenCrossed = inMessage.hasFinishLineBeenCrossed();
        this.entityId = inMessage.getEntityId();
    }

    /**
     * Constructor (type 1).
     * @param method Method an entity invokes on the shared region server.
     * @param entityId Id of the entity sending the message.
     */
    public RacingTrackMessage(RacingTrackMessageTypes method,
                              int entityId) {
        this.method = method.getId();
        this.entityId = entityId;
    }

    /**
     * Constructor (type 2).
     * @param method Method an entity invokes on the shared region server.
     * @param step Current Horses' step number.
     * @param entityId Id of the entity sending the message.
     */
    public RacingTrackMessage(RacingTrackMessageTypes method,
                              int step, int entityId) {
        this.method = method.getId();
        this.step = step;
        this.entityId = entityId;
    }

    /**
     * Constructor (type 3).
     * @param method Method an entity invokes on the shared region server.
     * @param hasFinishLineBeenCrossed Indicates if the Horses' has already
     *                                crossed the finish line.
     * @param entityId Id of the entity sending the message.
     */
    public RacingTrackMessage(RacingTrackMessageTypes method,
                              boolean hasFinishLineBeenCrossed, int entityId) {
        this.method = method.getId();
        this.hasFinishLineBeenCrossed = hasFinishLineBeenCrossed;
        this.entityId = entityId;
    }

    /**
     * Constructor (type 4).
     * @param method Method an entity invokes on the shared region server.
     * @param step Current Horses' step number.
     * @param raceID Identifier of the current race.
     * @param raceIdx Horses' index on the race.
     * @param agility Horse's agility value.
     * @param currentPosition Horses' position when he gets the finish line.
     * @param currentStep Current Horses' position in the track.
     * @param entityId Id of the entity sending the message.
     */
    public RacingTrackMessage(RacingTrackMessageTypes method, int step, int raceID,
                              int raceIdx, int agility, int currentPosition,
                              int currentStep, int entityId) {
        this.method = method.getId();
        this.step = step;
        this.raceID = raceID;
        this.raceIdx = raceIdx;
        this.agility = agility;
        this.currentPosition = currentPosition;
        this.currentStep = currentStep;
        this.entityId = entityId;
    }

    /**
     * Constructor (type 5).
     * @param method Method an entity invokes on the shared region server.
     * @param raceID Identifier of the current race.
     * @param raceIdx Horses' index on the race.
     * @param agility Horse's agility value.
     * @param currentPosition Horses' position when he gets the finish line.
     * @param currentStep Current Horses' position in the track.
     * @param entityId Id of the entity sending the message.
     */
    public RacingTrackMessage(RacingTrackMessageTypes method, int raceID,
                              int raceIdx, int agility, int currentPosition,
                              int currentStep, int entityId) {
        this.method = method.getId();
        this.raceID = raceID;
        this.raceIdx = raceIdx;
        this.agility = agility;
        this.currentPosition = currentPosition;
        this.currentStep = currentStep;
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
    public void setMethod(RacingTrackMessageTypes method) {
        this.method = method.getId();
    }

    /**
     * Method that returns the current Horses' step number.
     * @return The current Horses' step number.
     */
    public int getStep() {
        return step;
    }

    /**
     * Method that returns a value indicating if the Horses' has already
     * crossed the finish line.
     * @return True if the Horses' has already crossed the finish line, else False.
     */
    public boolean hasFinishLineBeenCrossed() {
        return hasFinishLineBeenCrossed;
    }

    /**
     * Method that returns the race identifier which the current
     * message corresponds.
     * @return The race identifier which the current message corresponds.
     */
    public int getRaceID() {
        return raceID;
    }

    /**
     * Method that returns the Horses' index on the race which the current
     * message corresponds.
     * @return The Horses' index on the race which the current message
     * corresponds.
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
     * Method that returns the position of the current Horse when he gets
     * the finish line.
     * @return The position of the current Horse when he gets
     * the finish line.
     */
    public int getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Method that returns the current Horses' position in the track.
     * @return The current Horses' position in the track.
     */
    public int getCurrentStep() {
        return currentStep;
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
        return "RacingTrackMessage{" +
                "method=" + method +
                ", raceID=" + raceID +
                ", raceIdx=" + raceIdx +
                ", agility=" + agility +
                ", currentPosition=" + currentPosition +
                ", currentStep=" + currentStep +
                ", step=" + step +
                ", hasFinishLineBeenCrossed=" + hasFinishLineBeenCrossed +
                ", entityId=" + entityId +
                '}';
    }
}

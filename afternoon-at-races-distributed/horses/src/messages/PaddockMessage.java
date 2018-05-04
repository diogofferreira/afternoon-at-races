package messages;

import messageTypes.PaddockMessageTypes;

import java.io.Serializable;

/**
 * This data type defines a message sent to and from the Paddock server.
 * It contains a variety of fields and constructors to adapt to the different
 * entities using it.
 */
public class PaddockMessage implements Serializable {

    /**
     * Serial version of the message.
     */
    private static final long serialVersionUID = 1004L;

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
     * Index of a horse in the specified race.
     */
    private int raceIdx;

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
    public PaddockMessage(PaddockMessage inMessage, String errorMessage) {
        if (inMessage == null)
            throw new IllegalArgumentException("Invalid inMessage");
        if (errorMessage == null)
            throw new IllegalArgumentException("Invalid error description");

        this.method = PaddockMessageTypes.ERROR.getId();
        this.errorMessage = errorMessage;
        this.raceId = inMessage.getRaceID();
        this.raceIdx = inMessage.getRaceIdx();
        this.entityId = inMessage.getEntityId();
    }

    /**
     * Constructor (type 1).
     * @param method Method an entity invokes on the shared region server.
     * @param entityId Id of the entity sending the message.
     */
    public PaddockMessage(PaddockMessageTypes method,
                          int entityId) {
        this.method = method.getId();
        this.entityId = entityId;
    }

    /**
     * Constructor (type 2).
     * @param method Method an entity invokes on the shared region server.
     * @param raceId Id of the race.
     * @param raceIdx Index of a horse in the specified race.
     * @param entityId Id of the entity sending the message.
     */
    public PaddockMessage(PaddockMessageTypes method, int raceId,
                          int raceIdx, int entityId) {
        this.method = method.getId();
        this.raceId = raceId;
        this.raceIdx = raceIdx;
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
    public void setMethod(PaddockMessageTypes method) {
        this.method = method.getId();
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
     * Method that returns the race identifier which the current
     * message corresponds.
     * @return The race identifier which the current message corresponds.
     */
    public int getRaceID() {
        return raceId;
    }

    /**
     * Method that returns the index of a horse in the specified race
     * of the message.
     * @return the index of a horse in the specified race of the message.
     */
    public int getRaceIdx() {
        return raceIdx;
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
        return "PaddockMessage{" +
                "method=" + method +
                ", raceID=" + raceId +
                ", raceIdx=" + raceIdx +
                ", entityId=" + entityId +
                '}';
    }
}

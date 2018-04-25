package messages;

import messageTypes.PaddockMessageTypes;

import java.io.Serializable;

public class PaddockMessage implements Serializable {
    // method type
    private int method;

    // implicit info
    private int raceID;
    private int raceIdx;

    // entity id
    private int entityId;

    public PaddockMessage(PaddockMessageTypes error) {
        this.method = error.getId();
    }

    public PaddockMessage(PaddockMessageTypes method,
                          int entityId) {
        this.method = method.getId();
        this.entityId = entityId;
    }

    public PaddockMessage(PaddockMessageTypes method, int raceID,
                          int raceIdx, int entityId) {
        this.method = method.getId();
        this.raceID = raceID;
        this.raceIdx = raceIdx;
        this.entityId = entityId;
    }

    public int getMethod() {
        return method;
    }

    public int getEntityId() {
        return entityId;
    }

    public int getRaceID() {
        return raceID;
    }

    public int getRaceIdx() {
        return raceIdx;
    }
}

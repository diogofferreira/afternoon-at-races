package messages;

import messageTypes.PaddockMessageTypes;

import java.io.Serializable;

public class PaddockMessage implements Serializable {
    // method type
    private int method;

    // entity id
    private int entityId;

    public PaddockMessage(PaddockMessageTypes method,
                          int entityId) {
        this.method = method.getId();
        this.entityId = entityId;
    }

    public int getMethod() {
        return method;
    }

    public int getEntityId() {
        return entityId;
    }
}

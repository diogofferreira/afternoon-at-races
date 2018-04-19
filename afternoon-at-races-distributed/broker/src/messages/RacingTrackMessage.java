package messages;

import messageTypes.RacingTrackMessageTypes;

import java.io.Serializable;

public class RacingTrackMessage implements Serializable {
    // method type
    private int method;

    // arguments
    private int step;

    // replies
    private boolean hasFinishLineBeenCrossed;

    // entity id
    private int entityId;

    public RacingTrackMessage(RacingTrackMessageTypes method,
                              int entityId) {
        this.method = method.getId();
        this.entityId = entityId;
    }

    public RacingTrackMessage(RacingTrackMessageTypes method,
                              int step, int entityId) {
        this.method = method.getId();
        this.step = step;
        this.entityId = entityId;
    }

    public RacingTrackMessage(RacingTrackMessageTypes method,
                              boolean hasFinishLineBeenCrossed, int entityId) {
        this.method = method.getId();
        this.hasFinishLineBeenCrossed = hasFinishLineBeenCrossed;
        this.entityId = entityId;
    }

    public int getMethod() {
        return method;
    }

    public int getStep() {
        return step;
    }

    public boolean isHasFinishLineBeenCrossed() {
        return hasFinishLineBeenCrossed;
    }

    public int getEntityId() {
        return entityId;
    }
}

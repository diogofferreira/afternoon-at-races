package messages;

import messageTypes.RacingTrackMessageTypes;

import java.io.Serializable;

public class RacingTrackMessage implements Serializable {
    // method type
    private int method;

    // implicit info
    private int raceID;
    private int raceIdx;
    private int agility;
    private int currentPosition;
    private int currentStep;

    // arguments
    private int step;

    // replies
    private boolean hasFinishLineBeenCrossed;

    // entity id
    private int entityId;

    public RacingTrackMessage(RacingTrackMessageTypes error) {
        this.method = error.getId();
    }

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

    public int getMethod() {
        return method;
    }

    public int getStep() {
        return step;
    }

    public boolean hasFinishLineBeenCrossed() {
        return hasFinishLineBeenCrossed;
    }

    public int getRaceID() {
        return raceID;
    }

    public int getRaceIdx() {
        return raceIdx;
    }

    public int getAgility() {
        return agility;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public int getEntityId() {
        return entityId;
    }
}

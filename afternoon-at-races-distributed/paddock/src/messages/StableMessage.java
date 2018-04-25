package messages;

import messageTypes.StableMessageTypes;

import java.io.Serializable;
import java.util.Arrays;

public class StableMessage implements Serializable {

    private static final long serialVersionUID = 1006L;

    // method type
    private int method;

    // implicit info
    private int raceID;
    private int raceIdx;
    private int agility;

    // arguments (raceID)
    private int raceId;
    private int[] horsesId;

    // replies
    private int[][] horsesAgility;
    private double[] raceOdds;

    // entity id
    private int entityId;

    public StableMessage(StableMessageTypes error) {
        this.method = error.getId();
    }

    public StableMessage(StableMessageTypes method,
                         int entityId) {
        this.method = method.getId();
        this.entityId = entityId;
    }

    public StableMessage(StableMessageTypes method,
                         int[] horsesId, int entityId) {
        this.method = method.getId();
        this.horsesId = horsesId;
        this.entityId = entityId;
    }

    public StableMessage(StableMessageTypes method,
                         int raceIdOrAgility, int entityId) {
        this.method = method.getId();
        if (method == StableMessageTypes.PROCEED_TO_STABLE)
            this.agility = raceIdOrAgility;
        else
            this.raceId = raceIdOrAgility;
        this.entityId = entityId;
    }

    public StableMessage(StableMessageTypes method,
                         double[] raceOdds, int entityId) {
        this.method = method.getId();
        this.raceOdds = raceOdds;
        this.entityId = entityId;
    }

    public StableMessage(StableMessageTypes method, int raceID, int raceIdx,
                         int entityId) {
        this.method = method.getId();
        this.raceID = raceID;
        this.raceIdx = raceIdx;
        this.entityId = entityId;
    }

    public int getMethod() {
        return method;
    }

    public int getRaceId() {
        return raceId;
    }

    public int[] getHorsesId() {
        return horsesId;
    }

    public int[][] getHorsesAgility() {
        return horsesAgility;
    }

    public double[] getRaceOdds() {
        return raceOdds;
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

    public int getEntityId() {
        return entityId;
    }

    @Override
    public String toString() {
        return "StableMessage{" +
                "method=" + method +
                ", raceID=" + raceID +
                ", raceIdx=" + raceIdx +
                ", agility=" + agility +
                ", raceId=" + raceId +
                ", horsesId=" + Arrays.toString(horsesId) +
                ", horsesAgility=" + Arrays.toString(horsesAgility) +
                ", raceOdds=" + Arrays.toString(raceOdds) +
                ", entityId=" + entityId +
                '}';
    }
}

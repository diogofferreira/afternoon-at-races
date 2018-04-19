package messages;

import messageTypes.StableMessageTypes;

import java.io.Serializable;

public class StableMessage implements Serializable {
    // method type
    private int method;

    // arguments (raceID)
    private int raceId;
    private int[] horsesId;

    // replies
    private int[][] horsesAgility;
    private double[] raceOdds;

    // entity id
    private int entityId;

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
                         int raceId, int entityId) {
        this.method = method.getId();
        this.raceId = raceId;
        this.entityId = entityId;
    }

    public StableMessage(StableMessageTypes method,
                         double[] raceOdds, int entityId) {
        this.method = method.getId();
        this.raceOdds = raceOdds;
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

    public int getEntityId() {
        return entityId;
    }
}

package messages;

import java.io.Serializable;

public class StableMessage implements Serializable {
    // method type
    private int method;
    // arguments (raceID)
    private int args;
    // reply (getRaceOdds)
    private double[] reply;
    // entity id
    private int entityId;
    // entity state
    private int entityState;
}

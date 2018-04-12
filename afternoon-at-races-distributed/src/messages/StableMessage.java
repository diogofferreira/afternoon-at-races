package messages;

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
}

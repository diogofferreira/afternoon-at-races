package messages;

import java.io.Serializable;

public class GeneralRepositoryMessage implements Serializable {
    // method type
    private int method;

    // arguments
    private int raceId;
    private int raceNumber;
    private int horseIdx;
    private int horsePosition;
    private int horseStep;
    private int spectatorBet;
    private int spectatorBettedHorse;
    private int[] standings;
    private double[] horsesOdd;

    // entity id
    private int entityId;
}

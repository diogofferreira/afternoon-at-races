package messages;

import java.io.Serializable;

public class ControlCentreMessage implements Serializable {
    // method type
    private int method;

    // arguments
    private int raceId;
    private int[] standings;
    private int horseIdx;

    // replies
    private boolean isThereARace;
    private boolean haceIWon;
    private int [] winners;

    // entity id
    private int entityId;

    // entity state
    private int entityState;
}

package messages;

import java.io.Serializable;

public class RacingTrack implements Serializable {
    // method type
    private int method;
    // arguments (step)
    private int args;
    // reply (hasFinishLineBeenCrossed)
    private boolean reply;
    // entity id
    private int entityId;
    // entity state
    private int entityState;
}

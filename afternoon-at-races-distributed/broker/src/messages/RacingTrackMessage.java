package messages;

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
}

package messages;

import java.io.Serializable;

public class ControlCentreMessage implements Serializable {
    // method type
    private int method;
    // arguments ([]finishTheRace | horseIdx | raceID)
    private int[] args;
    // reply (bool haveIWon | []winners | bool waitForNextRace)
    private int[] reply;
    // entity id
    private int entityId;
    // entity state
    private int entityState;
}

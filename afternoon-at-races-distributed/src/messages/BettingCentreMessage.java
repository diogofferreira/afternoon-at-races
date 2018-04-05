package messages;

import java.io.Serializable;

public class BettingCentreMessage implements Serializable {
    // method type
    private int method;
    // arguments (raceID | []winners | spectatorID)
    private int[] args;
    // reply (bool areThereAnyWinners | double collectTheGains | int placeABet)
    private double reply;
    // entity id
    private int entityId;
    // entity state
    private int entityState;
}

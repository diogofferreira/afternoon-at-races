package messages;

import java.io.Serializable;

public class BettingCentreMessage implements Serializable {
    // method type
    private int method;

    // arguments
    private int raceId;
    private int spectatorId;
    private int[] winner;

    // replies
    private boolean areThereAnyWinners;
    private double winningValue;
    private int bettedHorse;

    // entity id
    private int entityId;

    // acceptTheBets method
    public BettingCentreMessage(int raceId) {
        this.raceId = raceId;
        this.method = 1;
    }

    // placeABet method
    public BettingCentreMessage() {
    }

    public int getMethod() {
        return method;
    }

    public int getRaceId() {
        return raceId;
    }

    public int getSpectatorId() {
        return spectatorId;
    }

    public int[] getWinner() {
        return winner;
    }

    public boolean isAreThereAnyWinners() {
        return areThereAnyWinners;
    }

    public double getWinningValue() {
        return winningValue;
    }

    public int getBettedHorse() {
        return bettedHorse;
    }

    public int getEntityId() {
        return entityId;
    }
}

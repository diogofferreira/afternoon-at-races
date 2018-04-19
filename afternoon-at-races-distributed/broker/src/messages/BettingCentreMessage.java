package messages;

import messageTypes.BettingCentreMessageTypes;

import java.io.Serializable;

public class BettingCentreMessage implements Serializable {
    // method type
    private int method;

    // arguments
    private int raceId;
    private int spectatorId;
    private int[] winners;

    // replies
    private boolean areThereAnyWinners;
    private double winningValue;
    private int bettedHorse;

    // entity id
    private int entityId;

    public BettingCentreMessage(BettingCentreMessageTypes method,
                                int entityId) {
        this.method = method.getId();
        this.entityId = entityId;
    }

    public BettingCentreMessage(BettingCentreMessageTypes method,
                                int raceIdOrBettedHorse, int entityId) {
        this.method = method.getId();
        if (this.method == 0)
            this.raceId = raceIdOrBettedHorse;
        else
            this.bettedHorse = raceIdOrBettedHorse;
        this.entityId = entityId;
    }

    public BettingCentreMessage(BettingCentreMessageTypes method,
                                int[] winners, int entityId) {
        this.method = method.getId();
        this.winners = winners;
        this.entityId = entityId;
    }

    public BettingCentreMessage(BettingCentreMessageTypes method,
                                boolean areThereAnyWinners, int entityId) {
        this.method = method.getId();
        this.areThereAnyWinners = areThereAnyWinners;
        this.entityId = entityId;
    }

    public BettingCentreMessage(BettingCentreMessageTypes method,
                                double winningValue, int entityId) {
        this.method = method.getId();
        this.winningValue = winningValue;
        this.entityId = entityId;
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

    public int[] getWinners() {
        return winners;
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

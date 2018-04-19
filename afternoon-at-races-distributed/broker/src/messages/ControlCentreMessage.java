package messages;

import messageTypes.ControlCentreMessageTypes;

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
    private boolean haveIWon;
    private int[] winners;

    // entity id
    private int entityId;

    public ControlCentreMessage(ControlCentreMessageTypes error) {
        this.method = error.getId();
    }

    public ControlCentreMessage(ControlCentreMessageTypes method,
                                int entityId) {
        this.method = method.getId();
        this.entityId = entityId;
    }

    public ControlCentreMessage(ControlCentreMessageTypes method,
                                int raceIdOrHorseIdx, int entityId) {
        this.method = method.getId();
        if (this.method == 0)
            this.raceId = raceIdOrHorseIdx;
        else
            this.horseIdx = raceIdOrHorseIdx;
        this.entityId = entityId;
    }

    public ControlCentreMessage(ControlCentreMessageTypes method,
                                boolean isThereARaceOrHaveIWon, int entityId) {
        this.method = method.getId();
        if (this.method == 1)
            this.isThereARace = isThereARaceOrHaveIWon;
        else
            this.haveIWon = isThereARaceOrHaveIWon;
        this.entityId = entityId;
    }


    public ControlCentreMessage(ControlCentreMessageTypes method,
                                int[] standingsOrWinners, int entityId) {
        this.method = method.getId();
        if (this.method == 6)
            this.standings = standingsOrWinners;
        else
            this.winners = standingsOrWinners;
        this.entityId = entityId;
    }

    public int getMethod() {
        return method;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public int getRaceId() {
        return raceId;
    }

    public void setRaceId(int raceId) {
        this.raceId = raceId;
    }

    public int[] getStandings() {
        return standings;
    }

    public void setStandings(int[] standings) {
        this.standings = standings;
    }

    public int getHorseIdx() {
        return horseIdx;
    }

    public void setHorseIdx(int horseIdx) {
        this.horseIdx = horseIdx;
    }

    public boolean isThereARace() {
        return isThereARace;
    }

    public void setThereARace(boolean thereARace) {
        isThereARace = thereARace;
    }

    public boolean isHaveIWon() {
        return haveIWon;
    }

    public void setHaveIWon(boolean haceIWon) {
        this.haveIWon = haceIWon;
    }

    public int[] getWinners() {
        return winners;
    }

    public void setWinners(int[] winners) {
        this.winners = winners;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }
}

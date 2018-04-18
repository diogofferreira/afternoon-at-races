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
    private boolean haceIWon;
    private int[] winners;

    // entity id
    private int entityId;

    // entity state
    private int entityState;

    public ControlCentreMessage(ControlCentreMessageTypes method, int raceId) {
        this.method = method.getId();
        this.raceId = raceId;
        this.entityId = 0; // broker only
    }

    public ControlCentreMessage(ControlCentreMessageTypes method) {
        this.method = method.getId();
        this.entityId = 0; // broker only
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

    public boolean isHaceIWon() {
        return haceIWon;
    }

    public void setHaceIWon(boolean haceIWon) {
        this.haceIWon = haceIWon;
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

    public int getEntityState() {
        return entityState;
    }

    public void setEntityState(int entityState) {
        this.entityState = entityState;
    }
}

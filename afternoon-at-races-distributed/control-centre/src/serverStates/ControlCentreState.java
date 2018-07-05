package serverStates;

import main.EventVariables;
import messageTypes.ControlCentreMessageTypes;

import java.util.Arrays;

/**
 * Definition of the Control Centre states.
 */
public class ControlCentreState {
    private int entityId;

    private ControlCentreMessageTypes mType;

    private int raceNumber;

    private boolean isThereARace;

    private int[] winners;

    private boolean haveIWon;

    public ControlCentreState(int entityId, ControlCentreMessageTypes mType, int raceNumber) {
        if (entityId < 0 || raceNumber > EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Illegal arguments");

        this.entityId = entityId;
        this.mType = mType;
        this.raceNumber = raceNumber;
    }

    public ControlCentreState(String info) {
        if (info == null || info.length() < 1)
            throw new IllegalArgumentException("Illegal info string");

        try {
            String[] args = info.split("\\|");

            this.entityId = Integer.parseInt(args[0]);
            this.mType = ControlCentreMessageTypes.getType(Integer.parseInt(args[1]));
            this.raceNumber = Integer.parseInt(args[2]);
            this.isThereARace = Boolean.parseBoolean(args[3]);

            String[] w = args[4].equals("null") ? null :
                    args[4].substring(1, args[4].length()-1).split(",");
            if (w != null) {
                this.winners = new int[w.length];
                for (int i = 0; i < w.length; i++)
                    this.winners[i] = Integer.parseInt(w[i].trim());
            }

            this.haveIWon = Boolean.parseBoolean(args[5]);

        } catch (Exception e) {
            System.err.println("Invalid info arguments");
            System.exit(1);
        }

    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public ControlCentreMessageTypes getmType() {
        return mType;
    }

    public void setmType(ControlCentreMessageTypes mType) {
        this.mType = mType;
    }

    public int getRaceNumber() {
        return raceNumber;
    }

    public void setRaceNumber(int raceNumber) {
        this.raceNumber = raceNumber;
    }

    public boolean isThereARace() {
        return isThereARace;
    }

    public void setThereARace(boolean thereARace) {
        isThereARace = thereARace;
    }

    public int[] getWinners() {
        return winners;
    }

    public void setWinners(int[] winners) {
        this.winners = winners;
    }

    public boolean isHaveIWon() {
        return haveIWon;
    }

    public void setHaveIWon(boolean haveIWon) {
        this.haveIWon = haveIWon;
    }

    @Override
    public String toString() {
        int mId = mType == null ? -1 : mType.getId();
        return entityId + "|" + mId + "|" + raceNumber + "|" + isThereARace
                + "|" + Arrays.toString(winners) + "|" +  haveIWon;
    }
}
package utils;

public class Bet {
    private int spectatorID;
    private int horseID;
    private int value;

    public Bet(int spectatorID, int horseID, int value) {
        assert spectatorID >= 0 && horseID >= 0 && value >= 0;
        this.spectatorID = spectatorID;
        this.horseID = horseID;
        this.value = value;
    }

    public int getSpectatorID() {
        return spectatorID;
    }

    public int getHorseID() {
        return horseID;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Bet{" +
                "spectatorID=" + spectatorID +
                ", horseID=" + horseID +
                ", value=" + value +
                '}';
    }
}

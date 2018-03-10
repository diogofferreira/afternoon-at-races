package utils;

public class Racer {
    private int id;
    private int currentPosition;

    public Racer(int id) {
        this.id = id;
        this.currentPosition = 0;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int step) {
        this.currentPosition += step;
    }
}

package utils;

public class Racer {
    private int id;
    private int currentPosition;
    private int currentStep;

    public Racer(int id) {
        this.id = id;
        this.currentPosition = 0;
        this.currentStep = 0;
    }

    public int getId() { return this.id; }

    public int getCurrentPosition() {
        return this.currentPosition;
    }

    public int getCurrentStep() {
        return this.currentStep;
    }

    public void setCurrentPosition(int step) {
        this.currentPosition += step;
        this.currentStep++;
    }
}

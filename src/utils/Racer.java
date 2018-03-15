package utils;

public class Racer {
    private int idx;
    private int currentPosition;
    private int currentStep;

    public Racer(int idx) {
        this.idx = idx;
        this.currentPosition = 0;
        this.currentStep = 0;
    }

    public int getIdx() { return this.idx; }

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

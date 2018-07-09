package serverStates;

public class ControlCentreState {

    private int methodId;

    private int conditionVariableId;

    /**
     * Flag that signals if the Spectators are still at the Paddock.
     */
    private boolean spectatorsInPaddock;

    /**
     * Flag that signals if the Spectators can proceed to the Paddock.
     */
    private boolean spectatorsCanProceed;

    /**
     * Flag that signals the Broker if that race has already finished.
     */
    private boolean raceFinished;

    /**
     * Flag that signals the Spectators waiting for the results of the race
     * to be announced.
     */
    private boolean reportsPosted;

    /**
     * Counter that increments each time a spectator is waken up by the
     * announcing of the race results.
     */
    private int spectatorsLeavingRace;

    /**
     * Flag that signals if the event has already ended;
     */
    private boolean eventEnded;

    public ControlCentreState(int methodId) {
        this.methodId = methodId;
    }

    public int getMethodId() {
        return methodId;
    }

    public void setMethodId(int methodId) {
        this.methodId = methodId;
    }

    public int getConditionVariableId() {
        return conditionVariableId;
    }

    public void setConditionVariableId(int conditionVariableId) {
        this.conditionVariableId = conditionVariableId;
    }

    public boolean isSpectatorsInPaddock() {
        return spectatorsInPaddock;
    }

    public void setSpectatorsInPaddock(boolean spectatorsInPaddock) {
        this.spectatorsInPaddock = spectatorsInPaddock;
    }

    public boolean isSpectatorsCanProceed() {
        return spectatorsCanProceed;
    }

    public void setSpectatorsCanProceed(boolean spectatorsCanProceed) {
        this.spectatorsCanProceed = spectatorsCanProceed;
    }

    public boolean isRaceFinished() {
        return raceFinished;
    }

    public void setRaceFinished(boolean raceFinished) {
        this.raceFinished = raceFinished;
    }

    public boolean isReportsPosted() {
        return reportsPosted;
    }

    public void setReportsPosted(boolean reportsPosted) {
        this.reportsPosted = reportsPosted;
    }

    public int getSpectatorsLeavingRace() {
        return spectatorsLeavingRace;
    }

    public void setSpectatorsLeavingRace(int spectatorsLeavingRace) {
        this.spectatorsLeavingRace = spectatorsLeavingRace;
    }

    public boolean isEventEnded() {
        return eventEnded;
    }

    public void setEventEnded(boolean eventEnded) {
        this.eventEnded = eventEnded;
    }
}

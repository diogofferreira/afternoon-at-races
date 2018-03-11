package main;

public class EventVariables {
    private static int numSpectators = 4;
    private static int numRaces = 5;
    private static int numHorsesPerRace = 4;
    private static int numHorses = numHorsesPerRace * numRaces;

    public static int getNumSpectators() {
        return numSpectators;
    }

    public static int getNumRaces() {
        return numRaces;
    }

    public static int getNumHorsesPerRace() {
        return numHorsesPerRace;
    }

    public static int getNumHorses() {
        assert numHorses >= numHorsesPerRace * numRaces;
        return numHorses;
    }
}

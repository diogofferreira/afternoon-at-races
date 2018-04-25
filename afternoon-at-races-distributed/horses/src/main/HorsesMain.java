package main;

import entities.Horse;
import stubs.PaddockStub;
import stubs.RacingTrackStub;
import stubs.StableStub;

import java.util.Random;

/**
 * Main class of the event.
 * It will start all horses entities.
 */
public class HorsesMain {

    /**
     * Main method.
     * @param args Runtime arguments.
     */
    public static void main (String [] args) {
        Random rnd;
        int agility;                            // agility of the horses
        StableStub stable;                      // instance of Stable
        PaddockStub paddock;                    // instance of Paddock
        RacingTrackStub racingTrack;            // instance of Racing Track

        // shared regions initialization
        /*stable = new StableStub("l040101-ws03.ua.pt",
                22403);
        paddock = new PaddockStub("l040101-ws04.ua.pt",
                22404);
        racingTrack = new RacingTrackStub("l040101-ws05.ua.pt",
                22405);*/

        stable = new StableStub("127.0.0.1",
                22403);
        paddock = new PaddockStub("127.0.0.1",
                22404);
        racingTrack = new RacingTrackStub("127.0.0.1",
                22405);

        // entities initialization
        Horse[] horses = new Horse[EventVariables.NUMBER_OF_HORSES];

        rnd = new Random();

        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES; i++) {
            agility = rnd.nextInt(EventVariables.HORSE_MAX_STEP) + 1;
            horses[i] = new Horse(i, agility, stable, paddock, racingTrack);
        }

        // start of the simulation
        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES; i++)
            horses[i].start();

        // end of the simulation
        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES; i++) {
            try {
                horses[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

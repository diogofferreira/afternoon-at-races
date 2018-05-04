package main;

import communication.HostsInfo;
import entities.Horse;
import stubs.PaddockStub;
import stubs.RacingTrackStub;
import stubs.StableStub;

import javax.swing.plaf.basic.BasicScrollPaneUI;
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
        stable = new StableStub(
                HostsInfo.STABLE_HOSTNAME,
                HostsInfo.STABLE_PORT);
        paddock = new PaddockStub(
                HostsInfo.PADDOCK_HOSTNAME,
                HostsInfo.PADDOCK_PORT);
        racingTrack = new RacingTrackStub(
                HostsInfo.RACING_TRACK_HOSTNAME,
                HostsInfo.RACING_TRACK_PORT);

        /*stable = new StableStub("127.0.0.1",
                22402);
        paddock = new PaddockStub("127.0.0.1",
                22404);
        racingTrack = new RacingTrackStub("127.0.0.1",
                22405);*/

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

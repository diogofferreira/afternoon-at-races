package main;

import entities.Broker;
import stubs.BettingCentreStub;
import stubs.ControlCentreStub;
import stubs.RacingTrackStub;
import stubs.StableStub;

/**
 * Main class of the event.
 * It will start the broker entity.
 */
public class BrokerMain {

    /**
     * Main method.
     * @param args Runtime arguments.
     */
    public static void main (String [] args) {
        StableStub stable;                          // instance of Stable
        RacingTrackStub racingTrack;                // instance of Racing Track
        ControlCentreStub controlCentre;            // instance of Control Centre
        BettingCentreStub bettingCentre;            // instance of Betting Centre

        /**
         * LOCATIONS
         * CC - ws01
         * GR - ws02
         * ST - ws03
         * PA - ws04
         * RT - ws05
         * BC - ws06
         * B  - ws07
         * H  - ws08
         * S  - ws09
         */

        // shared regions initialization
        controlCentre = new ControlCentreStub("l040101-ws01.ua.pt",
                22401);
        stable = new StableStub("l040101-ws03.ua.pt",
                22403);
        racingTrack = new RacingTrackStub("l040101-ws05.ua.pt",
                22405);
        bettingCentre = new BettingCentreStub("l040101-ws06.ua.pt",
                22406);

        // entities initialization
        Broker broker = new Broker(stable, racingTrack, controlCentre, bettingCentre);

        // start of the simulation
        broker.start();

        // end of the simulation
        try {
            broker.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

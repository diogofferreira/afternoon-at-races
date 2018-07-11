package main;

import communication.HostsInfo;
import entities.Broker;
import stubs.BettingCentreStub;
import stubs.ControlCentreStub;
import stubs.RacingTrackStub;
import stubs.StableStub;

/**
 * Main class that starts the Broker entity server.
 */
public class BrokerMain {

    /**
     * Main method.
     * @param args Runtime arguments.
     */
    public static void main (String[] args) {
        StableStub stable;                          // instance of Stable
        RacingTrackStub racingTrack;                // instance of Racing Track
        ControlCentreStub controlCentre;            // instance of Control Centre
        BettingCentreStub bettingCentre;            // instance of Betting Centre
        int numExecs;

        numExecs = Integer.parseInt(args[0]);
        System.out.println("BROKEEER " + numExecs);
        /**
         * LOCATIONS
         * GR - ws01
         * ST - ws02
         * CC - ws03
         * PA - ws04
         * RT - ws05
         * BC - ws06
         * H  - ws07
         * S  - ws08
         * B  - ws09
         */

        // shared regions initialization
        stable = new StableStub(
                HostsInfo.STABLE_HOSTNAME,
                HostsInfo.STABLE_PORT, numExecs);
        controlCentre = new ControlCentreStub(
                HostsInfo.CONTROL_CENTRE_HOSTNAME,
                HostsInfo.CONTROL_CENTRE_PORT, numExecs);
        racingTrack = new RacingTrackStub(
                HostsInfo.RACING_TRACK_HOSTNAME,
                HostsInfo.RACING_TRACK_PORT);
        bettingCentre = new BettingCentreStub(
                HostsInfo.BETTING_CENTRE_HOSTNAME,
                HostsInfo.BETTING_CENTRE_PORT);
        /*stable = new StableStub("127.0.0.1",
                22402);
        controlCentre = new ControlCentreStub("127.0.0.1",
                22403);
        racingTrack = new RacingTrackStub("127.0.0.1",
                22405);
        bettingCentre = new BettingCentreStub("127.0.0.1",
                22406);*/

        // entities initialization
        Broker broker = new Broker(stable, racingTrack, controlCentre, bettingCentre, numExecs);

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

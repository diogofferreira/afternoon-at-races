package main;

import communication.HostsInfo;
import entities.Spectator;
import stubs.*;

/**
 * Main class that starts the Spectators entity server.
 */
public class SpectatorsMain {

    /**
     * Main method.
     * @param args Runtime arguments.
     */
    public static void main (String [] args) {
        PaddockStub paddock;                        // Instance of Paddock
        ControlCentreStub controlCentre;            // instance of Control Centre
        BettingCentreStub bettingCentre;            // instance of Betting Centre

        // shared regions initialization
        controlCentre = new ControlCentreStub(
                HostsInfo.CONTROL_CENTRE_HOSTNAME,
                HostsInfo.CONTROL_CENTRE_PORT);
        paddock = new PaddockStub(
                HostsInfo.PADDOCK_HOSTNAME,
                HostsInfo.PADDOCK_PORT);
        bettingCentre = new BettingCentreStub(
                HostsInfo.BETTING_CENTRE_HOSTNAME,
                HostsInfo.BETTING_CENTRE_PORT);

        /*controlCentre = new ControlCentreStub("127.0.0.1",
                22403);
        paddock = new PaddockStub("127.0.0.1",
                22404);
        bettingCentre = new BettingCentreStub("127.0.0.1",
                22406);*/

        // entities initialization
        Spectator[] spectators = new Spectator[EventVariables.NUMBER_OF_SPECTATORS];

        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++) {
            spectators[i] = new Spectator(i, EventVariables.INITIAL_WALLET, i,
                    paddock, controlCentre, bettingCentre);
        }

        // start of the simulation
        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++)
            spectators[i].start();

        // end of the simulation
        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++) {
            try {
                spectators[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

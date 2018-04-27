package main;

import entities.Spectator;
import stubs.*;

/**
 * Main class of the event.
 * It will start the broker entity.
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
        controlCentre = new ControlCentreStub("l040101-ws03.ua.pt",
                22403);
        paddock = new PaddockStub("l040101-ws04.ua.pt",
                22404);
        bettingCentre = new BettingCentreStub("l040101-ws06.ua.pt",
                22406);/*
        controlCentre = new ControlCentreStub("127.0.0.1",
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

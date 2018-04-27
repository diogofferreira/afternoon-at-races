package main;

import communication.ControlCentreAPS;
import communication.ServerCom;
import sharedRegions.*;
import sharedRegions.ControlCentreInterface;
import stubs.GeneralRepositoryStub;
import stubs.StableStub;

/**
 * Main class of the event.
 * It will start the control centre region.
 */
public class ControlCentreMain {

    /**
     * Main method.
     * @param args Runtime arguments.
     */
    public static void main (String [] args) {
        ControlCentre controlCentre;
        ControlCentreInterface controlCentreInterface;
        ControlCentreAPS controlCentreAPS;

        GeneralRepositoryStub generalRepository;
        StableStub stable;
        ServerCom scom, scomi;

        // shared regions stub initialization
        generalRepository = new GeneralRepositoryStub("l040101-ws01.ua.pt",
                22401);
        stable = new StableStub("l040101-ws02.ua.pt",
                22402);/*
        generalRepository = new GeneralRepositoryStub("127.0.0.1",
                22401);
        stable = new StableStub("127.0.0.1",
                22402);*/

        // service establishment
        scom = new ServerCom(22403);
        scom.start();

        // shared region initialization
        controlCentre = new ControlCentre(generalRepository, stable);
        controlCentreInterface = new ControlCentreInterface(controlCentre);

        // request processing
        while(controlCentreInterface.getRequests() != EventVariables.NUMBER_OF_SPECTATORS) {
            scomi = scom.accept();
            if (scomi != null) {
                controlCentreAPS = new ControlCentreAPS(scomi, controlCentreInterface);
                controlCentreAPS.start();
            }
        }
    }
}
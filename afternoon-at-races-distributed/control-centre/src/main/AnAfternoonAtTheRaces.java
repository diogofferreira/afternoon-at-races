package main;

import communication.ControlCentreAPS;
import communication.ServerCom;
import sharedRegions.*;
import sharedRegionsInterfaces.ControlCentreInterface;
import stubs.GeneralRepositoryStub;
import stubs.StableStub;

/**
 * Main class of the event.
 * It will start all shared regions and threads (active entities).
 */
public class AnAfternoonAtTheRaces {

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
        generalRepository = new GeneralRepositoryStub("l040101-ws02.ua.pt",
                22402);
        stable = new StableStub("l040101-ws03.ua.pt",
                22403);

        // service establishment
        scom = new ServerCom(22401);
        scom.start();

        // shared region initialization
        controlCentre = new ControlCentre(generalRepository, stable);
        controlCentreInterface = new ControlCentreInterface(controlCentre);

        // request processing
        while(controlCentreInterface.getRequests() != EventVariables.NUMBER_OF_SPECTATORS) {
            scomi = scom.accept();
            controlCentreAPS = new ControlCentreAPS(scomi, controlCentreInterface);
            controlCentreAPS.start();
        }
    }
}
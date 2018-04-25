package main;

import communication.PaddockAPS;
import communication.ServerCom;
import sharedRegions.*;
import sharedRegions.PaddockInterface;
import stubs.ControlCentreStub;
import stubs.GeneralRepositoryStub;

/**
 * Main class of the event.
 * It will start the paddock region.
 */
public class PaddockMain {

    /**
     * Main method.
     * @param args Runtime arguments.
     */
    public static void main (String [] args) {
        Paddock paddock;
        PaddockInterface paddockInterface;
        PaddockAPS paddockAPS;

        ControlCentreStub controlCentre;
        GeneralRepositoryStub generalRepository;
        ServerCom scom, scomi;

        // shared regions stub initialization
        /*controlCentre = new ControlCentreStub("l040101-ws01.ua.pt",
                22401);
        generalRepository = new GeneralRepositoryStub("l040101-ws02.ua.pt",
                22402);*/

        controlCentre = new ControlCentreStub("127.0.0.1",
                22401);
        generalRepository = new GeneralRepositoryStub("127.0.0.1",
                22402);

        // service establishment
        scom = new ServerCom(22404);
        scom.start();

        // shared region initialization
        paddock = new Paddock(generalRepository, controlCentre);
        paddockInterface = new PaddockInterface(paddock);

        // request processing
        while(paddockInterface.getRequests() !=
                EventVariables.NUMBER_OF_HORSES +
                        (EventVariables.NUMBER_OF_SPECTATORS * EventVariables.NUMBER_OF_RACES)) {
            scomi = scom.accept();
            if (scomi != null) {
                paddockAPS = new PaddockAPS(scomi, paddockInterface);
                paddockAPS.start();
            }
        }
    }
}
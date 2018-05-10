package main;

import communication.HostsInfo;
import communication.PaddockAPS;
import communication.ServerCom;
import sharedRegions.*;
import sharedRegions.PaddockInterface;
import stubs.ControlCentreStub;
import stubs.GeneralRepositoryStub;

/**
 * Main class that starts the Paddock shared region server.
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
        generalRepository = new GeneralRepositoryStub(
                HostsInfo.GENERAL_REPOSITORY_HOSTNAME,
                HostsInfo.GENERAL_REPOSITORY_PORT);
        controlCentre = new ControlCentreStub(
                HostsInfo.CONTROL_CENTRE_HOSTNAME,
                HostsInfo.CONTROL_CENTRE_PORT);

        /*generalRepository = new GeneralRepositoryStub("127.0.0.1",
                22401);
        controlCentre = new ControlCentreStub("127.0.0.1",
                22403);*/

        // service establishment
        scom = new ServerCom(HostsInfo.PADDOCK_PORT);
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
package main;

import communication.ControlCentreAPS;
import communication.HostsInfo;
import communication.ServerCom;
import sharedRegions.*;
import sharedRegions.ControlCentreInterface;
import stubs.GeneralRepositoryStub;
import stubs.StableStub;

/**
 * Main class that starts the Control Centre shared region server.
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
        int numExecs;

        numExecs = Integer.parseInt(args[0]);
        System.out.println("CONTROL CENTRE " + numExecs);

        // shared regions stub initialization
        generalRepository = new GeneralRepositoryStub(
                HostsInfo.GENERAL_REPOSITORY_HOSTNAME,
                HostsInfo.GENERAL_REPOSITORY_PORT);
        stable = new StableStub(
                HostsInfo.STABLE_HOSTNAME,
                HostsInfo.STABLE_PORT);
        /*generalRepository = new GeneralRepositoryStub("127.0.0.1",
                22401);
        stable = new StableStub("127.0.0.1",
                22402);*/

        // service establishment
        scom = new ServerCom(HostsInfo.CONTROL_CENTRE_PORT);
        scom.start();

        // shared region initialization
        controlCentre = new ControlCentre(generalRepository, stable, numExecs);
        controlCentreInterface = new ControlCentreInterface(controlCentre);

        // request processing
        while(controlCentreInterface.getRequests() != EventVariables.NUMBER_OF_SPECTATORS) {
            scomi = scom.accept();
            if (scomi != null) {
                controlCentreAPS = new ControlCentreAPS(scomi, controlCentreInterface);
                controlCentreAPS.start();
            }
        }

        // delete previously created status file
        controlCentreInterface.deleteStatusFiles();
        controlCentre.deleteStatusFiles();
    }
}
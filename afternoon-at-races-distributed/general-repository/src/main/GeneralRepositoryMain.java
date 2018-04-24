package main;

import communication.GeneralRepositoryAPS;
import communication.ServerCom;
import sharedRegions.GeneralRepository;
import sharedRegionsInterfaces.GeneralRepositoryInterface;

/**
 * Main class of the event.
 * It will start the control centre region.
 */
public class GeneralRepositoryMain {

    /**
     * Main method.
     * @param args Runtime arguments.
     */
    public static void main (String [] args) {
        GeneralRepository generalRepository;
        GeneralRepositoryInterface generalRepositoryInterface;
        GeneralRepositoryAPS generalRepositoryAPS;

        ServerCom scom, scomi;

        // service establishment
        scom = new ServerCom(22402);
        scom.start();

        // shared region initialization
        generalRepository = new GeneralRepository();
        generalRepositoryInterface = new GeneralRepositoryInterface(generalRepository);

        // request processing
        while(generalRepositoryInterface.getRequests()
                != EventVariables.NUMBER_OF_HORSES_PER_RACE
                + EventVariables.NUMBER_OF_SPECTATORS) {
            scomi = scom.accept();
            generalRepositoryAPS = new GeneralRepositoryAPS(scomi, generalRepositoryInterface);
            generalRepositoryAPS.start();
        }
    }
}
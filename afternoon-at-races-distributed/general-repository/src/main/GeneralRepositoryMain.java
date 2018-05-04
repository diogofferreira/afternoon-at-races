package main;

import communication.GeneralRepositoryAPS;
import communication.HostsInfo;
import communication.ServerCom;
import sharedRegions.GeneralRepository;
import sharedRegions.GeneralRepositoryInterface;

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
        scom = new ServerCom(HostsInfo.GENERAL_REPOSITORY_PORT);
        scom.start();

        // shared region initialization
        generalRepository = new GeneralRepository();
        generalRepositoryInterface = new GeneralRepositoryInterface(generalRepository);

        // request processing
        while(generalRepositoryInterface.getRequests()
                != (EventVariables.NUMBER_OF_HORSES_PER_RACE * 2
                + EventVariables.NUMBER_OF_SPECTATORS + 1)) {
            scomi = scom.accept();
            if (scomi != null) {
                generalRepositoryAPS = new GeneralRepositoryAPS(scomi, generalRepositoryInterface);
                generalRepositoryAPS.start();
            }
        }
    }
}
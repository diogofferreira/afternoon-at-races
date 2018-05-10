package main;

import communication.HostsInfo;
import communication.ServerCom;
import communication.StableAPS;
import sharedRegions.*;
import sharedRegions.StableInterface;
import stubs.GeneralRepositoryStub;

/**
 * Main class that starts the Stable shared region server.
 */
public class StableMain {

    /**
     * Main method.
     * @param args Runtime arguments.
     */
    public static void main (String [] args) {
        int[] horsesIdx;                        // array of horses indexes
        Stable stable;
        StableInterface stableInterface;
        StableAPS stableAPS;

        GeneralRepositoryStub generalRepository;
        ServerCom scom, scomi;

        // shared regions stub initialization
        generalRepository = new GeneralRepositoryStub(
                HostsInfo.GENERAL_REPOSITORY_HOSTNAME,
                HostsInfo.GENERAL_REPOSITORY_PORT);
        /*generalRepository = new GeneralRepositoryStub("127.0.0.1",
                22401);*/

        // service establishment
        scom = new ServerCom(HostsInfo.STABLE_PORT);
        scom.start();

        // shared region initialization

        // generate races lineup (just placing all ids in an array to later be
        // shuffled at the stable)
        horsesIdx = new int[EventVariables.NUMBER_OF_HORSES];
        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES; i++)
            horsesIdx[i] = i;

        stable = new Stable(generalRepository, horsesIdx);
        stableInterface = new StableInterface(stable);

        // request processing
        while(stableInterface.getRequests() !=
                (EventVariables.NUMBER_OF_HORSES * 2 + 1)) {
            scomi = scom.accept();
            if (scomi != null) {
                stableAPS = new StableAPS(scomi, stableInterface);
                stableAPS.start();
            }
        }
    }
}
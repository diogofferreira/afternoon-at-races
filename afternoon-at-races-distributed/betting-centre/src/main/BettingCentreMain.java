package main;

import communication.BettingCentreAPS;
import communication.ServerCom;
import sharedRegions.*;
import sharedRegions.BettingCentreInterface;
import stubs.GeneralRepositoryStub;
import stubs.StableStub;

/**
 * Main class that starts the Betting Centre shared region server.
 */
public class BettingCentreMain {

    /**
     * Main method.
     * @param args Runtime arguments.
     */
    public static void main (String [] args) {
        BettingCentre bettingCentre;
        BettingCentreInterface bettingCentreInterface;
        BettingCentreAPS bettingCentreAPS;

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
        scom = new ServerCom(22406);
        scom.start();

        // shared region initialization
        bettingCentre = new BettingCentre(generalRepository, stable);
        bettingCentreInterface = new BettingCentreInterface(bettingCentre);

        // request processing
        while(bettingCentreInterface.getRequests()
                != bettingCentreInterface.getNumberOfWinners()) {
            scomi = scom.accept();
            if (scomi != null) {
                bettingCentreAPS = new BettingCentreAPS(scomi, bettingCentreInterface);
                bettingCentreAPS.start();
            }
        }
    }
}
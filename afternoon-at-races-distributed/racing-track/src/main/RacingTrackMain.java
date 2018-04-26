package main;

import communication.RacingTrackAPS;
import communication.ServerCom;
import sharedRegions.RacingTrack;
import sharedRegions.RacingTrackInterface;
import stubs.ControlCentreStub;
import stubs.GeneralRepositoryStub;

/**
 * Main class of the event.
 * It will start the racing track region.
 */
public class RacingTrackMain {

    /**
     * Main method.
     * @param args Runtime arguments.
     */
    public static void main (String [] args) {
        RacingTrack racingTrack;
        RacingTrackInterface racingTrackInterface;
        RacingTrackAPS racingTrackAPS;

        GeneralRepositoryStub generalRepository;
        ControlCentreStub controlCentre;
        ServerCom scom, scomi;

        // shared regions stub initialization
        /*
        generalRepository = new GeneralRepositoryStub("l040101-ws02.ua.pt",
                22401);
        controlCentre = new ControlCentreStub("l040101-ws01.ua.pt",
                22403);
        */

        generalRepository = new GeneralRepositoryStub("127.0.0.1",
                22401);
        controlCentre = new ControlCentreStub("127.0.0.1",
                22403);

        // service establishment
        scom = new ServerCom(22405);
        scom.start();

        // shared region initialization
        racingTrack = new RacingTrack(generalRepository, controlCentre);
        racingTrackInterface = new RacingTrackInterface(racingTrack);

        // request processing
        while(racingTrackInterface.getRequests() != EventVariables.NUMBER_OF_HORSES) {
            scomi = scom.accept();
            if (scomi != null) {
                racingTrackAPS = new RacingTrackAPS(scomi, racingTrackInterface);
                racingTrackAPS.start();
            }
        }
    }
}
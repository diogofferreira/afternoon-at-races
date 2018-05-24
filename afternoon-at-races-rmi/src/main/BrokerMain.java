package main;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import entities.Broker;
import interfaces.*;

/**
 * This data type instantiates an active entity, in this case the Broker,
 * which looks up for remote shared regions on Locate Registry and executes
 * their methods remotely.
 * Communication is based in Java RMI.
 */
public class BrokerMain {

    /**
     * Main task that instantiates an active entity.
     * It also instantiates the Locate Registry that has the RMI registrations
     * for the other shared regions, so that it can execute its methods remotely.
     */
    public static void main(String[] args) {
        Registry registry = null;
        StableInt stableStub = null;
        ControlCentreInt controlCentreStub = null;
        RacingTrackInt racingTrackStub = null;
        BettingCentreInt bettingCentreStub = null;

        try {
            registry = LocateRegistry.getRegistry(
                    HostsInfo.REGISTRY_HOSTNAME,
                    HostsInfo.REGISTRY_PORT);
        } catch (RemoteException e) {
            System.out.println("RMI registry creation exception: " +
                    e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("RMI registry was created!");

        try {
            stableStub = (StableInt) registry.lookup("Stable");
            controlCentreStub = (ControlCentreInt)
                    registry.lookup("ControlCentre");
            racingTrackStub = (RacingTrackInt)
                    registry.lookup("RacingTrack");
            bettingCentreStub = (BettingCentreInt)
                    registry.lookup("BettingCentre");
        } catch (RemoteException e) {
            System.out.println("Shared Region look up exception: " +
                    e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (NotBoundException e) {
            System.out.println("Shared Region not bound exception: " +
                    e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // entities initialization
        Broker broker = new Broker(stableStub, racingTrackStub,
                controlCentreStub, bettingCentreStub);

        // start of the simulation
        broker.start();

        // end of the simulation
        try {
            broker.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

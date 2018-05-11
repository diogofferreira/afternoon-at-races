package main;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import entities.Broker;
import entities.Spectator;
import interfaces.*;

/**
 * This data type instantiates and registers a remote object that will run mobile code.
 * Communication is based in Java RMI.
 */
public class BrokerMain {

    /**
     * Main task.
     */
    public static void main(String[] args) {
        Registry registry = null;
        StableInt stableStub = null;
        ControlCentreInt controlCentreStub = null;
        RacingTrackInt racingTrackStub = null;
        BettingCentreInt bettingCentreStub = null;

        /* create and install the security manager */
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
        System.out.println("Security manager was installed!");

        try {
            registry = LocateRegistry.getRegistry(
                    HostsInfo.GENERAL_REPOSITORY_HOSTNAME,
                    HostsInfo.GENERAL_REPOSITORY_PORT);
        } catch (RemoteException e) {
            System.out.println("RMI registry creation exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("RMI registry was created!");

        try {
            stableStub = (StableInt) registry.lookup("Stable");
            controlCentreStub = (ControlCentreInt) registry.lookup("ControlCentre");
            racingTrackStub = (RacingTrackInt) registry.lookup("RacingTrack");
            bettingCentreStub = (BettingCentreInt) registry.lookup("BettingCentre");
        } catch (RemoteException e) {
            System.out.println("Shared Region look up exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (NotBoundException e) {
            System.out.println("Shared Region not bound exception: " + e.getMessage());
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

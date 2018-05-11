package main;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import interfaces.*;
import sharedRegions.RacingTrack;

/**
 * This data type instantiates and registers a remote object that will run mobile code.
 * Communication is based in Java RMI.
 */
public class RacingTrackMain {

    /**
     * Main task.
     */
    public static void main(String[] args) {
        Registry registry = null;
        Register reg = null;
        RacingTrack racingTrack;
        RacingTrackInt racingTrackStub = null;
        GeneralRepositoryInt generalRepositoryStub = null;
        ControlCentreInt controlCentreStub = null;

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
            generalRepositoryStub = (GeneralRepositoryInt) registry.lookup("GeneralRepository");
            controlCentreStub = (ControlCentreInt) registry.lookup("ControlCentre");
        } catch (RemoteException e) {
            System.out.println("Shared Region look up exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (NotBoundException e) {
            System.out.println("Shared Region not bound exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        /* instantiate a remote object that runs mobile code and generate a stub for it */
        racingTrack = new RacingTrack(generalRepositoryStub, controlCentreStub);

        try {
            racingTrackStub =
                    (RacingTrackInt) UnicastRemoteObject.exportObject(
                            racingTrack, HostsInfo.RACING_TRACK_PORT);
        } catch (RemoteException e) {
            System.out.println("RacingTrack stub generation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Stub was generated!");

        /* register it with the general registry service */
        try {
            reg = (Register) registry.lookup("RegisterHandler");
        } catch (RemoteException e) {
            System.out.println("RegisterRemoteObject lookup exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (NotBoundException e) {
            System.out.println("RegisterRemoteObject not bound exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        try {
            reg.bind("RacingTrack", racingTrackStub);
        } catch (RemoteException e) {
            System.out.println("RacingTrack registration exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (AlreadyBoundException e) {
            System.out.println("RacingTrack already bound exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("RacingTrack object was registered!");
    }
}

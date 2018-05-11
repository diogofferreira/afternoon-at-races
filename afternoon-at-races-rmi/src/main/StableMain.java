package main;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import interfaces.GeneralRepositoryInt;
import interfaces.Register;
import interfaces.StableInt;
import sharedRegions.Stable;

/**
 * This data type instantiates and registers a remote object that will run mobile code.
 * Communication is based in Java RMI.
 */
public class StableMain {

    /**
     * Main task.
     */
    public static void main(String[] args) {
        Registry registry = null;
        Register reg = null;
        Stable stable;
        StableInt stableStub = null;
        GeneralRepositoryInt generalRepositoryStub = null;
        int[] horsesIdx;                        // array of horses indexes

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
        } catch (RemoteException e) {
            System.out.println("Shared Region look up exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (NotBoundException e) {
            System.out.println("Shared Region not bound exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // generate races lineup (just placing all ids in an array to later be
        // shuffled at the stable)
        horsesIdx = new int[EventVariables.NUMBER_OF_HORSES];
        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES; i++)
            horsesIdx[i] = i;

        /* instantiate a remote object that runs mobile code and generate a stub for it */
        stable = new Stable(generalRepositoryStub, horsesIdx);

        try {
            stableStub =
                    (Stable) UnicastRemoteObject.exportObject(
                            stable, HostsInfo.STABLE_PORT);
        } catch (RemoteException e) {
            System.out.println("Stable stub generation exception: "
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
            reg.bind("Stable", stableStub);
        } catch (RemoteException e) {
            System.out.println("Stable registration exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (AlreadyBoundException e) {
            System.out.println("Stable already bound exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Stable object was registered!");
    }
}

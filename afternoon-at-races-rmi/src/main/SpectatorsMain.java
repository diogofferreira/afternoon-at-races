package main;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import entities.Spectator;
import interfaces.*;

/**
 * This data type instantiates and registers a remote object that will run mobile code.
 * Communication is based in Java RMI.
 */
public class SpectatorsMain {

    /**
     * Main task.
     */
    public static void main(String[] args) {
        Registry registry = null;
        ControlCentreInt controlCentreStub = null;
        PaddockInt paddockStub = null;
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
            controlCentreStub = (ControlCentreInt) registry.lookup("ControlCentre");
            paddockStub = (PaddockInt) registry.lookup("Paddock");
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
        Spectator[] spectators = new Spectator[EventVariables.NUMBER_OF_SPECTATORS];

        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++) {
            spectators[i] = new Spectator(i, EventVariables.INITIAL_WALLET, i,
                    paddockStub, controlCentreStub, bettingCentreStub);
        }

        // start of the simulation
        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++)
            spectators[i].start();

        // end of the simulation
        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++) {
            try {
                spectators[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}

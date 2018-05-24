package main;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Random;

import entities.Horse;
import interfaces.*;

/**
 * This data type instantiates an active entity, in this case the Horse/Jockey pairs,
 * which looks up for remote shared regions on Locate Registry and executes
 * their methods remotely.
 * Communication is based in Java RMI.
 */
public class HorsesMain {

    /**
     * Main task that instantiates an active entity.
     * It also instantiates the Locate Registry that has the RMI registrations
     * for the other shared regions, so that it can execute its methods remotely.
     */
    public static void main(String[] args) {
        Registry registry = null;
        StableInt stableStub = null;
        PaddockInt paddockStub = null;
        RacingTrackInt racingTrackStub = null;
        Random rnd;
        int agility;

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
            paddockStub = (PaddockInt) registry.lookup("Paddock");
            racingTrackStub = (RacingTrackInt)
                    registry.lookup("RacingTrack");
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
        Horse[] horses = new Horse[EventVariables.NUMBER_OF_HORSES];

        rnd = new Random();

        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES; i++) {
            agility = rnd.nextInt(EventVariables.HORSE_MAX_STEP) + 1;
            horses[i] = new Horse(i, agility, stableStub, paddockStub,
                    racingTrackStub);
        }

        // start of the simulation
        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES; i++)
            horses[i].start();

        // end of the simulation
        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES; i++) {
            try {
                horses[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}

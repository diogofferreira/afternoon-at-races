package main;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import interfaces.ControlCentreInt;
import interfaces.GeneralRepositoryInt;
import interfaces.Register;
import interfaces.StableInt;
import sharedRegions.ControlCentre;

/**
 * This data type instantiates and registers a remote object that will run mobile code.
 * Communication is based in Java RMI.
 */
public class ControlCentreMain {

    /**
     * Main task.
     */
    public static void main(String[] args) {
        Registry registry = null;
        Register reg = null;
        ControlCentre controlCentre;
        ControlCentreInt controlCentreStub = null;
        GeneralRepositoryInt generalRepositoryStub = null;
        StableInt stableStub = null;

        /* create and install the security manager */
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
        System.out.println("Security manager was installed!");

        try {
            registry = LocateRegistry.getRegistry(
                    HostsInfo.REGISTRY_HOSTNAME,
                    HostsInfo.REGISTRY_PORT);
        } catch (RemoteException e) {
            System.out.println("RMI registry creation exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("RMI registry was created!");

        try {
            generalRepositoryStub = (GeneralRepositoryInt) registry.lookup("GeneralRepository");
            stableStub = (StableInt) registry.lookup("Stable");
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
        controlCentre = new ControlCentre(generalRepositoryStub, stableStub);

        try {
            controlCentreStub =
                    (ControlCentreInt) UnicastRemoteObject.exportObject(
                            controlCentre, HostsInfo.CONTROL_CENTRE_PORT);
        } catch (RemoteException e) {
            System.out.println("Control Centre stub generation exception: "
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
            reg.bind("ControlCentre", controlCentreStub);
        } catch (RemoteException e) {
            System.out.println("ControlCentre registration exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (AlreadyBoundException e) {
            System.out.println("ControlCentre already bound exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("ControlCentre object was registered!");
    }
}

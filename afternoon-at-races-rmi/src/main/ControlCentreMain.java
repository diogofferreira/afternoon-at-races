package main;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import interfaces.ControlCentreInt;
import interfaces.Register;
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
        /* get location of the registry service */

        String rmiRegHostName = HostsInfo.GENERAL_REPOSITORY_HOSTNAME;
        int rmiRegPortNumb = HostsInfo.GENERAL_REPOSITORY_PORT;

        /* create and install the security manager */
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
        System.out.println("Security manager was installed!");

        /* instantiate a remote object that runs mobile code and generate a stub for it */
        ControlCentre controlCentre = new ControlCentre();
        ControlCentreInt controlCentreStub = null;

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
        String nameEntryBase = "RegisterHandler";
        String nameEntryObject = "ControlCentre";
        Registry registry = null;
        Register reg = null;

        try {
            registry = LocateRegistry.getRegistry(rmiRegHostName, rmiRegPortNumb);
        } catch (RemoteException e) {
            System.out.println("RMI registry creation exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("RMI registry was created!");

        try {
            reg = (Register) registry.lookup(nameEntryBase);
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
            reg.bind(nameEntryObject, controlCentreStub);
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

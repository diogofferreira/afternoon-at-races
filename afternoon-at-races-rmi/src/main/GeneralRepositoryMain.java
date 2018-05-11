package main;

import interfaces.GeneralRepositoryInt;
import interfaces.Register;
import sharedRegions.GeneralRepository;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * This data type instantiates and registers a remote object that will run mobile code.
 * Communication is based in Java RMI.
 */
public class GeneralRepositoryMain {

    /**
     * Main task.
     */
    public static void main(String[] args) {
        Registry registry = null;
        Register reg = null;
        GeneralRepository generalRepository;
        GeneralRepositoryInt generalRepositoryStub = null;

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

        /* instantiate a remote object that runs mobile code and generate a stub for it */
        generalRepository = new GeneralRepository();

        try {
            generalRepositoryStub =
                    (GeneralRepositoryInt) UnicastRemoteObject.exportObject(
                            generalRepository, HostsInfo.GENERAL_REPOSITORY_PORT);
        } catch (RemoteException e) {
            System.out.println("General Repository stub generation exception: "
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
            reg.bind("GeneralRepository", generalRepositoryStub);
        } catch (RemoteException e) {
            System.out.println("GeneralRepository registration exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (AlreadyBoundException e) {
            System.out.println("GeneralRepository already bound exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("GeneralRepository object was registered!");
    }
}

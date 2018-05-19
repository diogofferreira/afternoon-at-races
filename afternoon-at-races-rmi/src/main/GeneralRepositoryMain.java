package main;

import interfaces.GeneralRepositoryInt;
import interfaces.Register;
import sharedRegions.GeneralRepository;

import java.rmi.AlreadyBoundException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This data type instantiates and registers a remote object, in this particular
 * case the General Repository shared region, that will run mobile code.
 * Communication is based in Java RMI.
 */
public class GeneralRepositoryMain {

    /**
     * Instance of a monitor.
     */
    private static Lock mutex;

    /**
     * Condition variable where the main thread waits until the shared region
     * ends its life cycle to unbind it in the registry.
     */
    private static Condition shutdown;

    /**
     * Boolean that is true if the shared region has already ended its execution.
     */
    private static boolean ended;

    /**
     * Main task that instantiates the remote object and its stub.
     * It also instantiates the Locate Registry that has the RMI registrations
     * for the other shared regions. If this shared region needs a stub of another,
     * it looks it up on the registry before instantiating its own remote object.
     * Finally it binds its stub on the Registry Handler so other shared regions
     * and/or clients can access its methods.
     * After its lifecyle ends, it unbinds the previous registration.
     */
    public static void main(String[] args) {
        Registry registry = null;
        Register reg = null;
        String objectName;
        GeneralRepository generalRepository;
        GeneralRepositoryInt generalRepositoryStub = null;
        mutex = new ReentrantLock();
        shutdown = mutex.newCondition();
        ended = false;

        /* create and install the security manager */
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
        System.out.println("Security manager was installed!");

        /* instantiate a remote object that runs mobile code and generate a stub for it */
        generalRepository = new GeneralRepository();
        objectName = "GeneralRepository";

        try {
            generalRepositoryStub =
                    (GeneralRepositoryInt) UnicastRemoteObject.exportObject(
                            generalRepository, HostsInfo.GENERAL_REPOSITORY_PORT);
        } catch (RemoteException e) {
            System.out.println(objectName + " stub generation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Stub was generated!");

        /* register it with the general registry service */
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
            reg = (Register) registry.lookup("RegisterHandler");
        } catch (RemoteException e) {
            System.out.println("RegisterRemoteObject lookup exception: " +
                    e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (NotBoundException e) {
            System.out.println("RegisterRemoteObject not bound exception: " +
                    e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        try {
            reg.bind(objectName, generalRepositoryStub);
        } catch (RemoteException e) {
            System.out.println(objectName + " registration exception: " +
                    e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (AlreadyBoundException e) {
            System.out.println(objectName + " already bound exception: " +
                    e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println(objectName + " object was registered!");

        mutex.lock();

        while (!ended) {
            try {
                shutdown.await();
            } catch (InterruptedException ignored) { }
        }

        mutex.unlock();

        try {
            reg.unbind(objectName);
        } catch (RemoteException e) {
            System.out.println(objectName + " unregistration exception: " +
                    e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (NotBoundException e) {
            System.out.println(objectName + " not bound exception: " +
                    e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println(objectName + " object was unregistered!");

        try {
            UnicastRemoteObject.unexportObject(generalRepositoryStub, true);
        } catch (NoSuchObjectException e) {
            System.out.println(objectName + " stub destruction exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Stub was destroyed!");
    }

    /**
     * Method that signals the main thread to unbind the shared region from the
     * registry.
     */
    public static void wakeUp() {
        mutex.lock();

        ended = true;
        shutdown.signal();

        mutex.unlock();
    }
}

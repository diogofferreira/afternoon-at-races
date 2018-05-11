package registry;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.AccessException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.*;

import interfaces.Register;
import main.HostsInfo;

/**
 * This data type instantiates and registers a remote object that enables
 * the registration of other remote objects
 * located in the same or other processing nodes in the local registry service.
 * Communication is based in Java RMI.
 */
public class ServerRegisterRemoteObject {

    /**
     * Main task.
     */
    public static void main(String[] args) {
        /* create and install the security manager */
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
        System.out.println("Security manager was installed!");

        /* instantiate a registration remote object and generate a stub for it */
        RegisterRemoteObject regEngine = new RegisterRemoteObject(
                HostsInfo.REGISTRY_HOSTNAME,
                HostsInfo.REGISTRY_PORT);
        Register regEngineStub = null;

        try {
            regEngineStub = (Register) UnicastRemoteObject.exportObject(
                    regEngine, HostsInfo.REGISTRY_PORT);
        } catch (RemoteException e) {
            System.out.println("RegisterRemoteObject stub generation exception: "
                    + e.getMessage());
            System.exit(1);
        }
        System.out.println("Stub was generated!");

        /* register it with the local registry service */
        Registry registry = null;

        try {
            registry = LocateRegistry.getRegistry(
                    HostsInfo.REGISTRY_HOSTNAME, HostsInfo.REGISTRY_PORT);
        } catch (RemoteException e) {
            System.out.println("RMI registry creation exception: "
                    + e.getMessage());
            System.exit(1);
        }
        System.out.println("RMI registry was created!");

        try {
            registry.rebind("RegisterHandler", regEngineStub);
        } catch (RemoteException e) {
            System.out.println("RegisterRemoteObject remote exception " +
                    "on registration: " + e.getMessage());
            System.exit(1);
        }
        System.out.println("RegisterRemoteObject object was registered!");
    }
}

package communication;

import java.io.*;
import java.net.*;

/**
 * This data type implements the client side communication channel, in order to
 * exchange messages over sockets using the TCP protocol.
 * The exchanged data type is based on objects.
 */
public class ClientCom {
    /**
     * Communication socket.
     *
     * @serialField commSocket
     */
    private Socket commSocket = null;

    /**
     * Host name of the computational system where the server is located.
     */
    private String serverHostName = null;

    /**
     * Port number where the server is listening.
     */
    private int serverPortNumb;

    /**
     * Communication channel input stream.
     */
    private ObjectInputStream in = null;

    /**
     * Communication channel output stream.
     */
    private ObjectOutputStream out = null;

    /**
     * Instantiation of a communication channel.
     *
     * @param hostName host name of the computational system where the server
     *                 is located.
     * @param portNumb Port number where the server is listening.
     */
    public ClientCom(String hostName, int portNumb) {
        serverHostName = hostName;
        serverPortNumb = portNumb;
    }

    /**
     * Opening of a communication channel.
     * Instantiation of a socket and associate it to the server's address.
     * Opening of input and output socket streams.
     *
     * @return True, if the communication channel was successfully opened; <br>
     * False, otherwise.
     */
    public boolean open() {
        boolean success = true;
        SocketAddress serverAddress = new InetSocketAddress(serverHostName,
                serverPortNumb);

        try {
            commSocket = new Socket();
            commSocket.connect(serverAddress);
        } catch (UnknownHostException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - unknown server hostname: " + serverHostName + "!");
            e.printStackTrace();
            System.exit(1);
        } catch (NoRouteToHostException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - unattainable server hostname: " + serverHostName + "!");
            e.printStackTrace();
            System.exit(1);
        } catch (ConnectException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - the server isn't responding on: " + serverHostName + "."
                    + serverPortNumb + "!");
            if (e.getMessage().equals("Connection refused"))
                success = false;
            else {
                System.out.println(e.getMessage() + "!");
                e.printStackTrace();
                System.exit(1);
            }
        } catch (SocketTimeoutException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - timeout trying to establish the connection: " +
                    serverHostName + "." + serverPortNumb + "!");
            success = false;
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - unknown error while trying to establish the connection: " +
                    serverHostName + "." + serverPortNumb + "!");
            e.printStackTrace();
            System.exit(1);
        }

        if (!success)
            return success;

        try {
            out = new ObjectOutputStream(commSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - unable to open socket output channel!");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            in = new ObjectInputStream(commSocket.getInputStream());
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - unable to open socket input channel!");
            e.printStackTrace();
            System.exit(1);
        }

        return success;
    }

    /**
     * Closing of the communication channel, socket input and output streams and
     * closing of the socket.
     */
    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - unable to close socket input channel!");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            out.close();
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - unable to close socket output channel!");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            commSocket.close();
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - unable to close socket!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Read an object from the communication channel input stream.
     *
     * @return The read object.
     */
    public Object readObject() {
        Object fromServer = null;

        try {
            fromServer = in.readObject();
        } catch (InvalidClassException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - the read object cannot be deserialized!");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - an error occurred while reading an object from the socket input stream!");
            e.printStackTrace();
            System.exit(1);
        } catch (ClassNotFoundException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - read object unknow data type!");
            e.printStackTrace();
            System.exit(1);
        }

        return fromServer;
    }

    /**
     * Write an object to the communication channel output stream.
     *
     * @param toServer Object to be written.
     */
    public void writeObject(Object toServer) {
        try {
            out.writeObject(toServer);
        } catch (InvalidClassException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - the written object cannot be deserialized!");
            e.printStackTrace();
            System.exit(1);
        } catch (NotSerializableException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - the given object is an instance of a non serializable data type!");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - an error occurred while writing an object from the socket output stream!");
            e.printStackTrace();
            //System.exit(1);
        }
    }
}

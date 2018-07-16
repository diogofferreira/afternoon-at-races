package communication;

import java.io.*;
import java.net.*;

/**
 * This data type implements the server side communication channel, in order to
 * exchange messages over sockets using the TCP protocol.
 * The exchanged data type is based on objects.
 */
public class ServerCom {
    /**
     * Listening socket.
     *
     * @serialField listeningSocket
     */
    private ServerSocket listeningSocket = null;

    /**
     * Communication socket.
     *
     * @serialField commSocket
     */
    private Socket commSocket = null;

    /**
     * Server listening port number.
     *
     * @serialField serverPortNumb
     */
    private int serverPortNumb;

    /**
     * Communication channel input stream.
     *
     * @serialField in
     */
    private ObjectInputStream in = null;

    /**
     * Communication channel output stream.
     *
     * @serialField out
     */
    private ObjectOutputStream out = null;

    /**
     * Instantiation of a communication channel (method 1).
     *
     * @param portNumb Port number where the server is listening.
     */
    public ServerCom(int portNumb) {
        serverPortNumb = portNumb;
    }

    /**
     * Instantiation of a communication channel (method 2).
     *
     * @param portNumb Port number where the server is listening.
     * @param lSocket  Listening socket.
     */
    public ServerCom(int portNumb, ServerSocket lSocket) {
        serverPortNumb = portNumb;
        listeningSocket = lSocket;
    }

    /**
     * Service establishment.
     * Instantiation of a listening socket and associate it to the local machine's
     * IP address and public listening port.
     */
    public void start() {
        try {
            listeningSocket = new ServerSocket(serverPortNumb);
        } catch (BindException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - cannot associate the port number to the socket: " +
                    serverPortNumb + "!");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - unknown error while associating the port to the socket: " +
                    serverPortNumb + "!");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            listeningSocket.setSoTimeout(1000);
        } catch (SocketException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - cannot set socket timeout: " + serverPortNumb + "!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Service closing.
     * Closing of the listening socket.
     */
    public void end() {
        try {
            listeningSocket.close();
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - cannot close listening socket!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Listening process.
     * Creation of a communication channel for a pending request.
     * Instantiation of a communication socket and associate it to the client's
     * address.
     * Opening of the input and output socket streams.
     *
     * @return Communication channel.
     */
    public ServerCom accept() {
        ServerCom scon;
        boolean timeout = false;

        scon = new ServerCom(serverPortNumb, listeningSocket);
        try {
            scon.commSocket = listeningSocket.accept();
        } catch (SocketTimeoutException e) {
            timeout = true;
        } catch (SocketException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - the socket was closed while listening!");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - cannot open a communication channel for a pending request!");
            e.printStackTrace();
            System.exit(1);
        }

        if (timeout)
            return null;

        try {
            scon.in = new ObjectInputStream(scon.commSocket.getInputStream());
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - cannot open the socket input channel!");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            scon.out = new ObjectOutputStream(scon.commSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - cannot open the socket output channel!");
            e.printStackTrace();
            System.exit(1);
        }

        return scon;
    }

    /**
     * Closing of the communication channel.
     * Closing of the input and output streams of the socket.
     * Closing of the communication socket.
     */
    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - cannot close the socket input channel!");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            out.close();
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - cannot close the socket output channel!");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            commSocket.close();
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - cannot close communication socket!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Read an object from the communication channel input stream.
     *
     * @return Read object.
     */
    public Object readObject() {
        Object fromClient = null;

        try {
            fromClient = in.readObject();
        } catch (InvalidClassException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - the read object cannot be deserialized!");
            e.printStackTrace();
            //System.exit(1);
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - an error occurred while reading an object from the socket input stream!");
            e.printStackTrace();
            //System.exit(1);
        } catch (ClassNotFoundException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - read object unknow data type!");
            e.printStackTrace();
            //System.exit(1);
        }

        return fromClient;
    }

    /**
     * Write an object to the communication channel output stream.
     *
     * @param toClient Object to be written.
     */
    public void writeObject(Object toClient) {
        try {
            out.writeObject(toClient);
        } catch (InvalidClassException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - the written object cannot be deserialized!");
            e.printStackTrace();
            //System.exit(1);
        } catch (NotSerializableException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - the given object is an instance of a non serializable data type!");
            e.printStackTrace();
            //System.exit(1);
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() +
                    " - an error occurred while writing an object from the socket output stream!");
            e.printStackTrace();
            //System.exit(1);
        }
    }
}

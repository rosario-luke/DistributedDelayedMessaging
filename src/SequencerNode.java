import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.io.*;


/**
 * Main class for the Sequencer when implementing a linearizable model
 */
public class SequencerNode extends Thread {

    public static void main(String[] args) {

        try {
            if (args.length != 1) {
                System.out.println("Args should be 1 string containing the configuration file and optional command file");
                System.exit(1);

            }

            new SequencerNode().startServer(args);
        } catch (Exception e) {
            System.out.println("I/O failure: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void startServer(String[] files) throws Exception {

        String file = files[0];
        String commands = (files.length == 2) ? files[1] : null; // Set command file if specified

        ServerSocket serverSocket = null;
        boolean listening = true;
        ConfigurationFile config = ConfigurationManager.createConfig(file, commands);


            DelayQueue<DelayedServerMessage> dq = new DelayQueue<DelayedServerMessage>();



            // Setup clientNode and run
            ClientNode clientNode = new ClientNode(dq, config, "ClientNode");
            clientNode.start();

            HashMap<Character, DelayedServerMessage> lastMessages = new HashMap<Character, DelayedServerMessage>();
            Iterator<ServerInfo> it = config.getServerIterator();
            while (it.hasNext()) {
                ServerInfo cur = it.next();
                lastMessages.put(cur.getIdentifier(), null);
            }
            Random rand = new Random();
            MessageGenerator generator = new MessageGenerator(config, rand, lastMessages );

            serverSocket = new ServerSocket(config.getHostPort());
            serverSocket.setReuseAddress(true);
            System.out.println("Server now listening on port " + config.getHostPort());
            while (listening) {
                Socket clientSocket = null;
                try {
                    clientSocket = serverSocket.accept();
                } catch (IOException e) {
                    if(listening) {
                        System.out.println("Server Stopped.") ;
                        return;
                    }
                    throw new RuntimeException(
                            "Error accepting client connection", e);
                }

                new Thread(
                        new SequencerThread(clientSocket, config, generator, dq)
                ).start();
            }




    }



}



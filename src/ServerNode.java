import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.io.*;

/**
 * Main class for launching a Server
 */
public class ServerNode extends Thread {

    private ConcurrentHashMap<Integer, ServerValue> myTable;
    private ConfigurationFile config;
    private ConcurrentHashMap<Command, CommandResponse> myCommands;
    private MessageGenerator generator;
    private HashMap<Character, DelayedServerMessage> lastMessages;
    private Random rand;
    private DelayQueue<DelayedServerMessage> delayQueue;
    private boolean hasLaunchedRepair;
    private RepairThread repairThread;
    public static void main(String[] args) {

        try {
            if (args.length != 1 && args.length != 2) {
                System.out.println("Args should be 1 string containing the configuration file and optional command file");
                System.exit(1);

            }

            new ServerNode().startServer(args);
        } catch (Exception e) {
            System.out.println("I/O failure: " + e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     *
     * @param files The configuration and Command File names
     * @throws Exception Errors found while running the server
     */
    public void startServer(String[] files) throws Exception {

        String file = files[0];
        String commands = (files.length == 2) ? files[1] : null; // Set command file if specified
        myTable = new ConcurrentHashMap<Integer, ServerValue>();
        myCommands = new ConcurrentHashMap<Command, CommandResponse>();
        lastMessages = new HashMap<Character, DelayedServerMessage>();
        ServerSocket serverSocket = null;
        boolean listening = true;
        config = ConfigurationManager.createConfig(file, commands);
        hasLaunchedRepair = false;
        try {


            delayQueue = new DelayQueue<DelayedServerMessage>();


            Iterator<ServerInfo> it = config.getServerIterator();
            while (it.hasNext()) {
                ServerInfo cur = it.next();
                lastMessages.put(cur.getIdentifier(), null);
            }
            rand = new Random();
            generator = new MessageGenerator(config, rand, lastMessages);

            // Setup CommandConsole and run
            CommandConsole commandConsole = new CommandConsole(delayQueue, config, "CommandConsole", myTable, myCommands, generator);
            commandConsole.start();

            // Setup clientNode and run
            ClientNode clientNode = new ClientNode(delayQueue, config, "ClientNode");
            clientNode.start();

            // Create Repair thread but don't run
            repairThread = new RepairThread(config, generator, myTable, myCommands, delayQueue);

            serverSocket = new ServerSocket(config.getHostPort());
            serverSocket.setReuseAddress(true);
            System.out.println("Server running on " + serverSocket.getInetAddress().getHostAddress() + ":" + config.getHostPort());
            while (listening) {
                handleServerRequest(serverSocket);
            }


        } catch (IOException e) {
            System.err.println("Could not listen on port: " + config.getHostPort());

            System.exit(-1);
        } finally {
            serverSocket.close();

        }

    }

    private void handleServerRequest(ServerSocket serverSocket) {
        try {
            Socket socket = serverSocket.accept();
            BufferedReader _in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String fullMessage = _in.readLine();
            if (fullMessage.startsWith("Command")) { // WE ARE RECIEVING A COMMAND
                Command command = new Command(fullMessage.split("::")[0]);
                long timestamp = command.getTimestamp();
                boolean fromMyself = command.getOrigin() == config.getHostIdentifier();

                if (command.isLinearOrSequential()) {
                    switch (command.getType()) {
                        case Command.GET_COMMAND:
                            // No Need To Do Anything
                            break;
                        case Command.DELETE_COMMAND:
                            myTable.remove(command.getKey());
                            if(!fromMyself){System.out.println("Key " + command.getKey() + " deleted");}
                            break;
                        case Command.INSERT_COMMAND:
                            myTable.put(command.getKey(), new ServerValue(command.getValue(), timestamp));
                            if(!fromMyself){System.out.println("Inserted key " + command.getKey());}
                            break;
                        case Command.UPDATE_COMMAND:
                            ServerValue sv = myTable.get(command.getKey());
                            int prev = 0;
                            if(sv != null){ prev = sv.getValue();}
                            myTable.put(command.getKey(), new ServerValue(command.getValue(), timestamp));
                            if(!fromMyself) {
                                if (sv != null) {
                                    System.out.println("Key " + command.getKey() + " changed from " + prev + " to " + command.getValue());
                                } else {
                                    System.out.println("Inserted key " + command.getKey());
                                }
                            }
                            break;
                        case Command.SEARCH_COMMAND:
                            ServerValue curValue = myTable.get(command.getKey());
                            DelayedServerMessage res;
                            if(curValue != null){
                                res = generator.GenerateResponseMessageFromCommand(command, curValue.getValue(), curValue.getTimestamp());
                            } else {
                                res= generator.GenerateResponseMessageFromCommand(command, Integer.MIN_VALUE, -1);
                            }
                            delayQueue.add(res);
                            break;

                    }
                    if (command.getOrigin() == config.getHostIdentifier()) { // IF THE COMMAND IS FROM MYSELF NOTIFY SO THAT COMMANDS CONTINUE TO EXECUTE
                        myCommands.get(command).recieveCommandFromMyself();
                    }
                } else {
                    if(!hasLaunchedRepair){
                        repairThread.start();
                        hasLaunchedRepair = true;
                    }
                    DelayedServerMessage m = null;
                    ServerValue sv = myTable.get(command.getKey());
                    switch (command.getType()) {
                        case Command.GET_COMMAND:
                            if(sv != null){
                                m = generator.GenerateResponseMessageFromCommand(command,sv.getValue(),sv.getTimestamp());
                            } else {
                                m = generator.GenerateResponseMessageFromCommand(command, Integer.MIN_VALUE, -1);
                            }
                            break;
                        case Command.DELETE_COMMAND:
                            myTable.remove(command.getKey());
                            if(!fromMyself){System.out.println("Key " + command.getKey() + " deleted");}
                            m = generator.GenerateResponseMessageFromCommand(command,sv.getValue(),sv.getTimestamp());
                            break;
                        case Command.INSERT_COMMAND:
                            myTable.put(command.getKey(), new ServerValue(command.getValue(), timestamp));
                            if(!fromMyself){System.out.println("Inserted key " + command.getKey());}
                            m = generator.GenerateResponseMessageFromCommand(command, command.getValue(),timestamp);
                            break;
                        case Command.UPDATE_COMMAND:
                            myTable.put(command.getKey(), new ServerValue(command.getValue(), timestamp));
                            if(!fromMyself) {
                                if (sv != null) {
                                    System.out.println("Key " + command.getKey() + " changed from " + sv.getValue() + " to " + command.getValue());
                                } else {
                                    System.out.println("Inserted key " + command.getKey());
                                }
                            } else {
                                System.out.println("Updated key " + command.getKey() + " to " + command.getValue() + " but not acknowledge");
                            }
                            m = generator.GenerateResponseMessageFromCommand(command, command.getValue(),timestamp);
                            break;
                        case Command.SEARCH_COMMAND:
                            ServerValue curValue = myTable.get(command.getKey());

                            if(curValue != null){
                                m = generator.GenerateResponseMessageFromCommand(command, curValue.getValue(), curValue.getTimestamp());
                            } else {
                                m = generator.GenerateResponseMessageFromCommand(command, Integer.MIN_VALUE, -1);
                            }

                            break;
                    }
                    if(command.getOrigin() != config.getHostIdentifier() || command.getType() == Command.SEARCH_COMMAND) { // Send Response if Command wasn't from myself
                        delayQueue.add(m);
                    }

                }


            } else { // WE ARE RECEIVING A RESPONSE/ACKNOWLEDGEMENT

                Response response = new Response(fullMessage.split("::")[0]);
                Command command = new Command(fullMessage.split("::")[1]);

                CommandResponse commandRes = myCommands.get(command);
                //System.out.println("Recieved response " + response.toString() + " for commmand " + command.toString());
                commandRes.addResponse(response);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
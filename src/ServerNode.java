import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.io.*;



public class ServerNode extends Thread {

    private ConcurrentHashMap<Integer, ServerValue> myTable;
    private ConfigurationFile config;
    private ConcurrentHashMap<Command, CommandResponse> myCommands;
    private MessageGenerator generator;
    private HashMap<Character, DelayedServerMessage> lastMessages;
    private Random rand;
	public static void main(String[] args) {

		try {
			if(args.length != 1 && args.length != 2){
				System.out.println("Args should be 1 string containing the configuration file and optional command file");
				System.exit(1);

			}

			new ServerNode().startServer(args);
		} catch (Exception e) {
			System.out.println("I/O failure: " + e.getMessage());
			e.printStackTrace();
		}

	}

	public void startServer(String[] files) throws Exception {

		String file = files[0];
        String commands = (files.length == 2) ? files[1] : null; // Set command file if specified
        myTable = new ConcurrentHashMap<Integer, ServerValue>();
        myCommands = new ConcurrentHashMap<Command, CommandResponse>();
        lastMessages = new HashMap<Character, DelayedServerMessage>();
		ServerSocket serverSocket = null;
		boolean listening = true;
		config = ConfigurationManager.createConfig(file, commands);
		
		try{
			
			
			
			DelayQueue<DelayedServerMessage> dq = new DelayQueue<DelayedServerMessage>();


            Iterator<ServerInfo> it = config.getServerIterator();
            while (it.hasNext()) {
                ServerInfo cur = it.next();
                lastMessages.put(cur.getIdentifier(), null);
            }
            rand = new Random();
            generator = new MessageGenerator(config, rand, lastMessages);

			// Setup CommandConsole and run
			CommandConsole commandConsole = new CommandConsole(dq, config, "CommandConsole", myTable, myCommands, generator);
			commandConsole.start();
			
			// Setup clientNode and run
			ClientNode clientNode = new ClientNode(dq, config, "ClientNode");
			clientNode.start();
			
			serverSocket = new ServerSocket(config.getHostPort());
            serverSocket.setReuseAddress(true);
			System.out.println("Server now listening on port" + config.getHostPort());
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
            if(fullMessage.startsWith("Command")){ // WE ARE RECIEVING A COMMAND
                Command command = new Command(fullMessage.split("::")[0]);
                long timestamp = Long.parseLong(fullMessage.split("::")[1]);

                    if(command.isLinearOrSequential()){
                        switch(command.getType()){
                            case Command.GET_COMMAND:
                                if(command.getModel() == Command.LINEARIZABLE_MODEL && command.getOrigin() == config.getHostIdentifier()){ // IF THE COMMAND IF FROM MYSELF AND LINEARIZABLE
                                    System.out.println("get(" + command.getKey() + ") = " + myTable.get(command.getKey()).getValue());
                                }
                                break;
                            case Command.DELETE_COMMAND:
                                myTable.remove(command.getKey());
                                System.out.println("Key " + command.getKey() + " deleted");
                                break;
                            case Command.INSERT_COMMAND:
                                myTable.put(command.getKey(), new ServerValue(command.getValue(), timestamp));
                                System.out.println("Inserted key" + command.getKey());
                                break;
                            case Command.UPDATE_COMMAND:
                                ServerValue sv = myTable.get(command.getKey());
                                myTable.put(command.getKey(), new ServerValue(command.getValue(), timestamp));
                                if(sv != null){
                                    System.out.println("Key " + command.getKey() + " changed from " + sv.getValue() + " to " + command.getValue());
                                } else {
                                    System.out.println("Inserted key" + command.getKey());
                                }
                                break;
                        }
                        if(command.getOrigin() == config.getHostIdentifier()) { // IF THE COMMAND IS FROM MYSELF NOTIFY SO THAT COMMANDS CONTINUE TO EXECUTE
                            //myCommands.get(command).recieveCommandFromMyself();
                            CommandResponse cr = myCommands.get(command);
                            if(cr == null){
                                System.out.println("cr was null");
                            } else {
                                cr.recieveCommandFromMyself();
                            }
                        }
                    } else {
                        DelayedServerMessage m;
                        switch(command.getType()){
                            case Command.GET_COMMAND:

                                break;
                            case Command.DELETE_COMMAND:
                                myTable.remove(command.getKey());
                                System.out.println("Key " + command.getKey() + " deleted");
                                break;
                            case Command.INSERT_COMMAND:
                                myTable.put(command.getKey(), new ServerValue(command.getValue(), timestamp));
                                System.out.println("Inserted key" + command.getKey());
                                break;
                            case Command.UPDATE_COMMAND:
                                ServerValue sv = myTable.get(command.getKey());
                                myTable.put(command.getKey(), new ServerValue(command.getValue(), timestamp));
                                if(sv != null){
                                    System.out.println("Key " + command.getKey() + " changed from " + sv.getValue() + " to " + command.getValue());
                                } else {
                                    System.out.println("Inserted key" + command.getKey());
                                }
                                break;
                        }

                    }


            } else { // WE ARE RECIEVING A RESPONSE/ACKNOWLEDGEMENT

            }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
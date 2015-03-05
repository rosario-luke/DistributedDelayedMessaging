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



public class ServerNode extends Thread {

    private HashMap<Integer, ServerValue> myTable;
    private ConfigurationFile config;
    private HashMap<Command, CommandResponse> myCommands;
	public static void main(String[] args) {

		try {
			if(args.length != 1){
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
        myTable = new HashMap<Integer, ServerValue>();
        myCommands = new HashMap<Command, CommandResponse>();
		ServerSocket serverSocket = null;
		boolean listening = true;
		config = ConfigurationManager.createConfig(file, commands);
		
		try{
			
			
			
			DelayQueue<DelayedServerMessage> dq = new DelayQueue<DelayedServerMessage>();

			// Setup CommandConsole and run
			CommandConsole commandConsole = new CommandConsole(dq, config, "CommandConsole", myTable, myCommands);
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
                if(command.getOrigin() == config.getHostIdentifier()){ // THE COMMAND IS FROM MYSELF

                } else{ // THE COMMAND IS COMING FROM ANOTHER NODE

                }

            } else { // WE ARE RECIEVING A RESPONSE/ACKNOWLEDGEMENT

            }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	

	

}
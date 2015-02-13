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

	public static void main(String[] args) {

		try {
			if(args.length != 1){
				System.out.println("Args should be 1 string containing the configuration file");
				System.exit(1);

			}
			new ServerNode().startServer(args[0]);
		} catch (Exception e) {
			System.out.println("I/O failure: " + e.getMessage());
			e.printStackTrace();
		}

	}

	public void startServer(String file) throws Exception {

		
		ServerSocket serverSocket = null;
		boolean listening = true;
		ConfigurationFile config = ConfigurationManager.createConfig(file);
		
		try{
			
			
			
			DelayQueue<ServerMessage> dq = new DelayQueue<ServerMessage>();

			// Setup CommandConsole and run
			CommandConsole commandConsole = new CommandConsole(dq, config, "CommandConsole");
			commandConsole.start();
			
			// Setup clientNode and run
			ClientNode clientNode = new ClientNode(dq, config, "ClientNode");
			clientNode.start();
			
			serverSocket = new ServerSocket(config.getHostPort());
            serverSocket.setReuseAddress(true);
			System.out.println("Server now listening on port" + config.getHostPort());
			while (listening) {
				handleServerRequest(serverSocket, config);
			}
	 


		} catch (IOException e) {
			System.err.println("Could not listen on port: " + config.getHostPort());
			System.exit(-1);
		} finally {
            serverSocket.close();
			
		}

	}
	
	private void handleServerRequest(ServerSocket serverSocket, ConfigurationFile con) {
		try {
			Socket socket = serverSocket.accept();
			BufferedReader _in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String fullMessage = _in.readLine();
			String message = fullMessage.split("::")[0];
			char sender = fullMessage.charAt(fullMessage.length()-1);
			int maxDelay = con.findInfoByIdentifier(sender).getPortDelay();
			System.out.println("Received '" + message +"' from " + sender + ", Max delay is " + maxDelay + " s, system time is " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	

	

}
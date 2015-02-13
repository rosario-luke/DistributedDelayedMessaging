import java.net.*;
import java.io.*;
 
/**
 * Demo Server: Contains a multi-threaded socket server sample code.
 */
public class ServerDemo extends Thread
{
	final static int _portNumber = 5559; //Arbitrary port number
 
	public static void main(String[] args) 
	{
		try {
			new ServerDemo().startServer();
		} catch (Exception e) {
			System.out.println("I/O failure: " + e.getMessage());
			e.printStackTrace();
		}
 
	}
 
	public void startServer() throws Exception {
		ServerSocket serverSocket = null;
		boolean listening = true;
 
		try {
			serverSocket = new ServerSocket(_portNumber);
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + _portNumber);
			System.exit(-1);
		}
 
		while (listening) {
			handleClientRequest(serverSocket);
		}
 
		serverSocket.close();
	}
 
	private void handleClientRequest(ServerSocket serverSocket) {
		try {
			new ConnectionRequestHandler(serverSocket.accept()).run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
 
	/**
	 * Handles client connection requests. 
	 */
	public class ConnectionRequestHandler implements Runnable{
		private Socket _socket = null;
		private PrintWriter _out = null;
		private BufferedReader _in = null;
 
		public ConnectionRequestHandler(Socket socket) {
			_socket = socket;
		}
 
		public void run() {
			System.out.println("Client connected to socket: " + _socket.toString());
 
			try {
				_out = new PrintWriter(_socket.getOutputStream(), true);
				_in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
 
				String inputLine, outputLine;
				BusinessLogic businessLogic = new BusinessLogic();
				//outputLine = businessLogic.processInput(null);
				//_out.println(outputLine);
 
				//Read from socket and write back the response to client. 
				while ((inputLine = _in.readLine()) != null) {
					System.out.println(inputLine);
					if(inputLine.equals("exit")){
						outputLine = null;
					} else {
						outputLine = inputLine;
					}
					if(outputLine != null) {
						_out.println(outputLine);
						if (outputLine.equals("exit")) {
							System.out.println("Server is closing socket for client:" + _socket.getLocalSocketAddress());
							break;
						}
					} else {
						System.out.println("OutputLine is null!!!");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally { //In case anything goes wrong we need to close our I/O streams and sockets.
				try {
					_out.close();
					_in.close();
					_socket.close();
				} catch(Exception e) { 
					System.out.println("Couldn't close I/O streams");
				}
			}
		}
 
	}
 
	/**
	 * Handles business logic of application.
	 */
	public static class BusinessLogic {
		private static final int LoginUserName = 0;
		private static final int LoginPassword = 1;
		private static final int AuthenticateUser = 2;
		private static final int AuthSuccess   = 3;
 
		private int state = LoginUserName;
 
		private String userName =  null;
		private String userPassword =  null;
 
		public String processInput(String clientRequest) {
			String reply = null;
			try {
				if(clientRequest != null && clientRequest.equalsIgnoreCase("login")) {
					state = LoginPassword;
				}if(clientRequest != null && clientRequest.equalsIgnoreCase("exit")) {
					return "exit";
				}
 
				if(state == LoginUserName) {
					reply = "Please Enter your user name: ";
					state = LoginPassword;
				} else if(state == LoginPassword) {
					userName = clientRequest;
					reply = "Please Enter your password: ";
					state = AuthenticateUser;
				} else if(state == AuthenticateUser) {
					userPassword = clientRequest;
					if(userName.equalsIgnoreCase("John") && userPassword.equals("doe")) { 
						reply = "Login Successful...";
						state = AuthSuccess;
					} else {
						reply = "Invalid Credentials!!! Please try again. Enter you user name: ";
						state = LoginPassword;
					}
				} else {
					reply = "Invalid Request!!!";
				}
			} catch(Exception e) {
				System.out.println("input process falied: " + e.getMessage());
				return "exit";
			}
 
			return reply;
		}
	}
}
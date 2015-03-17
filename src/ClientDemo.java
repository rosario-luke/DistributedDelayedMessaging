import java.io.*;
import java.net.*;
 
public class ClientDemo {
	public static void main(String[] args) {
		try {
			new ClientDemo().startClient();
		} catch (Exception e) {
			System.out.println("Something failed: " + e.getMessage());
			e.printStackTrace();
		}
	}
 
	public void startClient() throws IOException {
 
		Socket socket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		InetAddress host = null;
		BufferedReader stdIn = null;
 
		try {
			host = InetAddress.getLocalHost();
			socket = new Socket(host.getHostName(), 5559);
 
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 
			stdIn = new BufferedReader(new InputStreamReader(System.in));
			String fromServer;
			String fromUser;
 
			//Read from socket and write back the response to server. 
			while ((fromServer = in.readLine()) != null) {
				System.out.println("Server - " + fromServer);
				if (fromServer.equals("exit"))
					break;
 
				fromUser = stdIn.readLine();
				if (fromUser != null) {
					System.out.println("Client - " + fromUser);
					out.println(fromUser);
				}
			}
		} catch (UnknownHostException e) {
			System.err.println("Cannot find the host: " + host.getHostName());
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't read/write from the connection: " + e.getMessage());
			System.exit(1);
		} finally { //Make sure we always clean up
			out.close();
			in.close();
			stdIn.close();
			socket.close();
		}
	}
}
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.DelayQueue;

public class ClientNode implements Runnable {

		private DelayQueue<DelayedServerMessage> delayQueue;
		private ConfigurationFile config;
		private Boolean running;
		private String threadName;
		private Thread t;
		

		public ClientNode(DelayQueue<DelayedServerMessage> dq, ConfigurationFile con, String name) {
			delayQueue = dq;
			config = con;
			running = true;
			threadName = name;
		
		}

		public void run() {

			Socket socket = null;
			PrintWriter out = null;
			
			InetAddress host = null;
			
			//System.out.println("ClientNode running");
            DelayedServerMessage message = null;

            while (running) {

                try{
                    message = delayQueue.take();
                } catch(InterruptedException e){
                    System.err.println("Could not take from queue" + e.getMessage());
                    if(out != null){out.close();}
                    System.exit(1);
                }

                try {
                    host = InetAddress.getLocalHost();
                } catch(UnknownHostException e){
                    System.err.println("Problems finding local host" + e.getMessage());
                    System.exit(1);
                }


                try{

                    socket = new Socket(host.getHostName(), message.getServerInfo().getPortNumber());
                    // Attach socket to reader
                    out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(message.getMessage() + "::" + config.getHostIdentifier());
                }catch (IOException e) {
                    System.err.println("Couldn't write to connection " + message.getServerInfo().getIdentifier()
                            + " " + e.getMessage());
                    //System.exit(1);
                }



                // Send message through socket


            }
            //if(out != null){out.close();}


		}
		
		public void start(){
			System.out.println("Starting" + threadName);
			if(t == null){
				t = new Thread(this, threadName);
			}
			t.start();
		}

	}
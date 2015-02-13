import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

public class CommandConsole implements Runnable {

		private DelayQueue<ServerMessage> delayQueue;
		private ConfigurationFile config;
		private HashMap<Character, ServerMessage> lastMessages;
		private String threadName;
		private Thread t;

		public CommandConsole(DelayQueue<ServerMessage> dq, ConfigurationFile con, String name) {
			delayQueue = dq;
			config = con;
			lastMessages = new HashMap<Character, ServerMessage>();
			Iterator<ServerInfo> it = config.getServerIterator();
			while (it.hasNext()) {
				ServerInfo cur = it.next();
				lastMessages.put(cur.getIdentifier(), null);
			}
			threadName = name;
		}

		public void run() {
			Scanner _input = new Scanner(System.in);
			String inputLine;
			Random rand = new Random();
			System.out.println("Command console running");
			try {

				
				
				// Start asking for commands
				while (!((inputLine = _input.nextLine()).equals("exit"))) {
					System.out.println(inputLine);
					ServerMessage nMessage;
					// Split up input to get destination
					String[] splitMessage = inputLine.split(" ");
					String message = splitMessage[1];
					char destination = splitMessage[2].charAt(0);

					// Find server info associated with the destination
					ServerInfo desInfo = config
							.findInfoByIdentifier(destination);

					// Calculate random delay based upon the specific port
					long randDelay = TimeUnit.MILLISECONDS.convert(
							(long) rand.nextInt(desInfo.getPortDelay()),
							TimeUnit.SECONDS);

					// Get the delay of the last object to go into the queue
					// If the start time of that message was before the current
					// message, then it does not exist in the queue
					// and the new message delay will be the random delay we
					// just generated
					// If the last message still hasn't sent, then we will take
					// the max of the two delays
					ServerMessage lastMessage = lastMessages.get(desInfo
							.getIdentifier());
					if (lastMessage == null || lastMessage.getDelay(TimeUnit.MILLISECONDS) < 0) {
						nMessage = new ServerMessage(desInfo, message,
								randDelay);
						lastMessages.put(desInfo.getIdentifier(), nMessage);
					} else {
						if (lastMessage.getDelay(TimeUnit.MILLISECONDS) > randDelay) {
							nMessage = new ServerMessage(
									desInfo,
									message,
									lastMessage.getDelay(TimeUnit.MILLISECONDS) + 100);
							lastMessages.put(desInfo.getIdentifier(), nMessage);
						} else {
							nMessage = new ServerMessage(desInfo, message,
									randDelay);
							lastMessages.put(desInfo.getIdentifier(), nMessage);
						}
					}
					
					delayQueue.add(nMessage);
					System.out.println("Sent '" + nMessage.getMessage() + "' to " + nMessage.getServerInfo().getIdentifier() +", system time is " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
					
				}

			} catch (Exception e) {
				System.err.print("Caught error " + e.getMessage());
				e.printStackTrace();
			} finally {
				_input.close();
			}
		}
		
		public void start(){
			System.out.println("Starting" + threadName);
			if(t == null){
				t = new Thread(this, threadName);
			}
			t.start();
		}

	}
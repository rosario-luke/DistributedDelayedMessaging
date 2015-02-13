import java.io.File;
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
        private MessageGenerator generator;
        private Random rand;
		public CommandConsole(DelayQueue<ServerMessage> dq, ConfigurationFile con, String name) {
			delayQueue = dq;
			config = con;
			lastMessages = new HashMap<Character, ServerMessage>();
			Iterator<ServerInfo> it = config.getServerIterator();
			while (it.hasNext()) {
				ServerInfo cur = it.next();
				lastMessages.put(cur.getIdentifier(), null);
			}
            rand = new Random();
            generator = new MessageGenerator(config, rand, lastMessages );
			threadName = name;
		}

		public void run() {
			Scanner _input = null;
			String inputLine;

			System.out.println("Command console running");
			try {

				if(config.hasCommandFile()){
                    File cf = new File(config.getCommandFile());
                    _input = new Scanner(cf);

                    while (_input.hasNext()) {
                        inputLine = _input.nextLine();
                        ServerMessage nMessage = generator.GenerateMessageFromCommand(inputLine);

                        delayQueue.add(nMessage);
                        System.out.println("Sent '" + nMessage.getMessage() + "' to " + nMessage.getServerInfo().getIdentifier() +", system time is " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));

                    }
                    _input.close();
                }
                _input = new Scanner(System.in);
				// Start asking for commands
				while (!((inputLine = _input.nextLine()).equals("exit"))) {

					ServerMessage nMessage = generator.GenerateMessageFromCommand(inputLine);
					
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
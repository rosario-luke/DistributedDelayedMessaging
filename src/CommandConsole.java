import java.io.File;
import java.util.*;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

public class CommandConsole implements Runnable {

		private DelayQueue<DelayedServerMessage> delayQueue;
		private ConfigurationFile config;
		private HashMap<Character, DelayedServerMessage> lastMessages;
		private String threadName;
		private Thread t;
        private MessageGenerator generator;
        private Random rand;
		public CommandConsole(DelayQueue<DelayedServerMessage> dq, ConfigurationFile con, String name) {
			delayQueue = dq;
			config = con;
			lastMessages = new HashMap<Character, DelayedServerMessage>();
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

			System.out.println("Press enter to start sending commands");
            _input = new Scanner(System.in);
            _input.nextLine();

			try {

				if(config.hasCommandFile()){
                    System.out.println("Sending commands from file");
                    File cf = new File(config.getCommandFile());
                    _input = new Scanner(cf);

                    while (_input.hasNext()) {
                        inputLine = _input.nextLine();
                        ArrayList<DelayedServerMessage> mList = generator.GenerateMessageFromCommandString(inputLine);
                        for(DelayedServerMessage nMessage : mList) {
                            delayQueue.add(nMessage);
                            System.out.println("Sent '" + nMessage.getMessage().toString() + "' to " + nMessage.getServerInfo().getIdentifier() + ", system time is " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
                        }
                    }
                    _input.close();
                }
                _input = new Scanner(System.in);
				// Start asking for commands
                System.out.println("Please enter commands now");
                while(true) {

                    inputLine = _input.nextLine();
                    while (!(inputLine.equals("exit"))) {

                        ArrayList<DelayedServerMessage> mList = generator.GenerateMessageFromCommandString(inputLine);
                        for (DelayedServerMessage nMessage : mList) {
                            delayQueue.add(nMessage);
                            System.out.println("Sent '" + nMessage.getMessage().toString() + "' to " + nMessage.getServerInfo().getIdentifier() + ", system time is " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
                        }

                    }
                }

			} catch (Exception e) {
				System.err.print("Caught error " + e.getMessage());
				e.printStackTrace();
			} finally {
				_input.close();
			}
		}
		
		public void start(){

			if(t == null){
				t = new Thread(this, threadName);
			}
			t.start();
		}

	}
import java.io.File;
import java.util.*;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

public class CommandConsole implements Runnable {

    private DelayQueue<DelayedServerMessage> delayQueue;
    private ConfigurationFile config;
    private HashMap<Character, DelayedServerMessage> lastMessages;
    private HashMap<Integer, ServerValue> myTable;
    private String threadName;
    private Thread t;
    private MessageGenerator generator;
    private Random rand;
    private HashMap<Command, CommandResponse> myCommands;

    public CommandConsole(DelayQueue<DelayedServerMessage> dq, ConfigurationFile con, String name, HashMap<Integer, ServerValue> map,HashMap<Command, CommandResponse> mC ) {
        delayQueue = dq;
        config = con;
        lastMessages = new HashMap<Character, DelayedServerMessage>();
        Iterator<ServerInfo> it = config.getServerIterator();
        while (it.hasNext()) {
            ServerInfo cur = it.next();
            lastMessages.put(cur.getIdentifier(), null);
        }
        rand = new Random();
        generator = new MessageGenerator(config, rand, lastMessages);
        threadName = name;
        myTable = map;
        myCommands = mC;
    }

    public void run() {
        Scanner _input = null;
        String inputLine;

        System.out.println("Press enter to start sending commands");
        _input = new Scanner(System.in);
        _input.nextLine();

        try {

            if (config.hasCommandFile()) {
                System.out.println("Sending commands from file");
                File cf = new File(config.getCommandFile());
                _input = new Scanner(cf);

                while (_input.hasNext()) {
                    inputLine = _input.nextLine();
                    ArrayList<DelayedServerMessage> mList = generator.GenerateMessageFromCommandString(inputLine);
                    if(mList.size() == 1 && mList.get(0).isReadSeq()){
                        // IF THE COMMAND IS READ AND MODEL IS SEQUENTIALLY CONSISTENT, DON'T TOTAL BROADCAST, JUST OUTPUT VALUE
                        Command c = mList.get(0).getMessage();
                        if(myTable.get(c.getKey()) != null) {
                            System.out.println("get(" + c.getKey() + ") = " + myTable.get(c.getKey()).getValue());
                        } else {
                            System.out.println("get(" + c.getKey() + ") occurred but value did not exist");
                        }
                    } else {
                        Command c = mList.get(0).getMessage();
                        myCommands.put(c, new CommandResponse(c));
                        for (DelayedServerMessage nMessage : mList) {
                            delayQueue.add(nMessage);
                            System.out.println("Sent '" + nMessage.getMessage().toString() + "' to " + nMessage.getServerInfo().getIdentifier() + ", system time is " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
                        }
                    }
                    myCommands.get(mList.get(0).getMessage()).waitForResponses();
                }
                _input.close();
            }
            _input = new Scanner(System.in);
            // Start asking for commands
            System.out.println("Please enter commands now");

            while (!((inputLine = _input.nextLine()).equals("exit"))) {

                ArrayList<DelayedServerMessage> mList = generator.GenerateMessageFromCommandString(inputLine);
                if(mList.size() == 1 && mList.get(0).isReadSeq()){
                    // IF THE COMMAND IS READ AND MODEL IS SEQUENTIALLY CONSISTENT, DON'T TOTAL BROADCAST, JUST OUTPUT VALUE
                    Command c = mList.get(0).getMessage();
                    if(myTable.get(c.getKey()) != null) {
                        System.out.println("get(" + c.getKey() + ") = " + myTable.get(c.getKey()).getValue());
                    } else {
                        System.out.println("get(" + c.getKey() + ") occurred but value did not exist");
                    }
                } else {
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

    public void start() {

        if (t == null) {
            t = new Thread(this, threadName);
        }
        t.start();
    }

}
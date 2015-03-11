import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.Random;

/**
 * Class to help generate messages/commands to send to other servers
 */
public class MessageGenerator {
    private ConfigurationFile config;
    private Random rand;
    private HashMap<Character, DelayedServerMessage> lastMessages;

    public MessageGenerator(ConfigurationFile c, Random r, HashMap<Character, DelayedServerMessage> hm) {
        config = c;
        rand = r;
        lastMessages = hm;
    }

    public ArrayList<DelayedServerMessage> GenerateMessageFromCommandString(String inputLine) {
        DelayedServerMessage nMessage;
        // Split up input to get destination
        String[] s = inputLine.split(" ");
        Command c = null;
        String type = s[0];
        long uniq = System.currentTimeMillis();
        if (s[0].equals("delete")) {
            c = new Command(config.getHostIdentifier(), 1, Integer.parseInt(s[1]), 0, 0, uniq);
        } else if (s[0].equals("get")) {
            c = new Command(config.getHostIdentifier(), 2, Integer.parseInt(s[1]), 0, Integer.parseInt(s[2]), uniq);
        } else if (s[0].equals("insert")) {
            c = new Command(config.getHostIdentifier(), 3, Integer.parseInt(s[1]), Integer.parseInt(s[2]), Integer.parseInt(s[3]), uniq);
        } else if (s[0].equals("update")) {
            c = new Command(config.getHostIdentifier(), 4, Integer.parseInt(s[1]), Integer.parseInt(s[2]), Integer.parseInt(s[3]), uniq);
        } else if(s[0].equals("search")){
             c = new Command(config.getHostIdentifier(), Command.SEARCH_COMMAND, Integer.parseInt(s[1]), 0, Command.EVENTUAL_2_MODEL, uniq);
        }


        //String message = splitMessage[1];
        //char destination = splitMessage[2].charAt(0);
        ArrayList<DelayedServerMessage> dList = new ArrayList<DelayedServerMessage>();

        if (c.getModel() == Command.EVENTUAL_1_MODEL || c.getModel() == Command.EVENTUAL_2_MODEL) {
            // Find server info associated with the destination
            Iterator<ServerInfo> it = config.getServerIterator();
            ServerInfo desInfo;
            while (it.hasNext()) {
                desInfo = it.next();
                if (desInfo.getIdentifier() == 'S') {
                    continue;
                }
                dList.add(createMessageWithDelay(desInfo, c));
            }

        } else {
            ServerInfo desInfo = config.findInfoByIdentifier('S'); // Get Sequencer Identifier
            nMessage = createMessageWithDelay(desInfo, c);
            if (c.getModel() == Command.SEQUENTIALLY_CONSISTENT_MODEL && c.getType() == Command.GET_COMMAND) {
                nMessage.setReadSeq(true);
            }
            dList.add(nMessage);
        }
        return dList;
    }

    public ArrayList<DelayedServerMessage> GenerateMessageFromCommand(Command c) {


        ArrayList<DelayedServerMessage> dList = new ArrayList<DelayedServerMessage>();

        // Find server info associated with the destination
        Iterator<ServerInfo> it = config.getServerIterator();
        ServerInfo desInfo;
        while (it.hasNext()) {
            desInfo = it.next();
            dList.add(createMessageWithDelay(desInfo, c));
        }
        return dList;
    }


    public DelayedServerMessage GenerateResponseMessageFromCommand(Command c, int value, long timestamp) {

        ServerInfo desInfo = config.findInfoByIdentifier(c.getOrigin());
        DelayedServerMessage nMessage = createMessageWithDelay(desInfo, c);

        nMessage.setResponse(new Response(config.getHostIdentifier(), value, timestamp));
        return nMessage;
    }


    public DelayedServerMessage createMessageWithDelay(ServerInfo desInfo, Command c){
        DelayedServerMessage nMessage;
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
        DelayedServerMessage lastMessage = lastMessages.get(desInfo
                .getIdentifier());
        if (lastMessage == null || lastMessage.getDelay(TimeUnit.MILLISECONDS) < 0) {
            nMessage = new DelayedServerMessage(desInfo, c,
                    randDelay);
            lastMessages.put(desInfo.getIdentifier(), nMessage);
        } else {
            if (lastMessage.getDelay(TimeUnit.MILLISECONDS) > randDelay) {
                nMessage = new DelayedServerMessage(
                        desInfo,
                        c,
                        lastMessage.getDelay(TimeUnit.MILLISECONDS) + 100);
                lastMessages.put(desInfo.getIdentifier(), nMessage);
            } else {
                nMessage = new DelayedServerMessage(desInfo, c,
                        randDelay);
                lastMessages.put(desInfo.getIdentifier(), nMessage);
            }
        }
        return nMessage;
    }

}


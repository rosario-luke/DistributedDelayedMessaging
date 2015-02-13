import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.Random;
/**
 * Created by Lucas Rosario on 2/12/2015.
 */
public class MessageGenerator {
    private ConfigurationFile config;
    private Random rand;
    private HashMap<Character, ServerMessage> lastMessages;

    public MessageGenerator(ConfigurationFile c, Random r, HashMap<Character, ServerMessage> hm){
        config = c;
        rand = r;
        lastMessages = hm;
    }
    public ServerMessage GenerateMessageFromCommand(String inputLine){
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
        return nMessage;
    }
}

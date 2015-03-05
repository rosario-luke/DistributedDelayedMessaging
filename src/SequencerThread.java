import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Lucas Rosario on 3/5/2015.
 */
public class SequencerThread implements Runnable{


    protected Socket socket = null;
    protected ConfigurationFile config = null;
    protected MessageGenerator generator = null;
    protected DelayQueue<DelayedServerMessage> queue = null;

    public SequencerThread(Socket s, ConfigurationFile con, MessageGenerator g, DelayQueue<DelayedServerMessage> q) {
        socket = s;
        config = con;
        generator = g;
        queue = q;
    }

    public void run() {
        try {

            BufferedReader _in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String fullMessage = _in.readLine();
            String message = fullMessage.split("::")[0];
            char sender = fullMessage.charAt(fullMessage.length() - 1);
            int maxDelay = config.findInfoByIdentifier(sender).getPortDelay();
            System.out.println("Received '" + message + "' from " + sender + ", Max delay is " + maxDelay + " s, system time is " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
            Command c = new Command(message);
            ArrayList<DelayedServerMessage> messages = generator.GenerateMessageFromCommand(c);
            queue.addAll(messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

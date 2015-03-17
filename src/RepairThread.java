import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yuriy on 3/15/2015.
 */
public class RepairThread implements Runnable {
    protected ConfigurationFile config = null;
    protected MessageGenerator generator = null;
    protected DelayQueue<DelayedServerMessage> queue = null;
    protected ConcurrentHashMap<Integer, ServerValue> table=null;
    protected ConcurrentHashMap<Command, CommandResponse> commands= null;
    private Thread t;
    public RepairThread(ConfigurationFile con, MessageGenerator g, ConcurrentHashMap t, ConcurrentHashMap c, DelayQueue q) {

        config = con;
        generator = g;
        table= t;
        commands=c;
        queue = q;
    }

    public void run() {
        System.out.println("Repair thread started");
        while(true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                // handle the exception...
                // For example consider calling Thread.currentThread().interrupt(); here.
            }
            Enumeration<Integer> keys = table.keys();
            Integer curKey;
            ArrayList<DelayedServerMessage> mList;
            while (keys.hasMoreElements()) {
                curKey = keys.nextElement();
                String queryUpdatedValues = "search " + curKey.toString(); //placeholder  model number
                try {
                    mList = generator.GenerateMessageFromCommandString(queryUpdatedValues);
                } catch (Exception e) {
                    return;
                }
                Command c = mList.get(0).getCommand();
                commands.put(c, new CommandResponse(c, config.getNumberOfServers()));
                for (DelayedServerMessage nMessage : mList) {
                    queue.add(nMessage);
                }

                commands.get(mList.get(0).getCommand()).waitForResponses();
                Response bestResponse = CommandConsole.analyzeGetResponses(commands.get(c).getResponseList(), new Response(c.getOrigin(), table.get(c.getKey()).getValue(), table.get(c.getKey()).getTimestamp()));
                if (bestResponse.getValue() != table.get(curKey).getValue() && bestResponse.getTimestamp() > table.get(curKey).getTimestamp()) {
                    System.out.println("Repair tool updated key(" + curKey + ") from value(" + table.get(curKey).getValue() + ") to new value(" + bestResponse.getValue() + ")");
                    table.replace(curKey, new ServerValue(bestResponse.getValue(), bestResponse.getTimestamp()));
                }


            }
        }
    }

    public void start() {

        if (t == null) {
            t = new Thread(this);
        }
        t.start();
    }
}

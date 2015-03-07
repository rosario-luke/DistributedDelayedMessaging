import sun.plugin2.message.Message;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

public class CommandConsole implements Runnable {

    private DelayQueue<DelayedServerMessage> delayQueue;
    private ConfigurationFile config;
    private ConcurrentHashMap<Integer, ServerValue> myTable;
    private String threadName;
    private Thread t;
    private MessageGenerator generator;

    private ConcurrentHashMap<Command, CommandResponse> myCommands;

    public CommandConsole(DelayQueue<DelayedServerMessage> dq, ConfigurationFile con, String name, ConcurrentHashMap<Integer, ServerValue> map, ConcurrentHashMap<Command, CommandResponse> mC, MessageGenerator g) {
        delayQueue = dq;
        config = con;
        generator = g;
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


        if (config.hasCommandFile()) {
            System.out.println("Sending commands from file");
            File cf = new File(config.getCommandFile());
            Boolean fileFound = false;
            try {
                _input = new Scanner(cf);
                fileFound = true;
            } catch (FileNotFoundException e) {
                System.out.println("Unable to find file, skipping to manual input");
            }

            if (fileFound) {
                while (_input.hasNext()) {
                    inputLine = _input.nextLine();
                    handleCommand(inputLine);

                }
            }
            _input.close(); // Close File Reader
        }
        _input = new Scanner(System.in);
        // Start asking for commands
        System.out.println("Please enter commands now");

        while (!((inputLine = _input.nextLine()).equals("exit"))) {

            handleCommand(inputLine);
        }
        _input.close();


    }

    public void start() {

        if (t == null) {
            t = new Thread(this, threadName);
        }
        t.start();
    }

    public void printAcknowledgementForCommand(Command c) {

        switch (c.getType()) {
            case Command.GET_COMMAND:
                if (c.getModel() == Command.LINEARIZABLE_MODEL) {
                    System.out.println("get(" + c.getKey() + ") = " + myTable.get(c.getKey()).getValue());
                } else {
                    Response best = analyzeGetResponses(myCommands.get(c).getResponseList());
                    System.out.println("get(" + c.getKey() + ") = (" + best.getValue() + ", " + best.getTimestamp() + ") accepted");
                    for (Response r : myCommands.get(c).getResponseList()) {
                        if (!r.equals(best)) {
                            System.out.println("get(" + c.getKey() + ") = (" + r.getValue() + ", " + r.getTimestamp() + ") not accepted");
                        }
                    }
                }
                break;
            case Command.INSERT_COMMAND:
                System.out.println("Inserted key " + c.getKey());
                break;
            case Command.UPDATE_COMMAND:
                System.out.println("Key " + c.getKey() + " updated to " + c.getValue());
                break;
            case Command.DELETE_COMMAND:
                System.out.println("Key " + c.getKey() + " deleted");
                break;

        }

    }

    public void handleCommand(String inputLine) {

        if(inputLine.contains("Delay")){
            int delay = Integer.parseInt(inputLine.split(" ")[1]);
            try {
                Thread.sleep(delay * 1000);
            } catch(InterruptedException e){
                System.out.println("Error occurred while delaying next command");
            }

        } else if(inputLine.contains("Search")){
            int key = Integer.parseInt(inputLine.split(" ")[1]);
            ServerValue v = myTable.get(key);
            if(v == null) {
                System.out.println("Value for " + key + " is not defined");
            } else {
                System.out.println("Value (" + key + ") = " + v.getValue());
            }
        } else if(inputLine.contains("Show-all")){
            for(Map.Entry<Integer, ServerValue> ksv : myTable.entrySet()){
                System.out.println("Value (" + ksv.getKey() + ") = " + ksv.getValue().getValue());
            }
        }

        ArrayList<DelayedServerMessage> mList;
        try {
            mList = generator.GenerateMessageFromCommandString(inputLine);
        } catch (Exception e) {
            System.out.println("Error occured while generating message, skipping to next command");
            return;
        }
        if (mList.size() == 1 && mList.get(0).isReadSeq()) {
            // IF THE COMMAND IS READ AND MODEL IS SEQUENTIALLY CONSISTENT, DON'T TOTAL BROADCAST, JUST OUTPUT VALUE
            Command c = mList.get(0).getCommand();
            if (myTable.get(c.getKey()) != null) {
                System.out.println("get(" + c.getKey() + ") = " + myTable.get(c.getKey()).getValue());
            } else {
                System.out.println("get(" + c.getKey() + ") occurred but value did not exist");
            }
        } else {
            Command c = mList.get(0).getCommand();
            myCommands.put(c, new CommandResponse(c));
            for (DelayedServerMessage nMessage : mList) {
                delayQueue.add(nMessage);
            }
        }
        myCommands.get(mList.get(0).getCommand()).waitForResponses();
        printAcknowledgementForCommand(mList.get(0).getCommand());

    }

    public Response analyzeGetResponses(ArrayList<Response> responseList) {
        Response bestResponse = responseList.get(0);
        for (Response r : responseList) {
            if (r.getTimestamp() > bestResponse.getTimestamp()) {
                bestResponse = r;
            }
        }
        return bestResponse;
    }

}